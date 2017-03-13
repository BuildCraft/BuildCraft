package buildcraft.builders.snapshot;

import buildcraft.api.mj.MjBattery;
import buildcraft.api.permission.IPlayerOwned;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ITileForSnapshotBuilder extends IPlayerOwned {
    World getWorld();

    MjBattery getBattery();

    BlockPos getBuilderPos();
}
