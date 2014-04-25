package buildcraft.factory;

import buildcraft.api.transport.IPipeConnection;
import buildcraft.api.transport.IPipeTile;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class TileMultiblockValve extends TileMultiblockSlave implements IFluidHandler, IPipeConnection {

	private ForgeDirection orientation = ForgeDirection.UNKNOWN;

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		orientation = ForgeDirection.getOrientation(nbt.getByte("orientation"));
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setByte("orientation", (byte) orientation.ordinal());
	}

	private IFluidHandler getMaster() {
		if (!formed || masterPosition == null) {
			return null;
		}

		TileEntity tile = worldObj.getTileEntity((int) masterPosition.x, (int) masterPosition.y, (int) masterPosition.z);
		return tile != null && tile instanceof IFluidHandler ? (IFluidHandler) tile : null;
	}

	/* IFLUIDHANDLER */
	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		return getMaster() != null ? getMaster().fill(from, resource, doFill) : 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
		return getMaster() != null ? getMaster().drain(from, resource, doDrain) : null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		return getMaster() != null ? getMaster().drain(from, maxDrain, doDrain) : null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		return getMaster() != null ? getMaster().canFill(from, fluid) : false;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		return getMaster() != null ? getMaster().canDrain(from, fluid) : false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		return getMaster() != null ? getMaster().getTankInfo(from) : new FluidTankInfo[0];
	}

	/* IPIPECONNECTION */
	@Override
	public ConnectOverride overridePipeConnection(IPipeTile.PipeType type, ForgeDirection with) {
		return formed ? ConnectOverride.CONNECT : ConnectOverride.DISCONNECT;
	}

}
