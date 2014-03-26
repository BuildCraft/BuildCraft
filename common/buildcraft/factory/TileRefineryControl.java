package buildcraft.factory;


import buildcraft.BuildCraftEnergy;
import buildcraft.api.fuels.IronEngineFuel;
import buildcraft.api.gates.IAction;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;
import buildcraft.core.IMachine;
import buildcraft.core.fluids.SingleUseTank;
import buildcraft.core.fluids.TankManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class TileRefineryControl extends TileEntity implements IFluidHandler, IPowerReceptor, IInventory, IMachine{
	
	private PowerHandler powerHandler;
	public static int MAX_LIQUID = FluidContainerRegistry.BUCKET_VOLUME * 10;
	public SingleUseTank input = new SingleUseTank("input", MAX_LIQUID, this);
	public SingleUseTank output = new SingleUseTank("output", MAX_LIQUID, this);
	private TankManager tankManager = new TankManager();
	public double clientRequiredEnergy = 0;
	private double energy = 0;
	private int tick = 0;
	private int recentEnergyAverage;
	private double[] recentEnergy = new double[20];
	
	public TileRefineryControl() {
		powerHandler = new PowerHandler(this, Type.MACHINE);
		initPowerProvider();
		tankManager.add(input);
		tankManager.add(output);
	}

	private void initPowerProvider() {
		powerHandler.configure(50, 150, 25, 1000);
		powerHandler.configurePowerPerdition(1, 1);
	}
	
	@Override
	public void updateEntity() {
		tick++;
		tick = tick % recentEnergy.length;
		recentEnergy[tick] = 0.0f;
	}
	public int getRecentEnergyAverage() {
		return recentEnergyAverage;
	}
	public int AmountOfOil(){
		//System.out.println(input.getFluidAmount());
		//return input.getFluidAmount();
		return 10000 	;
	}
	
	public int AmountOfFuel(){
		return output.getFluidAmount();
	}
	
	public int getScaledInput(int i) {
		return this.input.getFluid() != null ? (int) (((float) this.input.getFluid().amount / (float) (MAX_LIQUID)) * i) : 0;
	}
	public int getScaledOutput(int i) {
		return output.getFluid() != null ? (int) (((float) this.output.getFluid().amount / (float) (MAX_LIQUID)) * i) : 0;
	}
	
	public FluidStack getInput(){
		//return input.getFluid();
		return new FluidStack(BuildCraftEnergy.fluidOil, AmountOfOil());
	}
	
	public FluidStack getOutput(){
		return output.getFluid();
	}

	public double getEnergy() {
		return energy;
	}

	public void setEnergy(double energy) {
		this.energy = energy;
	}

	public void addEnergy(double energy) {
		this.energy += energy;
	}

	public void subtractEnergy(double energy) {
		this.energy -= energy;
	}


	public int getSizeInventory() {
		return 0;
	}

	@Override
	public boolean isActive() {
		return false;
	}

	@Override
	public boolean manageFluids() {
		return false;
	}

	@Override
	public boolean manageSolids() {
		return false;
	}

	@Override
	public boolean allowAction(IAction action) {
		return false;
	}

	@Override
	public ItemStack getStackInSlot(int var1) {
		return null;
	}

	@Override
	public ItemStack decrStackSize(int var1, int var2) {
		return null;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int var1) {
		return null;
	}

	@Override
	public void setInventorySlotContents(int var1, ItemStack var2) {}

	@Override
	public String getInventoryName() {
		return null;
	}

	@Override
	public boolean hasCustomInventoryName() {
		return false;
	}

	@Override
	public int getInventoryStackLimit() {
		return 0;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer var1) {
		return false;
	}

	@Override
	public void openInventory() {}

	@Override
	public void closeInventory() {}

	@Override
	public boolean isItemValidForSlot(int var1, ItemStack var2) {
		return false;
	}

	@Override
	public PowerReceiver getPowerReceiver(ForgeDirection side) {
		return powerHandler.getPowerReceiver();
	}

	@Override
	public void doWork(PowerHandler workProvider) {}

	@Override
	public World getWorld() {
		return null;
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		if (resource.getFluid() == BuildCraftEnergy.fluidOil)
			return input.fill(resource, doFill);

		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		if (fluid == BuildCraftEnergy.fluidOil){
			return true;
		}
		return false;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection direction) {
		return tankManager.getTankInfo(direction);
	}
	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		tankManager.readFromNBT(data);

	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		tankManager.writeToNBT(data);

	}
	
}
