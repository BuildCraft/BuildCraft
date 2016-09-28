package buildcraft.transport.pipe.flow;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import buildcraft.transport.api_move.IPipe;
import buildcraft.transport.api_move.PipeFlow;

public class PipeFlowFluids extends PipeFlow {
    public PipeFlowFluids(IPipe pipe) {
        super(pipe);
    }

    public PipeFlowFluids(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
    }

    @Override
    public boolean canConnect(EnumFacing face, PipeFlow other) {
        return other instanceof PipeFlowFluids;
    }

    @Override
    public boolean canConnect(EnumFacing face, TileEntity oTile) {
        return oTile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, face.getOpposite());
    }
}
