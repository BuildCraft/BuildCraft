/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.lib.misc.HashUtil;
import buildcraft.lib.misc.StringUtilBC;
import buildcraft.lib.nbt.NbtSquisher;

public class GlobalSavedDataSnapshots {
    public static final String SNAPSHOT_FILE_EXTENSION = ".bcnbt";

    private static final Map<Side, GlobalSavedDataSnapshots> INSTANCES = new EnumMap<>(Side.class);
    public final List<Snapshot> snapshots = new ArrayList<>();
    private final File snapshotsFile;

    private GlobalSavedDataSnapshots(Side side) {
        snapshotsFile = new File(FMLCommonHandler.instance().getSavesDirectory().getParentFile(), "snapshots-" + side
            .name().toLowerCase(Locale.ROOT));
        if (!snapshotsFile.exists()) {
            if (!snapshotsFile.mkdirs()) {
                throw new RuntimeException("Failed to make the directories required for snapshots! \n\tdir = "
                    + snapshotsFile);
            }
        } else if (!snapshotsFile.isDirectory()) {
            throw new IllegalStateException("The snapshots directory was a file! We can't use this!\n\tfile = "
                + snapshotsFile);
        }
        readSnapshots();
    }

    public static void reInit(Side side) {
        INSTANCES.put(side, new GlobalSavedDataSnapshots(side));
    }

    public static GlobalSavedDataSnapshots get(Side side) {
        if (!INSTANCES.containsKey(side)) {
            INSTANCES.put(side, new GlobalSavedDataSnapshots(side));
        }
        return INSTANCES.get(side);
    }

    public static GlobalSavedDataSnapshots get(World world) {
        return get(world.isRemote ? Side.CLIENT : Side.SERVER);
    }

    public void exportSnapshot(Snapshot.Header header, File folder) {
        Snapshot snapshot = snapshots.stream().filter(s -> header.equals(s.header)).findAny().orElse(null);
        if (snapshot != null) {
            String fileName = header.name + SNAPSHOT_FILE_EXTENSION;
            fileName = StringUtilBC.replaceCharactersForFilename(fileName);
            writeSingleSnapshot(snapshot, new File(folder, fileName));
        }
    }

    private void writeSnapshots() {
        for (Snapshot snapshot : snapshots) {
            String hashName = HashUtil.convertHashToString(snapshot.header.hash);
            File snapshotFile = new File(snapshotsFile, hashName + SNAPSHOT_FILE_EXTENSION);
            writeSingleSnapshot(snapshot, snapshotFile);
        }
    }

    private static void writeSingleSnapshot(Snapshot snapshot, File snapshotFile) {
        if (!snapshotFile.exists()) {
            try (FileOutputStream fos = new FileOutputStream(snapshotFile)) {
                NbtSquisher.squishVanilla(Snapshot.writeToNBT(snapshot), fos);
            } catch (IOException io) {
                IOException ex = new IOException("Failed to write the snapshot file" + snapshotFile, io);
                ex.printStackTrace();
            }
        }
    }

    private void readSnapshots() {
        File[] files = snapshotsFile.listFiles();
        if (files != null) {
            for (File snapshotFile : files) {
                try (FileInputStream fis = new FileInputStream(snapshotFile)) {
                    NBTTagCompound nbt;
                    if (snapshotFile.getName().endsWith(SNAPSHOT_FILE_EXTENSION)) {
                        nbt = NbtSquisher.expand(fis);
                    } else {
                        // 7.99.4 + back compat
                        nbt = CompressedStreamTools.read(new DataInputStream(fis));
                    }
                    snapshots.add(Snapshot.readFromNBT(nbt));
                } catch (IOException e) {
                    IOException ex = new IOException("Failed to read the snapshot " + snapshotFile, e);
                    ex.printStackTrace();
                }
            }
        }
    }

    public void markDirty() {
        writeSnapshots();
    }

    public Snapshot getSnapshotByHeader(Snapshot.Header header) {
        return snapshots.stream().filter(snapshot -> snapshot.header.equals(header)).findFirst().orElse(null);
    }
}
