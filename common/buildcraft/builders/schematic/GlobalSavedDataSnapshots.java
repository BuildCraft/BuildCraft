package buildcraft.builders.schematic;

import buildcraft.lib.misc.NBTUtilBC;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class GlobalSavedDataSnapshots {
    public static final Map<Side, GlobalSavedDataSnapshots> instances = new EnumMap<>(Side.class);
    public final List<Snapshot> snapshots = new ArrayList<>();
    private final File snapshotsFile;

    private GlobalSavedDataSnapshots(Side side) {
        try {
            snapshotsFile = new File(
                    FMLCommonHandler.instance().getSavesDirectory().getParentFile(),
                    "snapshots-" + side.name().toLowerCase(Locale.ROOT)
            );
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

    private void writeSnapshots() throws IOException {
        for (Snapshot snapshot : snapshots) {
            File snapshotFile = new File(snapshotsFile, snapshot.header.getFileName());
            NBTTagCompound nbt = snapshot.serializeNBT();
            nbt.setTag("type", NBTUtilBC.writeEnum(snapshot.getType()));
            CompressedStreamTools.write(nbt, snapshotFile);
        }
    }

    private void readSnapshots() throws IOException {
        // noinspection ConstantConditions
        for (File snapshotFile : snapshotsFile.listFiles()) {
            NBTTagCompound nbt = CompressedStreamTools.read(snapshotFile);
            Snapshot snapshot = NBTUtilBC.readEnum(nbt.getTag("type"), Snapshot.EnumSnapshotType.class).create.get();
            snapshot.deserializeNBT(nbt);
            snapshots.add(snapshot);
        }
    }

    public void markDirty() {
        try {
            writeSnapshots();
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
}
