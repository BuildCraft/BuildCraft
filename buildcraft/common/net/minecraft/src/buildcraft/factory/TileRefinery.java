package net.minecraft.src.buildcraft.factory;

import java.util.LinkedList;

import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.buildcraft.api.IPowerReceptor;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.PowerProvider;
import net.minecraft.src.buildcraft.api.SafeTimeTracker;
import net.minecraft.src.buildcraft.core.ILiquidContainer;
import net.minecraft.src.buildcraft.core.IMachine;
import net.minecraft.src.buildcraft.core.TileBuildCraft;

public class TileRefinery extends TileBuildCraft implements ILiquidContainer,
		IPowerReceptor, IInventory, IMachine {	

	public static LinkedList <RefineryRecipe> recipes = new LinkedList <RefineryRecipe> ();
	
	public static int LIQUID_PER_SLOT = BuildCraftCore.BUCKET_VOLUME * 4;	
	
	public static class Slot {
		int liquidId = 0;
		int quantity = 0;
		
		public int fill(Orientations from, int amount, int id, boolean doFill) {
			if (quantity != 0 && liquidId != id) {
				return 0;
			} else if (quantity + amount <= LIQUID_PER_SLOT) {
				if (doFill) {
					quantity = quantity + amount;
				}
				
				liquidId = id;
				return amount;
			} else {
				int used = LIQUID_PER_SLOT - quantity;
				
				if (doFill) {
					quantity = LIQUID_PER_SLOT;
				}
				
				liquidId = id;
				return used;				
			}			
		}				
		
		public void writeFromNBT(NBTTagCompound nbttagcompound) {
			nbttagcompound.setInteger("liquidId", liquidId);
			nbttagcompound.setInteger("quantity", quantity);
		}
		
		public void readFromNBT(NBTTagCompound nbttagcompound) {
			liquidId = nbttagcompound.getInteger("liquidId");
			quantity = nbttagcompound.getInteger("quantity");			
		}
	}
	
	public Slot slot1 = new Slot ();
	public Slot slot2 = new Slot ();
	public Slot result = new Slot ();
	
	SafeTimeTracker time = new SafeTimeTracker();
	
	PowerProvider powerProvider;

	private int animationStage = 0;
	
	public TileRefinery () {
		powerProvider = BuildCraftCore.powerFramework.createPowerProvider();
		powerProvider.configure(20, 25, 25, 25, 1000);
	}
	
	@Override
	public int fill(Orientations from, int quantity, int id, boolean doFill) {
		int used = slot1.fill(from, quantity, id, doFill);
		used += slot2.fill(from, quantity - used, id, doFill);
				
		return used;
	}

	@Override
	public int empty(int quantityMax, boolean doEmpty) {
		int res = 0;
		
		if (result.quantity >= quantityMax) {
			res = quantityMax;
			
			if (doEmpty) {
				result.quantity -= quantityMax;
			}
		} else {
			res = result.quantity;
			
			if (doEmpty) {
				result.quantity = 0;
			}
		}
				
		return res;
	}

	@Override
	public int getLiquidQuantity() {
		return result.quantity;
	}

	@Override
	public int getCapacity() {
		return BuildCraftCore.BUCKET_VOLUME * 3;
	}

	@Override
	public int getLiquidId() {
		return result.liquidId;
	}

	@Override
	public int getSizeInventory() {
		return 0;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return null;
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		return null;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		
	}

	@Override
	public String getInvName() {
		return null;
	}

	@Override
	public int getInventoryStackLimit() {
		return 0;
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
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
		
	}
	
	@Override
	public void updateEntity () {
		RefineryRecipe currentRecipe = null;
		Slot src1 = null, src2 = null;
		
		for (RefineryRecipe r : recipes) {					
			if (r.sourceId1 == this.slot1.liquidId && this.slot1.quantity >= r.sourceQty1) {
				src1 = slot1;
				src2 = slot2;
			} else if (r.sourceId1 == this.slot2.liquidId && this.slot2.quantity >= r.sourceQty1) {
				src1 = slot2;
				src2 = slot1;				
			}
			
			if (src1 == null) {
				continue;
			}
			
			if (r.sourceQty2 > 0) {
				if (r.sourceId2 != src2.liquidId || src2.quantity < r.sourceQty2) {
					continue;
				}	
			} else {
				src2 = null;
			}
			
			currentRecipe = r;
			break;
		}
		
		if (currentRecipe == null) {
			decreaseAnimation();
			return;
		}
		
		if (result.quantity != 0 && result.liquidId != currentRecipe.resultId) {
			decreaseAnimation();
			return;
		}
		
		if (result.quantity + currentRecipe.resultQty > LIQUID_PER_SLOT) {
			decreaseAnimation();
			return;
		}
		
		if (powerProvider.energyStored >= currentRecipe.energy) {
			increaseAnimation();
		} else {
			decreaseAnimation();
		}
		
		if (!time.markTimeIfDelay(worldObj, currentRecipe.delay)) {
			return;
		}
		
		int energyUsed = powerProvider.useEnergy(currentRecipe.energy,
				currentRecipe.energy, true);
		
		if (energyUsed != 0) {
			result.liquidId = currentRecipe.resultId;
			result.quantity += currentRecipe.resultQty;
			src1.quantity -= currentRecipe.sourceQty1;
			
			if (src2 != null) {
				src2.quantity -= currentRecipe.sourceQty2;	
			}
		}
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
	
	public static void addRecipe (RefineryRecipe r) {
		recipes.add(r);
	}
	
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		
		if (nbttagcompound.hasKey("slot1")) {
			slot1.readFromNBT(nbttagcompound.getCompoundTag("slot1"));
			slot2.readFromNBT(nbttagcompound.getCompoundTag("slot2"));
			result.readFromNBT(nbttagcompound.getCompoundTag("result"));
		}
				
		animationStage = nbttagcompound.getInteger("animationStage");	
		animationSpeed = nbttagcompound.getFloat("animationSpeed");
		
		BuildCraftCore.powerFramework.loadPowerProvider(this, nbttagcompound);
		powerProvider.configure(20, 25, 25, 25, 1000);
    }

	@Override
    public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		
		NBTTagCompound NBTslot1 = new NBTTagCompound();
		NBTTagCompound NBTslot2 = new NBTTagCompound();
		NBTTagCompound NBTresult = new NBTTagCompound();
		
		slot1.writeFromNBT(NBTslot1);
		slot2.writeFromNBT(NBTslot2);
		result.writeFromNBT(NBTresult);
		
		nbttagcompound.setTag("slot1", NBTslot1);
		nbttagcompound.setTag("slot2", NBTslot2);
		nbttagcompound.setTag("result", NBTresult);
		
		nbttagcompound.setInteger("animationStage", animationStage);
		nbttagcompound.setFloat("animationSpeed", animationSpeed);
		BuildCraftCore.powerFramework.savePowerProvider(this, nbttagcompound);
	}
	
	public int getAnimationStage () {
		return animationStage ;
	}
	
	public float animationSpeed = 1;
	
	public void increaseAnimation () {
		if (animationSpeed < 2) {
			animationSpeed = 2;
		} else if (animationSpeed <= 5) {
			animationSpeed += 0.1;
		}
		
		animationStage += animationSpeed;
		
		if (animationStage > 300) {
			animationStage = 100;
		}
	}
	
	public void decreaseAnimation () {
		if (animationSpeed >= 1) {
			animationSpeed -= 0.1;
			
			animationStage += animationSpeed;
			
			if (animationStage > 300) {
				animationStage = 100;
			}
		} else {
			if (animationStage > 0) {
				animationStage--;
			}
		}
	}

	@Override
	public void openChest() {
		
	}

	@Override
	public void closeChest() {
		
	}
}
