package buildcraft.transport.flow;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import buildcraft.transport.api_move.IPipe;
import buildcraft.transport.api_move.PipeFlow;

public class PipeFlowStructure extends PipeFlow {
    public PipeFlowStructure(IPipe pipe) {
        super(pipe);
    }

    public PipeFlowStructure(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
    }

    @Override
    public boolean canConnect(EnumFacing face, PipeFlow other) {
        return other instanceof PipeFlowStructure;
    }

    @Override
    public boolean canConnect(EnumFacing face, TileEntity oTile) {
        return false;
    }
}
