package buildcraft.transport.pipe.flow;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import buildcraft.api.core.IFluidFilter;
import buildcraft.api.transport.neptune.IFlowFluid;
import buildcraft.api.transport.neptune.IPipe;
import buildcraft.api.transport.neptune.PipeFlow;

public class PipeFlowFluids extends PipeFlow implements IFlowFluid {
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

    // IFlowFluid

    @Override
    public int tryExtractFluid(int millibuckets, EnumFacing from, IFluidFilter filter) {
        return 0;
    }
}
