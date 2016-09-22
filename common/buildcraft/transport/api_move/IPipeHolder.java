package buildcraft.transport.api_move;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public interface IPipeHolder {
    World getPipeWorld();

    IPipe getPipe();

    PipePluggable getPluggable(EnumFacing side);

    TileEntity getNeighbouringTile(EnumFacing side);

    IPipe getNeighbouringPipe(EnumFacing side);

    void scheduleRenderUpdate();

    void scheduleNetworkUpdate();
}
