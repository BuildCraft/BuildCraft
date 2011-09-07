package net.minecraft.src.buildcraft.factory;

import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.BuildCraftEnergy;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.buildcraft.api.IPowerReceptor;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.PowerProvider;
import net.minecraft.src.buildcraft.core.ILiquidContainer;
import net.minecraft.src.buildcraft.core.IMachine;
import net.minecraft.src.buildcraft.core.TileBuildCraft;

public class TileRefinery extends TileBuildCraft implements ILiquidContainer,
		IPowerReceptor, IInventory, IMachine {	

	public static int LIQUID_PER_SLOT = BuildCraftCore.BUCKET_VOLUME * 4;	
	
	public static class Slot {
		int liquidId = 0;
		int quantity = 0;
		
		public int fill(Orientations from, int amount, int id) {
			if (quantity != 0 && liquidId != id) {
				return 0;
			} else if (quantity + amount <= LIQUID_PER_SLOT) {
				quantity = quantity + amount;
				liquidId = id;
				return amount;
			} else {
				int used = LIQUID_PER_SLOT - quantity;
				quantity = LIQUID_PER_SLOT;
				liquidId = id;
				return used;				
			}			
		}
	}
	
	public Slot slot1 = new Slot ();
	public Slot slot2 = new Slot ();
	
	PowerProvider powerProvider;
	
	public TileRefinery () {
		powerProvider = BuildCraftCore.powerFramework.createPowerProvider();
		powerProvider.configure(20, 25, 25, 25, 1000);
	}
	
	@Override
	public int fill(Orientations from, int quantity, int id) {
		int used = slot1.fill(from, quantity, id);
		used += slot2.fill(from, quantity - used, id);
				
		return used;
	}

	@Override
	public int empty(int quantityMax, boolean doEmpty) {
		return quantityMax;
	}

	@Override
	public int getLiquidQuantity() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getCapacity() {
		return BuildCraftCore.BUCKET_VOLUME * 3;
	}

	@Override
	public int getLiquidId() {
		return BuildCraftEnergy.fuel.shiftedIndex;
	}

	@Override
	public int getSizeInventory() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getInvName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getInventoryStackLimit() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setPowerProvider(PowerProvider provider) {
		powerProvider = provider;		
	}

	@Override
	public PowerProvider getPowerProvider() {
		return powerProvider;
	}

	@Override
	public void doWork() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isActive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean manageLiquids() {
		return true;
	}

	@Override
	public boolean manageSolids() {
		return true;
	}

}
