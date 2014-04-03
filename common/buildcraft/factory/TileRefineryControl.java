package buildcraft.factory;


import io.netty.buffer.ByteBuf;

import java.io.IOException;

import buildcraft.BuildCraftEnergy;
import buildcraft.api.fuels.IronEngineFuel;
import buildcraft.api.gates.IAction;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;
import buildcraft.core.IMachine;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.fluids.SingleUseTank;
import buildcraft.core.fluids.TankManager;
import buildcraft.core.network.NetworkData;
import buildcraft.core.network.PacketPayload;
import buildcraft.core.network.PacketUpdate;
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

public class TileRefineryControl extends TileBuildCraft implements IFluidHandler, IInventory {
	
	public static int MAX_LIQUID = FluidContainerRegistry.BUCKET_VOLUME * 10;
	public SingleUseTank inputTank = new SingleUseTank("inputTank", MAX_LIQUID, this);
	public SingleUseTank outputTank = new SingleUseTank("outputTank", MAX_LIQUID, this);
	private TankManager tankManager = new TankManager();
	@MjBattery (maxReceivedPerCycle = 25, maxCapacity = 1000)
	public double energy;
	
	
	
	public TileRefineryControl() {
		tankManager.add(inputTank);
		tankManager.add(outputTank);
	}
	
	@Override
	public void updateEntity() {
			if (AmountOfOil() > 1 && AmountOfFuel() <= 9999 && energy >=25){
				inputTank.drain(1, true);
				outputTank.fill(new FluidStack (BuildCraftEnergy.fluidFuel, 1), true);
				energy = energy-10;
				sendNetworkUpdate();
		}
	}
	
	public int AmountOfOil(){
		if (inputTank.isEmpty()){
			return 0;
		}
		return inputTank.getFluid().amount;
	}
	
	public int AmountOfFuel(){
		if (outputTank.isEmpty()){
			return 0;
		}
		return outputTank.getFluidAmount();
	}
	
	public int getScaledInput(int i) {
		return this.inputTank.getFluid() != null ? (int) (((float) this.inputTank.getFluid().amount / (float) (MAX_LIQUID)) * i) : 0;
	}
	public int getScaledOutput(int i) {
		return outputTank.getFluid() != null ? (int) (((float) this.outputTank.getFluid().amount / (float) (MAX_LIQUID)) * i) : 0;
	}
	
	public FluidStack getInput(){
		return inputTank.getFluid();
	}
	
	public FluidStack getOutput(){
		return outputTank.getFluid();
	}

	public double getEnergy() {
		return energy;
	}

	public int getSizeInventory() {
		return 0;
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
	public World getWorld() {
		return null;
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		sendNetworkUpdate();
		int fluid = 0;
		if (resource.getFluid() == BuildCraftEnergy.fluidOil){
			fluid = inputTank.fill(resource, doFill);
			sendNetworkUpdate();
		}
		return fluid;
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
		energy = data.getDouble("energy");

	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		tankManager.writeToNBT(data);
		data.setDouble("energy", energy);
	}

	@Override
	public void markDirty() {

	}

	@Override
	public PacketPayload getPacketPayload() {
		PacketPayload payload = new PacketPayload(new PacketPayload.StreamWriter() {
			@Override
			public void writeData(ByteBuf data) {
				tankManager.writeData(data);
			}
		});
		return payload;
	}

	@Override
	public void handleUpdatePacket(PacketUpdate packet) throws IOException {
		ByteBuf stream = packet.payload.stream;
		tankManager.readData(stream);
	}
	
}
