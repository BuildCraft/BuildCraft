/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory;

import java.lang.ref.WeakReference;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.WorldServer;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.IInvSlot;
import buildcraft.api.power.IRedstoneEngine;
import buildcraft.api.power.IRedstoneEngineReceiver;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.tiles.IHasWork;
import buildcraft.core.lib.RFBattery;
import buildcraft.core.lib.block.TileBuildCraft;
import buildcraft.core.lib.gui.ContainerDummy;
import buildcraft.core.lib.inventory.InvUtils;
import buildcraft.core.lib.inventory.InventoryConcatenator;
import buildcraft.core.lib.inventory.InventoryIterator;
import buildcraft.core.lib.inventory.SimpleInventory;
import buildcraft.core.lib.inventory.StackHelper;
import buildcraft.core.lib.utils.CraftingUtils;
import buildcraft.core.lib.utils.Utils;
import buildcraft.core.proxy.CoreProxy;

public class TileAutoWorkbench extends TileBuildCraft implements ISidedInventory, IHasWork, IRedstoneEngineReceiver, IDebuggable {

	public static final int SLOT_RESULT = 9;
	public static final int CRAFT_TIME = 256;
	public static final int UPDATE_TIME = 16;
	private static final int[] SLOTS = Utils.createSlotArray(0, 10);

	public int progress = 0;
	public LocalInventoryCrafting craftMatrix = new LocalInventoryCrafting();

	private SimpleInventory resultInv = new SimpleInventory(1, "Auto Workbench", 64);
	private SimpleInventory inputInv = new SimpleInventory(9, "Auto Workbench", 64) {
		@Override
		public void setInventorySlotContents(int slotId, ItemStack itemstack) {
			super.setInventorySlotContents(slotId, itemstack);
			if (craftMatrix.isInputMissing && getStackInSlot(slotId) != null) {
				craftMatrix.isInputMissing = false;
			}
		}

		@Override
		public void markDirty() {
			super.markDirty();
			craftMatrix.isInputMissing = false;
		}
	};

	private IInventory inv = InventoryConcatenator.make().add(inputInv).add(resultInv).add(craftMatrix);

	private SlotCrafting craftSlot;
	private InventoryCraftResult craftResult = new InventoryCraftResult();

	private int[] bindings = new int[9];
	private int[] bindingCounts = new int[9];

	private int update = Utils.RANDOM.nextInt();

	private boolean hasWork = false;
	private boolean scheduledCacheRebuild = false;

	public TileAutoWorkbench() {
		super();
		this.setBattery(new RFBattery(16, 16, 0));
	}

	@Override
	public boolean hasWork() {
		return hasWork;
	}

	@Override
	public boolean canConnectRedstoneEngine(ForgeDirection side) {
		return true;
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection side) {
		TileEntity tile = worldObj.getTileEntity(xCoord + side.offsetX, yCoord + side.offsetY, zCoord + side.offsetZ);
		return tile instanceof IRedstoneEngine;
	}

	@Override
	public void getDebugInfo(List<String> info, ForgeDirection side, ItemStack debugger, EntityPlayer player) {
		info.add("isInputMissing = " + craftMatrix.isInputMissing);
		info.add("isOutputJammed = " + craftMatrix.isOutputJammed);
	}

	public class LocalInventoryCrafting extends InventoryCrafting {
		public IRecipe currentRecipe;
		public boolean useBindings, isOutputJammed, isInputMissing;

		public LocalInventoryCrafting() {
			super(new ContainerDummy(), 3, 3);
		}

		@Override
		public ItemStack getStackInSlot(int slot) {
			if (useBindings) {
				if (slot >= 0 && slot < 9 && bindings[slot] >= 0) {
					return inputInv.getStackInSlot(bindings[slot]);
				} else {
					return null;
				}
			} else {
				return super.getStackInSlot(slot);
			}
		}

		public ItemStack getRecipeOutput() {
			currentRecipe = findRecipe(); // Fixes repair recipe handling (why is it not dynamic?)
			if (currentRecipe == null) {
				return null;
			}
			ItemStack result = currentRecipe.getCraftingResult(this);
			if (result != null) {
				result = result.copy();
			}
			return result;
		}

		private IRecipe findRecipe() {
			for (IInvSlot slot : InventoryIterator.getIterable(this, ForgeDirection.UP)) {
				ItemStack stack = slot.getStackInSlot();
				if (stack == null) {
					continue;
				}
				if (stack.getItem().hasContainerItem(stack)) {
					return null;
				}
			}

			return CraftingUtils.findMatchingRecipe(craftMatrix, worldObj);
		}

		public void rebuildCache() {
			currentRecipe = findRecipe();
			hasWork = currentRecipe != null && currentRecipe.getRecipeOutput() != null;

			ItemStack result = getRecipeOutput();
			ItemStack resultInto = resultInv.getStackInSlot(0);

			if (resultInto != null && (
					!StackHelper.canStacksMerge(resultInto, result)
							|| resultInto.stackSize + result.stackSize > resultInto.getMaxStackSize())
					) {
				isOutputJammed = true;
			} else {
				isOutputJammed = false;
			}
		}

		@Override
		public void setInventorySlotContents(int slot, ItemStack stack) {
			if (useBindings) {
				if (slot >= 0 && slot < 9 && bindings[slot] >= 0) {
					inputInv.setInventorySlotContents(bindings[slot], stack);
				}
				return;
			}
			super.setInventorySlotContents(slot, stack);
			scheduledCacheRebuild = true;
		}

		@Override
		public void markDirty() {
			super.markDirty();
			scheduledCacheRebuild = true;
		}

		@Override
		public ItemStack decrStackSize(int slot, int amount) {
			if (useBindings) {
				if (slot >= 0 && slot < 9 && bindings[slot] >= 0) {
					return inputInv.decrStackSize(bindings[slot], amount);
				} else {
					return null;
				}
			}
			scheduledCacheRebuild = true;
			return decrStackSize(slot, amount);
		}

		public void setUseBindings(boolean use) {
			useBindings = use;
		}
	}

	public WeakReference<EntityPlayer> getInternalPlayer() {
		return CoreProxy.proxy.getBuildCraftPlayer((WorldServer) worldObj, xCoord, yCoord + 1, zCoord);
	}

	@Override
	public void markDirty() {
		super.markDirty();
		inv.markDirty();
	}

	@Override
	public int getSizeInventory() {
		return 10;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return inv.getStackInSlot(slot);
	}

	@Override
	public ItemStack decrStackSize(int slot, int count) {
		return inv.decrStackSize(slot, count);
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		inv.setInventorySlotContents(slot, stack);
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		return inv.getStackInSlotOnClosing(slot);
	}

	@Override
	public String getInventoryName() {
		return "";
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this && player.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 64.0D;
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		resultInv.readFromNBT(data);
		if (data.hasKey("input")) {
			InvUtils.readInvFromNBT(inputInv, "input", data);
			InvUtils.readInvFromNBT(craftMatrix, "matrix", data);
		} else {
			InvUtils.readInvFromNBT(inputInv, "matrix", data);
			for (int i = 0; i < 9; i++) {
				ItemStack inputStack = inputInv.getStackInSlot(i);
				if (inputStack != null) {
					ItemStack matrixStack = inputStack.copy();
					matrixStack.stackSize = 1;
					craftMatrix.setInventorySlotContents(i, matrixStack);
				}
			}
		}

		craftMatrix.rebuildCache();
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		resultInv.writeToNBT(data);
		InvUtils.writeInvToNBT(inputInv, "input", data);
		InvUtils.writeInvToNBT(craftMatrix, "matrix", data);
	}


	@Override
	public boolean canUpdate() {
		return true;
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (worldObj.isRemote) {
			return;
		}

		if (scheduledCacheRebuild) {
			craftMatrix.rebuildCache();
			scheduledCacheRebuild = false;
		}

		if (craftMatrix.isOutputJammed || craftMatrix.isInputMissing || craftMatrix.currentRecipe == null) {
			progress = 0;
			return;
		}

		if (craftSlot == null) {
			craftSlot = new SlotCrafting(getInternalPlayer().get(), craftMatrix, craftResult, 0, 0, 0);
		}

		if (!hasWork) {
			return;
		}

		int updateNext = update + getBattery().getEnergyStored() + 1;
		int updateThreshold = (update & ~15) + 16;
		update = Math.min(updateThreshold, updateNext);
		if ((update % UPDATE_TIME) == 0) {
			updateCrafting();
		}
		getBattery().setEnergy(0);
	}

	public int getProgressScaled(int i) {
		return (progress * i) / CRAFT_TIME;
	}

	/**
	 * Increment craft job, find recipes, produce output
	 */
	private void updateCrafting() {
		progress += UPDATE_TIME;

		for (int i = 0; i < 9; i++) {
			bindingCounts[i] = 0;
		}
		for (int i = 0; i < 9; i++) {
			ItemStack comparedStack = craftMatrix.getStackInSlot(i);
			if (comparedStack == null || comparedStack.getItem() == null) {
				bindings[i] = -1;
				continue;
			}

			if (bindings[i] == -1 || !StackHelper.isMatchingItem(inputInv.getStackInSlot(bindings[i]), comparedStack, true, true)) {
				boolean found = false;
				for (int j = 0; j < 9; j++) {
					if (j == bindings[i]) {
						continue;
					}

					ItemStack inputInvStack = inputInv.getStackInSlot(j);

					if (StackHelper.isMatchingItem(inputInvStack, comparedStack, true, false)
							&& inputInvStack.stackSize > bindingCounts[j]) {
						found = true;
						bindings[i] = j;
						bindingCounts[j]++;
						break;
					}
				}
				if (!found) {
					craftMatrix.isInputMissing = true;
					progress = 0;
					return;
				}
			} else {
				bindingCounts[bindings[i]]++;
			}
		}

		for (int i = 0; i < 9; i++) {
			if (bindingCounts[i] > 0) {
				ItemStack stack = inputInv.getStackInSlot(i);
				if (stack != null && stack.stackSize < bindingCounts[i]) {
					// Do not break progress yet, instead give it a chance to rebuild
					// It will quit when trying to find a valid binding to "fit in"
					for (int j = 0; j < 9; j++) {
						if (bindings[j] == i) {
							bindings[j] = -1;
						}
					}
					return;
				}
			}
		}

		if (progress < CRAFT_TIME) {
			return;
		}

		progress = 0;

		craftMatrix.setUseBindings(true);
		ItemStack result = craftMatrix.getRecipeOutput();

		if (result != null && result.stackSize > 0) {
			ItemStack resultInto = resultInv.getStackInSlot(0);

			craftSlot.onPickupFromSlot(getInternalPlayer().get(), result);

			if (resultInto == null) {
				resultInv.setInventorySlotContents(0, result);
			} else {
				resultInto.stackSize += result.stackSize;
			}
		}

		craftMatrix.setUseBindings(false);
		craftMatrix.rebuildCache();
	}

	@Override
	public void openInventory() {
	}

	@Override
	public void closeInventory() {
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		if (slot == SLOT_RESULT) {
			return false;
		}
		if (stack.getItem().hasContainerItem(stack)) {
			return false;
		}
		return true;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int var1) {
		return SLOTS;
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack stack, int side) {
		if (slot >= 9) {
			return false;
		}
		ItemStack slotStack = inv.getStackInSlot(slot);
		if (StackHelper.canStacksMerge(stack, slotStack)) {
			return true;
		}
		for (int i = 0; i < 9; i++) {
			ItemStack inputStack = craftMatrix.getStackInSlot(i);
			if (inputStack != null && StackHelper.isMatchingItem(inputStack, stack, true, false)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, int side) {
		return slot == SLOT_RESULT;
	}

	@Override
	public boolean hasCustomInventoryName() {
		return false;
	}
}
