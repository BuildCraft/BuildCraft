/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory;

import java.lang.ref.WeakReference;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.IInvSlot;
import buildcraft.api.power.IRedstoneEngineReceiver;
import buildcraft.api.tiles.IHasWork;
import buildcraft.core.lib.RFBattery;
import buildcraft.core.lib.block.TileBuildCraft;
import buildcraft.core.lib.inventory.InvUtils;
import buildcraft.core.lib.inventory.InventoryConcatenator;
import buildcraft.core.lib.inventory.InventoryIterator;
import buildcraft.core.lib.inventory.SimpleInventory;
import buildcraft.core.lib.inventory.StackHelper;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.lib.utils.CraftingUtils;
import buildcraft.core.lib.utils.Utils;

public class TileAutoWorkbench extends TileBuildCraft implements ISidedInventory, IHasWork, IRedstoneEngineReceiver {

	public static final int SLOT_RESULT = 0;
	public static final int CRAFT_TIME = 256;
	public static final int UPDATE_TIME = 16;
	private static final int[] SLOTS = Utils.createSlotArray(0, 10);

	public LocalInventoryCrafting craftMatrix = new LocalInventoryCrafting();
	public boolean useLast;
	public int progress;
	
	/** Set when nothing can be crafted unless the input grid is changed.
	 * Cleared whenever the input grid is changed. Not persisted. */
	private boolean isJammed;
	
	/** Set when any slot changes. Cleared when balanceSlots is called. */
	private boolean needsBalancing;

	private SimpleInventory resultInv = new SimpleInventory(1, "Auto Workbench", 64);

	private IInventory inv = InventoryConcatenator.make().add(resultInv).add(craftMatrix);

	private SlotCrafting craftSlot;
	private InventoryCraftResult craftResult = new InventoryCraftResult();

	private int update = Utils.RANDOM.nextInt();

	private IRecipe currentRecipe;
	private boolean hasWork = false;

	public TileAutoWorkbench() {
		super();
		this.setBattery(new RFBattery(16, 16, 0));
	}

	@Override
	public boolean hasWork() {
		return !isJammed && hasWork;
	}

	@Override
	public boolean canConnectRedstoneEngine(ForgeDirection side) {
		return true;
	}

	public class LocalInventoryCrafting extends InventoryCrafting {

		public LocalInventoryCrafting() {
			super(new Container() {
				@Override
				public boolean canInteractWith(EntityPlayer entityplayer) {
					return false;
				}
			}, 3, 3);
		}

		private void rebuildCache() {
			isJammed = false;
			needsBalancing = true;
			currentRecipe = findRecipe();
			hasWork = currentRecipe != null && currentRecipe.getRecipeOutput() != null && (useLast || !isLast());
		}
		
		@Override
		public void setInventorySlotContents(int slot, ItemStack stack) {
			super.setInventorySlotContents(slot, stack);
			rebuildCache();
		}
		
		@Override
		public void markDirty() {
			super.markDirty();
			rebuildCache();
		}
		
		@Override
		public ItemStack decrStackSize(int slot, int amount) {
			rebuildCache();
			return super.decrStackSize(slot, amount);
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
		InvUtils.readInvFromNBT(craftMatrix, "matrix", data);
		craftMatrix.rebuildCache();

		// Legacy Code
		if (data.hasKey("stackList")) {
			ItemStack[] stacks = new ItemStack[9];
			InvUtils.readStacksFromNBT(data, "stackList", stacks);
			for (int i = 0; i < 9; i++) {
				craftMatrix.setInventorySlotContents(i, stacks[i]);
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		resultInv.writeToNBT(data);
		InvUtils.writeInvToNBT(craftMatrix, "matrix", data);
	}

	public ItemStack findRecipeOutput() {
		IRecipe recipe = findRecipe();
		if (recipe == null) {
			return null;
		}
		ItemStack result = recipe.getCraftingResult(craftMatrix);
		if (result != null) {
			result = result.copy();
		}
		return result;
	}

	public IRecipe findRecipe() {
		for (IInvSlot slot : InventoryIterator.getIterable(craftMatrix, ForgeDirection.UP)) {
			ItemStack stack = slot.getStackInSlot();
			if (stack == null) {
				continue;
			}
			if (!stack.isStackable()) {
				return null;
			}
			if (stack.getItem().hasContainerItem(stack)) {
				return null;
			}
		}

		return CraftingUtils.findMatchingRecipe(craftMatrix, worldObj);
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
		
		if (needsBalancing) {
			balanceSlots();
		}
		
		if (isJammed) {
			progress = 0;
			return;
		}

		if (craftSlot == null) {
			craftSlot = new SlotCrafting(getInternalPlayer().get(), craftMatrix, craftResult, 0, 0, 0);
		}

		if (!hasWork) {
			return;
		}

		if (resultInv.getStackInSlot(SLOT_RESULT) != null) {
			return;
		}

		int updatesLeft = getBattery().getEnergyStored() + 1;

		while (updatesLeft > 0) {
			update++;

			if (update % UPDATE_TIME == 0) {
				updateCrafting();

				if (isJammed || !hasWork || resultInv.getStackInSlot(SLOT_RESULT) != null) {
					progress = 0;
					break;
				}
			}

			updatesLeft--;

		}
		getBattery().setEnergy(0);
	}

	public int getProgressScaled(int i) {
		return (progress * i) / CRAFT_TIME;
	}

	/**
	 * Evenly redistributes items between all the slots.
	 */
	private void balanceSlots() {
		for (IInvSlot slotA : InventoryIterator.getIterable(craftMatrix, ForgeDirection.UP)) {
			ItemStack stackA = slotA.getStackInSlot();
			if (stackA == null) {
				continue;
			}
			for (IInvSlot slotB : InventoryIterator.getIterable(craftMatrix, ForgeDirection.UP)) {
				if (slotA.getIndex() == slotB.getIndex()) {
					continue;
				}
				ItemStack stackB = slotB.getStackInSlot();
				if (stackB == null) {
					continue;
				}
				if (StackHelper.canStacksMerge(stackA, stackB)) {
					if (stackA.stackSize > stackB.stackSize + 1) {
						stackA.stackSize--;
						stackB.stackSize++;
						return;
					}
				}
			}
		}

		craftMatrix.rebuildCache();
		needsBalancing = false;
	}

	/**
	 * Increment craft job, find recipes, produce output
	 */
	private void updateCrafting() {
		if (currentRecipe == null) {
			progress = 0;
			isJammed = true;
			return;
		}
		if (!useLast && isLast()) {
			progress = 0;
			isJammed = true;
			return;
		}
		progress += UPDATE_TIME;
		if (progress < CRAFT_TIME) {
			return;
		}
		progress = 0;
		useLast = false;
		ItemStack result = currentRecipe.getCraftingResult(craftMatrix);
		if (result == null) {
			isJammed = true;
			return;
		}
		result = result.copy();
		craftSlot.onPickupFromSlot(getInternalPlayer().get(), result);
		resultInv.setInventorySlotContents(SLOT_RESULT, result);

		// clean fake player inventory (crafting handler support)
		for (IInvSlot slot : InventoryIterator.getIterable(getInternalPlayer().get().inventory, ForgeDirection.UP)) {
			ItemStack stack = slot.getStackInSlot();
			if (stack != null) {
				slot.setStackInSlot(null);
				InvUtils.dropItems(worldObj, stack, xCoord, yCoord + 1, zCoord);
			}
		}

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
		if (stack == null) {
			return false;
		}
		if (!stack.isStackable()) {
			return false;
		}
		if (stack.getItem().hasContainerItem(stack)) {
			return false;
		}
		if (getStackInSlot(slot) == null) {
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
		return isItemValidForSlot(slot, stack);
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, int side) {
		return slot == SLOT_RESULT;
	}

	/**
	 * Returns true if there are only enough inputs for a single craft job.
	 *
	 * @return true or false
	 */
	public boolean isLast() {
		for (IInvSlot slot : InventoryIterator.getIterable(craftMatrix, ForgeDirection.UP)) {
			ItemStack stack = slot.getStackInSlot();
			if (stack != null && stack.stackSize <= 1) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean hasCustomInventoryName() {
		return false;
	}
}
