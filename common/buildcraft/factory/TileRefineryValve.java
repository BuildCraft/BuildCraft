package buildcraft.factory;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import buildcraft.BuildCraftEnergy;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.fluids.SingleUseTank;
import buildcraft.core.fluids.TankManager;

public class TileRefineryValve extends TileBuildCraft implements IFluidHandler{
	
	public int type = 0;
	public static int MAX_LIQUID = FluidContainerRegistry.BUCKET_VOLUME * 10;
	public SingleUseTank tank = new SingleUseTank("tank", MAX_LIQUID, this);
	private TankManager tankManager = new TankManager();
	
	public TileRefineryValve(){
		tankManager.add(tank);
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		if (type != 1){
			return 0;
		}
		return tank.fill(resource, doFill);
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource,
			boolean doDrain) {
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		if (type != 2){
			return null;
		}
		return tank.drain(maxDrain, doDrain);
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		if (type == 1 && fluid == BuildCraftEnergy.fluidOil){
			return true;
		}
		return false;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		if (type == 2 ){
			return true;
		}
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		return tankManager.getTankInfo(from);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		tankManager.readFromNBT(data);
		type = data.getInteger("type");

	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		tankManager.writeToNBT(data);
		data.setInteger("type", type);
	}

}
