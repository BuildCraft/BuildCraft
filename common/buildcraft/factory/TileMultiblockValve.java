package buildcraft.factory;

import buildcraft.api.transport.IPipeConnection;
import buildcraft.api.transport.IPipeTile;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class TileMultiblockValve extends TileMultiblockSlave implements IFluidHandler, IPipeConnection {

	private IFluidHandler getFluidHandler() {
		if (!formed || masterPosition == null) {
			return null;
		}

		TileEntity tile = getMaster();
		return tile != null && tile instanceof IFluidHandler ? (IFluidHandler) tile : null;
	}

	/* IFLUIDHANDLER */
	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		return getFluidHandler() != null ? getFluidHandler().fill(from, resource, doFill) : 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
		return getFluidHandler() != null ? getFluidHandler().drain(from, resource, doDrain) : null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		return getFluidHandler() != null ? getFluidHandler().drain(from, maxDrain, doDrain) : null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		return getFluidHandler() != null ? getFluidHandler().canFill(from, fluid) : false;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		return getFluidHandler() != null ? getFluidHandler().canDrain(from, fluid) : false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		return getFluidHandler() != null ? getFluidHandler().getTankInfo(from) : new FluidTankInfo[0];
	}

	/* IPIPECONNECTION */
	@Override
	public ConnectOverride overridePipeConnection(IPipeTile.PipeType type, ForgeDirection with) {
		return formed ? ConnectOverride.CONNECT : ConnectOverride.DISCONNECT;
	}

}
