package buildcraft.factory;


import io.netty.buffer.ByteBuf;

import java.io.IOException;

import buildcraft.BuildCraftEnergy;
import buildcraft.BuildCraftFactory;
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
import buildcraft.core.utils.MultiBlockCheck;
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

public class TileRefineryControl extends TileBuildCraft implements IInventory {

	@MjBattery (maxReceivedPerCycle = 25, maxCapacity = 1000)
	public double energy;
	public TileRefineryValve input, output;
	public boolean valvesAssinged = false;
	public boolean active = false;
	@NetworkData
	public double temperature = 20;
	
	
	public TileRefineryControl() {
	}
	
	@Override
	public void updateEntity() {
		if (MultiBlockCheck.isPartOfAMultiBlock("refinery", this.xCoord, this.yCoord, this.zCoord, this.getWorldObj())){
			if (!valvesAssinged){
				World world = this.getWorldObj();
				int x = this.xCoord;
				int y = this.yCoord;
				int z = this.zCoord;
				if (world.getTileEntity(x+1, y, z) instanceof TileRefineryValve && world.getTileEntity(x+1, y+5, z) instanceof TileRefineryValve){
					input =  (TileRefineryValve) world.getTileEntity(x+1, y, z);
					output = (TileRefineryValve) world.getTileEntity(x+1, y+5, z);
					}
				if (world.getTileEntity(x-1, y, z) instanceof TileRefineryValve && world.getTileEntity(x-1, y+5, z) instanceof TileRefineryValve){
					input = (TileRefineryValve) world.getTileEntity(x-1, y, z);
					output = (TileRefineryValve) world.getTileEntity(x-1, y+5, z);
					}
				if (world.getTileEntity(x, y, z+1) instanceof TileRefineryValve && world.getTileEntity (x, y+5, z+1) instanceof TileRefineryValve){
					input = (TileRefineryValve) world.getTileEntity(x, y, z+1);
					output = (TileRefineryValve) world.getTileEntity(x, y+5, z+1);
					}
				if (world.getTileEntity(x, y, z-1) instanceof TileRefineryValve && world.getTileEntity(x, y+5, z-1) instanceof TileRefineryValve){
					input = (TileRefineryValve) world.getTileEntity(x, y, z-1);
					output = (TileRefineryValve) world.getTileEntity(x, y+5, z-1);
				}
				input.markAsInput();
				output.markAsOutput();
				valvesAssinged = true;
				}
			if (energy>=10){
				energy = energy-10;
				active = true;
				increaseTemperature(0.1);
				if (input.getAmountOfLiquid() >= 1 && output.getAmountOfLiquid() <= 9999 && temperature>=370){
					input.tank.drain(1, true);
					output.tank.fill(new FluidStack (BuildCraftEnergy.fluidFuel, 1), true);
					input.sendNetworkUpdate();
					output.sendNetworkUpdate();
					}
				} else {
					active = false;
					decreaseTemperature(0.1);
					}
			} else {
				if (valvesAssinged){
					input.markNeutral();
					output.markNeutral();
					valvesAssinged = false;
					}
				}
		}
	public void increaseTemperature(double amount){
		if (temperature<400){
			temperature = temperature+amount;
			sendNetworkUpdate();
		}
	}
	
	public void decreaseTemperature(double amount){
		if (temperature>20){
			temperature = temperature - amount;
			sendNetworkUpdate();
		}
	}
	
	public int getTemprature(){
		return (int) Math.round(temperature);
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
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		energy = data.getDouble("energy");
		temperature = data.getDouble("temprature");
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		data.setDouble("energy", energy);
		data.setDouble("temprature", temperature);
	}

	@Override
	public void markDirty() {

	}	
	}
