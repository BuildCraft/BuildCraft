/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory;

import buildcraft.core.TileBuildCraft;
import buildcraft.core.inventory.InvUtils;
import buildcraft.core.inventory.InventoryConcatenator;
import buildcraft.core.inventory.InventoryIterator;
import buildcraft.core.inventory.InventoryIterator.IInvSlot;
import buildcraft.core.inventory.SimpleInventory;
import buildcraft.core.inventory.StackHelper;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.CraftingHelper;
import buildcraft.core.utils.Utils;
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
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.ChunkCoordinates;
import net.minecraftforge.common.ForgeDirection;

public class TileAutoWorkbench extends TileBuildCraft implements ISidedInventory {

	public static final int SLOT_RESULT = 0;
	public static final int CRAFT_TIME = 256;
	public static final int UPDATE_TIME = 16;
	private static final int[] SLOTS = Utils.createSlotArray(0, 10);
	private SimpleInventory resultInv = new SimpleInventory(1, "Auto Workbench", 64);
	public InventoryCrafting craftMatrix = new LocalInventoryCrafting();
	private IInventory inv = InventoryConcatenator.make().add(resultInv).add(craftMatrix);
	public boolean useLast;
	private EntityPlayer internalPlayer;
	private SlotCrafting craftSlot;
	private InventoryCraftResult craftResult = new InventoryCraftResult();
	public int progress;
	private int update = Utils.RANDOM.nextInt();

	private class LocalInventoryCrafting extends InventoryCrafting {

		public LocalInventoryCrafting() {
			super(new Container() {
				@Override
				public boolean canInteractWith(EntityPlayer entityplayer) {
					return false;
				}
			}, 3, 3);
		}
	}

	private final class InternalPlayer extends EntityPlayer {

		public InternalPlayer() {
			super(TileAutoWorkbench.this.worldObj, "[BuildCraft]");
			posX = TileAutoWorkbench.this.xCoord;
			posY = TileAutoWorkbench.this.yCoord + 1;
			posZ = TileAutoWorkbench.this.zCoord;
		}

		@Override
		public void sendChatToPlayer(ChatMessageComponent var1) {
		}

		@Override
		public boolean canCommandSenderUseCommand(int var1, String var2) {
			return false;
		}

		@Override
		public ChunkCoordinates getPlayerCoordinates() {
			return null;
		}
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
	public String getInvName() {
		return "";
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this && player.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 64.0D;
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		resultInv.readFromNBT(data);
		Utils.readInvFromNBT(craftMatrix, "matrix", data);

		// Legacy Code
		if (data.hasKey("stackList")) {
			ItemStack[] stacks = new ItemStack[9];
			Utils.readStacksFromNBT(data, "stackList", stacks);
			for (int i = 0; i < 9; i++) {
				craftMatrix.setInventorySlotContents(i, stacks[i]);
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		resultInv.writeToNBT(data);
		Utils.writeInvToNBT(craftMatrix, "matrix", data);
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
			if (stack.getItem().hasContainerItem()) {
				return null;
			}
		}

		return CraftingHelper.findMatchingRecipe(craftMatrix, worldObj);
	}

	@Override
	public boolean canUpdate() {
		return true;
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		if (CoreProxy.proxy.isRenderWorld(worldObj)) {
			return;
		}

		balanceSlots();

		if (craftSlot == null) {
			internalPlayer = new InternalPlayer();
			craftSlot = new SlotCrafting(internalPlayer, craftMatrix, craftResult, 0, 0, 0);
		}
		if (resultInv.getStackInSlot(SLOT_RESULT) != null) {
			return;
		}
		update++;
		if (update % UPDATE_TIME == 0) {
			updateCrafting();
		}
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
			if (stackA == null)
				continue;
			for (IInvSlot slotB : InventoryIterator.getIterable(craftMatrix, ForgeDirection.UP)) {
				if (slotA.getIndex() == slotB.getIndex())
					continue;
				ItemStack stackB = slotB.getStackInSlot();
				if (stackB == null)
					continue;
				if (StackHelper.instance().canStacksMerge(stackA, stackB)) {
					if (stackA.stackSize > stackB.stackSize + 1) {
						stackA.stackSize--;
						stackB.stackSize++;
						return;
					}
				}
			}
		}
	}

	/**
	 * Increment craft job, find recipes, produce output
	 */
	private void updateCrafting() {
		IRecipe recipe = findRecipe();
		if (recipe == null) {
			progress = 0;
			return;
		}
		if (!useLast && isLast()) {
			progress = 0;
			return;
		}
		progress += UPDATE_TIME;
		if (progress < CRAFT_TIME) {
			return;
		}
		progress = 0;
		useLast = false;
		ItemStack result = recipe.getCraftingResult(craftMatrix);
		if (result == null) {
			return;
		}
		result = result.copy();
		craftSlot.onPickupFromSlot(internalPlayer, result);
		resultInv.setInventorySlotContents(SLOT_RESULT, result);

		// clean fake player inventory (crafting handler support)
		for (IInvSlot slot : InventoryIterator.getIterable(internalPlayer.inventory, ForgeDirection.UP)) {
			ItemStack stack = slot.getStackInSlot();
			if (stack != null) {
				slot.setStackInSlot(null);
				InvUtils.dropItems(worldObj, stack, xCoord, yCoord + 1, zCoord);
			}
		}
	}

	@Override
	public void openChest() {
	}

	@Override
	public void closeChest() {
	}

	@Override
	public boolean isInvNameLocalized() {
		return false;
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		if (slot == SLOT_RESULT)
			return false;
		if (stack == null)
			return false;
		if (!stack.isStackable())
			return false;
		if (stack.getItem().hasContainerItem())
			return false;
		if (getStackInSlot(slot) == null)
			return false;
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
		int minStackSize = 64;
		for (IInvSlot slot : InventoryIterator.getIterable(craftMatrix, ForgeDirection.UP)) {
			ItemStack stack = slot.getStackInSlot();
			if (stack != null && stack.stackSize < minStackSize) {
				minStackSize = stack.stackSize;
			}
		}
		return minStackSize <= 1;
	}
}