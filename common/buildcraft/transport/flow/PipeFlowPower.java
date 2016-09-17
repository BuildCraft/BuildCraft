package buildcraft.transport.flow;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import buildcraft.api.mj.MjAPI;
import buildcraft.transport.api_move.IPipe;
import buildcraft.transport.api_move.PipeFlow;

public class PipeFlowPower extends PipeFlow {
    public PipeFlowPower(IPipe pipe) {
        super(pipe);
    }

    public PipeFlowPower(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
    }

    @Override
    public boolean canConnect(EnumFacing face, PipeFlow other) {
        return other instanceof PipeFlowPower;
    }

    @Override
    public boolean canConnect(EnumFacing face, TileEntity oTile) {
        return oTile.hasCapability(MjAPI.CAP_RECEIVER, face.getOpposite());
    }
}
