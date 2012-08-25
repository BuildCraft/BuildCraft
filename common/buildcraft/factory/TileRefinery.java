/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.factory;

import buildcraft.BuildCraftCore;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.core.Orientations;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.liquids.ILiquidTank;
import buildcraft.api.liquids.ITankContainer;
import buildcraft.api.liquids.LiquidStack;
import buildcraft.api.liquids.LiquidTank;
import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerFramework;
import buildcraft.api.recipes.RefineryRecipe;
import buildcraft.core.IMachine;
import buildcraft.core.ProxyCore;
import buildcraft.core.network.TileNetworkData;
import net.minecraft.src.Container;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ICrafting;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;

public class TileRefinery extends TileMachine implements ITankContainer, IPowerReceptor, IInventory, IMachine {

	private int[] filters = new int[2];

	public static int LIQUID_PER_SLOT = BuildCraftAPI.BUCKET_VOLUME * 4;

	public static class Slot {

		@TileNetworkData
		public int liquidId = 0;
		@TileNetworkData
		public int quantity = 0;

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

			if (liquidId != 0) {
				quantity = nbttagcompound.getInteger("quantity");
			} else {
				quantity = 0;
			}
		}
	}

	@TileNetworkData
	public Slot slot1 = new Slot();
	@TileNetworkData
	public Slot slot2 = new Slot();
	@TileNetworkData
	public Slot result = new Slot();
	@TileNetworkData
	public float animationSpeed = 1;
	private int animationStage = 0;

	SafeTimeTracker time = new SafeTimeTracker();

	SafeTimeTracker updateNetworkTime = new SafeTimeTracker();

	IPowerProvider powerProvider;

	private boolean isActive;

	public TileRefinery() {
		powerProvider = PowerFramework.currentFramework.createPowerProvider();
		powerProvider.configure(20, 25, 25, 25, 1000);

		filters[0] = 0;
		filters[1] = 0;
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
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int var1) {
		return null;
	}

	@Override
	public void setPowerProvider(IPowerProvider provider) {
		powerProvider = provider;
	}

	@Override
	public IPowerProvider getPowerProvider() {
		return powerProvider;
	}

	@Override
	public void doWork() {

	}

	@Override
	public void updateEntity() {
		if (ProxyCore.proxy.isRemote(worldObj)) {
			simpleAnimationIterate();
		} else if (ProxyCore.proxy.isSimulating(worldObj) && updateNetworkTime.markTimeIfDelay(worldObj, 2 * BuildCraftCore.updateFactor)) {
			sendNetworkUpdate();
		}

		isActive = false;

		RefineryRecipe currentRecipe = null;

		currentRecipe = RefineryRecipe.findRefineryRecipe(new LiquidStack(slot1.liquidId, slot1.quantity, 0), new LiquidStack(slot2.liquidId, slot2.quantity, 0));

		if (currentRecipe == null) {
			decreaseAnimation();
			return;
		}

		if (result.quantity != 0 && result.liquidId != currentRecipe.result.itemID) {
			decreaseAnimation();
			return;
		}

		if (result.quantity + currentRecipe.result.amount > LIQUID_PER_SLOT) {
			decreaseAnimation();
			return;
		}

		isActive = true;

		if (powerProvider.getEnergyStored() >= currentRecipe.energy) {
			increaseAnimation();
		} else {
			decreaseAnimation();
			return;
		}

		if (!time.markTimeIfDelay(worldObj, currentRecipe.delay)) {
			return;
		}

		if (!containsInput(currentRecipe.ingredient1)
				|| !containsInput(currentRecipe.ingredient2)) {
			decreaseAnimation();
			return;
		}

		float energyUsed = powerProvider.useEnergy(currentRecipe.energy, currentRecipe.energy, true);

		if (energyUsed != 0) {
			if (consumeInput(currentRecipe.ingredient1)
					&& consumeInput(currentRecipe.ingredient2)) {
				result.liquidId = currentRecipe.result.itemID;
				result.quantity += currentRecipe.result.amount;
			}
		}
	}

	private boolean containsInput(LiquidStack liquid) {
		if(liquid == null)
			return true;
		
		return new LiquidStack(slot1.liquidId, slot1.quantity, 0).containsLiquid(liquid) || new LiquidStack(slot2.liquidId, slot2.quantity, 0).containsLiquid(liquid);
	}

	private boolean consumeInput(LiquidStack liquid) {
		if(liquid == null)
			return true;
		
		if(liquid.isLiquidEqual(new LiquidStack(slot1.liquidId, slot1.quantity, 0))) {
			slot1.quantity -= liquid.amount;
			return true;
		} else if(liquid.isLiquidEqual(new LiquidStack(slot2.liquidId, slot2.quantity, 0))) {
			slot2.quantity -= liquid.amount;
			return true;
		}
		
		return false;
	}

	@Override
	public boolean isActive() {
		return isActive;
	}

	@Override
	public boolean manageLiquids() {
		return true;
	}

	@Override
	public boolean manageSolids() {
		return true;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		if (nbttagcompound.hasKey("slot1")) {
			slot1.readFromNBT(nbttagcompound.getCompoundTag("slot1"));
			slot2.readFromNBT(nbttagcompound.getCompoundTag("slot2"));
			result.readFromNBT(nbttagcompound.getCompoundTag("result"));
		}

		animationStage = nbttagcompound.getInteger("animationStage");
		animationSpeed = nbttagcompound.getFloat("animationSpeed");

		PowerFramework.currentFramework.loadPowerProvider(this, nbttagcompound);
		powerProvider.configure(20, 25, 25, 25, 1000);

		filters[0] = nbttagcompound.getInteger("filters_0");
		filters[1] = nbttagcompound.getInteger("filters_1");
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
		PowerFramework.currentFramework.savePowerProvider(this, nbttagcompound);

		nbttagcompound.setInteger("filters_0", filters[0]);
		nbttagcompound.setInteger("filters_1", filters[1]);
	}

	public int getAnimationStage() {
		return animationStage;
	}

	/**
	 * Used to iterate the animation without computing the speed
	 */
	public void simpleAnimationIterate() {
		if (animationSpeed > 1) {
			animationStage += animationSpeed;

			if (animationStage > 300) {
				animationStage = 100;
			}
		} else if (animationStage > 0) {
			animationStage--;
		}
	}

	public void increaseAnimation() {
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

	public void decreaseAnimation() {
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

	@Override public void openChest() {}
	@Override public void closeChest() {}

	public void setFilter(int number, int liquidId) {
		filters[number] = liquidId;
	}

	public int getFilter(int number) {
		return filters[number];
	}

	@Override
	public boolean allowActions() {
		return false;
	}

	/* SMP GUI */
	public void getGUINetworkData(int i, int j) {
		switch (i) {
		case 0:
			filters[0] = j;
			break;
		case 1:
			filters[1] = j;
			break;
		}
	}

	public void sendGUINetworkData(Container container, ICrafting iCrafting) {
		iCrafting.updateCraftingInventoryInfo(container, 0, filters[0]);
		iCrafting.updateCraftingInventoryInfo(container, 1, filters[1]);
	}

	/* ITANKCONTAINER */
	@Override
	public int fill(Orientations from, LiquidStack resource, boolean doFill) {
		int used = 0;

		if (filters[0] != 0 || filters[1] != 0) {
			if (filters[0] == resource.itemID) {
				used += slot1.fill(from, resource.amount, resource.itemID, doFill);
			}

			if (filters[1] == resource.itemID) {
				used += slot2.fill(from, resource.amount - used, resource.itemID, doFill);
			}
		} else {
			used += slot1.fill(from, resource.amount, resource.itemID, doFill);
			used += slot2.fill(from, resource.amount - used, resource.itemID, doFill);
		}

		if (doFill && used > 0) {
			updateNetworkTime.markTime(worldObj);
			sendNetworkUpdate();
		}

		return used;
	}

	@Override
	public int fill(int tankIndex, LiquidStack resource, boolean doFill) {
		/// FIXME: TileRefinery.Slot must die!
		return 0;
	}

	@Override
	public LiquidStack drain(Orientations from, int maxEmpty, boolean doDrain) {
		return drain(2, maxEmpty, doDrain);
	}

	@Override
	public LiquidStack drain(int tankIndex, int maxEmpty, boolean doDrain) {
		int res = 0;

		if (result.quantity >= maxEmpty) {
			res = maxEmpty;

			if (doDrain) {
				result.quantity -= maxEmpty;
			}
		} else {
			res = result.quantity;

			if (doDrain) {
				result.quantity = 0;
			}
		}

		if (doDrain && res > 0) {
			updateNetworkTime.markTime(worldObj);
			sendNetworkUpdate();
		}

		return new LiquidStack(result.liquidId, res);
	}

	@Override
	public ILiquidTank[] getTanks() {
		return new ILiquidTank[] {
				new LiquidTank(slot1.liquidId, slot1.quantity, LIQUID_PER_SLOT),
				new LiquidTank(slot2.liquidId, slot2.quantity, LIQUID_PER_SLOT),
				new LiquidTank(result.liquidId, result.quantity, LIQUID_PER_SLOT),
		};
	}

}
