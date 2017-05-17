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
        snapshotsFile = new File(FMLCommonHandler.instance().getSavesDirectory().getParentFile(), "snapshots-" + side.name().toLowerCase(Locale.ROOT));
        if (!snapshotsFile.exists()) {
            if (!snapshotsFile.mkdirs()) {
                throw new RuntimeException("Failed to make the directories required for snapshots! \n\tdir = " + snapshotsFile);
            }
        } else if (!snapshotsFile.isDirectory()) {
            throw new IllegalStateException("The snapshots directory was a file! We can't use this!\n\tfile = " + snapshotsFile);
        }
        readSnapshots();
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

    private void writeSnapshots() {
        for (Snapshot snapshot : snapshots) {
            File snapshotFile = new File(snapshotsFile, snapshot.header.getFileName());
            if (!snapshotFile.exists()) {
                try {
                    CompressedStreamTools.write(Snapshot.writeToNBT(snapshot), snapshotFile);
                } catch (IOException io) {
                    IOException ex = new IOException("Failed to write the snapshot file" + snapshotFile, io);
                    ex.printStackTrace();
                }
            }
        }
    }

    private void readSnapshots() {
        for (File snapshotFile : snapshotsFile.listFiles()) {
            try {
                NBTTagCompound nbt = CompressedStreamTools.read(snapshotFile);
                snapshots.add(Snapshot.readFromNBT(nbt));
            } catch (IOException io) {
                IOException ex = new IOException("Failed to read the snapshot file" + snapshotFile, io);
                ex.printStackTrace();
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
