package buildcraft.builders.snapshot;

import java.io.File;
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

public class GlobalSavedDataSnapshots {
    public static final Map<Side, GlobalSavedDataSnapshots> instances = new EnumMap<>(Side.class);
    public final List<Snapshot> snapshots = new ArrayList<>();
    private final File snapshotsFile;

    private GlobalSavedDataSnapshots(Side side) {
        try {
            snapshotsFile = new File(FMLCommonHandler.instance().getSavesDirectory().getParentFile(), "snapshots-" + side.name().toLowerCase(Locale.ROOT));
            if (!snapshotsFile.exists()) {
                if (!snapshotsFile.mkdirs()) {
                    throw new IOException();
                }
            } else if (!snapshotsFile.isDirectory()) {
                throw new IllegalArgumentException();
            }
            readSnapshots();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static GlobalSavedDataSnapshots get(Side side) {
        if (!instances.containsKey(side)) {
            instances.put(side, new GlobalSavedDataSnapshots(side));
        }
        return instances.get(side);
    }

    public static GlobalSavedDataSnapshots get(World world) {
        return get(world.isRemote ? Side.CLIENT : Side.SERVER);
    }

    private void writeSnapshots() throws IOException {
        for (Snapshot snapshot : snapshots) {
            File snapshotFile = new File(snapshotsFile, snapshot.header.getFileName());
            if (!snapshotFile.exists()) {
                CompressedStreamTools.write(Snapshot.writeToNBT(snapshot), snapshotFile);
            }
        }
    }

    private void readSnapshots() throws IOException {
        // noinspection ConstantConditions
        for (File snapshotFile : snapshotsFile.listFiles()) {
            NBTTagCompound nbt = CompressedStreamTools.read(snapshotFile);
            snapshots.add(Snapshot.readFromNBT(nbt));
        }
    }

    public void markDirty() {
        try {
            writeSnapshots();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Snapshot getSnapshotByHeader(Snapshot.Header header) {
        return snapshots.stream().filter(snapshot -> snapshot.header.equals(header)).findFirst().orElse(null);
    }
}
