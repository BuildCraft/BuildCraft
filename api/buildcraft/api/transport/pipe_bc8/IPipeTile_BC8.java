package buildcraft.api.transport.pipe_bc8;

import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public interface IPipeTile_BC8 {
    BlockPos getPos();

    World getWorld();

    IPipe_BC8 getPipe();
}
