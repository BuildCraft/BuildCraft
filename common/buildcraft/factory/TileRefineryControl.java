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
				if (world.getBlock(x - 4, y + 1, z+1) == BuildCraftFactory.blockRefineryValve && world.getBlock(x+4, y+1, z+1) == BuildCraftFactory.blockRefineryValve){
					input = (TileRefineryValve) world.getTileEntity(x+4, y+1, z+1);
					input.markAsInput();
					output = (TileRefineryValve) world.getTileEntity(x-4, y+1, z+1);
					output.markAsOutput();
					valvesAssinged = true;
					}
				if (world.getBlock(x - 4, y + 1, z-1) == BuildCraftFactory.blockRefineryValve && world.getBlock(x+4, y+1, z-1) == BuildCraftFactory.blockRefineryValve){
					input = (TileRefineryValve) world.getTileEntity(x+4, y+1, z-1);
					input.markAsInput();
					output = (TileRefineryValve) world.getTileEntity(x-4, y+1, z-1);
					output.markAsOutput();
					valvesAssinged = true;
					}
				if (world.getBlock(x+1, y+1, z-4) == BuildCraftFactory.blockRefineryValve && world.getBlock (x+1, y+1, z+4) == BuildCraftFactory.blockRefineryValve){
					input = (TileRefineryValve) world.getTileEntity(x+1, y+1, z+4);
					input.markAsInput();
					output = (TileRefineryValve) world.getTileEntity(x+1, y+1, z-4);
					output.markAsOutput();
					valvesAssinged = true;
					}
				if (world.getBlock(x-1, y+1, z-4) == BuildCraftFactory.blockRefineryValve && world.getBlock (x-1, y+1, z+4) == BuildCraftFactory.blockRefineryValve){
					input = (TileRefineryValve) world.getTileEntity(x-1, y+1, z+4);
					input.markAsInput();
					output = (TileRefineryValve) world.getTileEntity(x-1, y+1, z-4);
					output.markAsOutput();
					valvesAssinged = true;
					}
				}
			if (input.getAmountOfLiquid() >= 1 && output.getAmountOfLiquid() <= 9999 && energy >=25 ){
				active = true;
				input.tank.drain(1, true);
				output.tank.fill(new FluidStack (BuildCraftEnergy.fluidFuel, 1), true);
				energy = energy-10;
				sendNetworkUpdate();
				input.sendNetworkUpdate();
				output.sendNetworkUpdate();
			} else {
				active = false;
			}
			} else {
				if (valvesAssinged){
					input.markNeutral();
					output.markNeutral();
					}
				}
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
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		data.setDouble("energy", energy);
	}

	@Override
	public void markDirty() {

	}	
}
