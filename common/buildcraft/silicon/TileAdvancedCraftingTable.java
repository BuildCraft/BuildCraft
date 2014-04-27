/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftSilicon;
import buildcraft.api.gates.IAction;
import buildcraft.api.gates.IActionReceptor;
import buildcraft.api.power.ILaserTarget;
import buildcraft.core.IMachine;
import buildcraft.core.TileBuffer;
import buildcraft.core.inventory.*;
import buildcraft.core.inventory.InventoryIterator.IInvSlot;
import buildcraft.core.inventory.filters.CraftingFilter;
import buildcraft.core.inventory.filters.IStackFilter;
import buildcraft.core.network.PacketIds;
import buildcraft.core.network.PacketSlotChange;
import buildcraft.core.triggers.ActionMachineControl;
import buildcraft.core.utils.CraftingHelper;
import buildcraft.core.utils.StringUtils;
import buildcraft.core.utils.Utils;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import static net.minecraftforge.common.util.ForgeDirection.*;

public class TileAdvancedCraftingTable extends TileLaserTableBase implements IInventory, ILaserTarget, IMachine, IActionReceptor, ISidedInventory {

	private final class InternalInventoryCraftingContainer extends Container {

		@Override
		public boolean canInteractWith(EntityPlayer var1) {
			return false;
		}
	}

	private final class CraftingGrid extends SimpleInventory {

		public int[] oreIDs = new int[9];

		public CraftingGrid() {
			super(9, "CraftingSlots", 1);
			Arrays.fill(oreIDs, -1);
		}

		@Override
		public void setInventorySlotContents(int slotId, ItemStack itemstack) {
			super.setInventorySlotContents(slotId, itemstack);
			if (TileAdvancedCraftingTable.this.getWorldObj() == null || !TileAdvancedCraftingTable.this.getWorldObj().isRemote) {
				oreIDs[slotId] = itemstack == null ? -1 : OreDictionary.getOreID(itemstack);
			}
		}
	}

	private final class InternalInventoryCrafting extends InventoryCrafting {

		int[] bindings = new int[9];
		ItemStack[] tempStacks;
		public int[] hitCount;
		private boolean useRecipeStack;

		private InternalInventoryCrafting() {
			super(new InternalInventoryCraftingContainer(), 3, 3);
		}

		@Override
		public ItemStack getStackInSlot(int slot) {
			if (slot >= 0 && slot < 9) {
				if (useRecipeStack || tempStacks == null) {
					return craftingSlots.getStackInSlot(slot);
				} else {
					if (bindings[slot] >= 0) {
						return tempStacks[bindings[slot]];
					}
				}
			}

			// vanilla returns null for out of bound stacks in InventoryCrafting as well
			return null;
		}

		@Override
		public void setInventorySlotContents(int slot, ItemStack par2ItemStack) {
			if (tempStacks != null && slot >= 0 && slot < 9 && bindings[slot] >= 0) {
				tempStacks[bindings[slot]] = par2ItemStack;
			}
		}

		@Override
		public ItemStack decrStackSize(int slot, int amount) {
			if (tempStacks != null && slot >= 0 && slot < 9 && bindings[slot] >= 0) {
				if (tempStacks[bindings[slot]].stackSize <= amount) {
					ItemStack result = tempStacks[bindings[slot]];
					tempStacks[bindings[slot]] = null;
					return result;
				} else {
					ItemStack result = tempStacks[bindings[slot]].splitStack(amount);
					if (tempStacks[bindings[slot]].stackSize <= 0) {
						tempStacks[bindings[slot]] = null;
					}
					return result;
				}
			} else {
				return null;
			}
		}

		public void recipeUpdate(boolean flag) {
			useRecipeStack = flag;
		}
	}

	private final class InternalPlayer extends EntityPlayer {

		public InternalPlayer() {
			super(TileAdvancedCraftingTable.this.getWorldObj(), new GameProfile(null, "[BuildCraft]"));
			posX = TileAdvancedCraftingTable.this.xCoord;
			posY = TileAdvancedCraftingTable.this.yCoord + 1;
			posZ = TileAdvancedCraftingTable.this.zCoord;
		}

		@Override
		public void addChatMessage(IChatComponent var1) {
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

	public InventoryCraftResult craftResult;
	private InternalInventoryCrafting internalInventoryCrafting;

	public TileAdvancedCraftingTable() {
		craftingSlots = new CraftingGrid();
		inv.addListener(this);
		invInput = new InventoryMapper(inv, 0, 15);
		invOutput = new InventoryMapper(inv, 15, 9);
		craftResult = new InventoryCraftResult();
	}

	private static final int[] SLOTS = Utils.createSlotArray(0, 24);
	private static final EnumSet<ForgeDirection> SEARCH_SIDES = EnumSet.of(DOWN, NORTH, SOUTH, EAST, WEST);
	private static final float REQUIRED_POWER = 500F;
	private final CraftingGrid craftingSlots;
	private final InventoryMapper invInput;
	private final InventoryMapper invOutput;
	private SlotCrafting craftSlot;
	private boolean craftable;
	private boolean justCrafted;
	private InternalPlayer internalPlayer;
	private IRecipe currentRecipe;
	private ActionMachineControl.Mode lastMode = ActionMachineControl.Mode.Unknown;
	private TileBuffer[] cache;

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		craftingSlots.writeToNBT(data, "craftingSlots");
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		if (data.hasKey("StorageSlots")) {
			inv.readFromNBT(data, "StorageSlots");
		}

		if (data.hasKey("items")) {
			craftingSlots.readFromNBT(data);
		} else {
			craftingSlots.readFromNBT(data, "craftingSlots");
		}
	}

	@Override
	public int getSizeInventory() {
		return 24;
	}

	@Override
	public String getInventoryName() {
		return StringUtils.localize("tile.assemblyWorkbenchBlock.name");
	}

	@Override
	public void markDirty() {
		super.markDirty();
		craftable = craftResult.getStackInSlot(0) != null;
	}

	@Override
	public double getRequiredEnergy() {
		return craftResult.getStackInSlot(0) != null ? REQUIRED_POWER : 0f;
	}

	@Override
	public int getProgressScaled(int i) {
		return (int) ((getEnergy() * i) / REQUIRED_POWER);
	}

	@Override
	public void invalidate() {
		super.invalidate();
		cache = null;
	}

	@Override
	public void updateEntity() {
		if (internalPlayer == null) {
			internalInventoryCrafting = new InternalInventoryCrafting();
			internalPlayer = new InternalPlayer();
			craftSlot = new SlotCrafting(internalPlayer, internalInventoryCrafting, craftResult, 0, 0, 0);
			updateRecipe();
		}
		if (!!worldObj.isRemote) {
			return;
		}
		if (lastMode == ActionMachineControl.Mode.Off) {
			return;
		}
		updateRecipe();
		searchNeighborsForIngredients();
		locateAndBindIngredients();
		updateRecipeOutputDisplay();
		justCrafted = false;
		if (canCraftAndOutput()) {
			if (getEnergy() >= getRequiredEnergy()) {
				craftItem();
				justCrafted = true;
			}
		} else {
			craftable = false;
			internalInventoryCrafting.tempStacks = null;
			internalInventoryCrafting.hitCount = null;
			setEnergy(0);
		}
	}

	private boolean canCraftAndOutput() {
		if (!hasIngredients()) {
			return false;
		}
		ItemStack output = getRecipeOutput();
		if (output == null) {
			return false;
		}
		return InvUtils.isRoomForStack(output, ForgeDirection.UP, invOutput);
	}

	private void locateAndBindIngredients() {
		internalInventoryCrafting.tempStacks = new InventoryCopy(inv).getItemStacks();
		internalInventoryCrafting.hitCount = new int[internalInventoryCrafting.tempStacks.length];
		ItemStack[] inputSlots = internalInventoryCrafting.tempStacks;
		for (int gridSlot = 0; gridSlot < craftingSlots.getSizeInventory(); gridSlot++) {
			internalInventoryCrafting.bindings[gridSlot] = -1;
			if (craftingSlots.getStackInSlot(gridSlot) == null) {
				continue;
			}
			boolean foundMatch = false;
			for (int inputSlot = 0; inputSlot < inputSlots.length; inputSlot++) {
				if (!isMatchingIngredient(gridSlot, inputSlot)) {
					continue;
				}
				if (internalInventoryCrafting.hitCount[inputSlot] < inputSlots[inputSlot].stackSize
						&& internalInventoryCrafting.hitCount[inputSlot] < inputSlots[inputSlot].getMaxStackSize()) {
					internalInventoryCrafting.bindings[gridSlot] = inputSlot;
					internalInventoryCrafting.hitCount[inputSlot]++;
					foundMatch = true;
					break;
				}
			}
			if (!foundMatch) {
				return;
			}
		}
	}

	private boolean isMatchingIngredient(int gridSlot, int inputSlot) {
		ItemStack inputStack = internalInventoryCrafting.tempStacks[inputSlot];
		if (inputStack == null) {
			return false;
		}
		if (StackHelper.isMatchingItem(craftingSlots.getStackInSlot(gridSlot), inputStack, true, false)) {
			return true;
		}
		if (StackHelper.isCraftingEquivalent(craftingSlots.oreIDs[gridSlot], inputStack)) {
			return true;
		}
		return false;
	}

	private boolean hasIngredients() {
		return currentRecipe != null && currentRecipe.matches(internalInventoryCrafting, worldObj);
	}

	private void craftItem() {
		ItemStack recipeOutput = getRecipeOutput();
		craftSlot.onPickupFromSlot(internalPlayer, recipeOutput);
		ItemStack[] tempStorage = internalInventoryCrafting.tempStacks;
		for (int i = 0; i < tempStorage.length; i++) {
			if (tempStorage[i] != null && tempStorage[i].stackSize <= 0) {
				tempStorage[i] = null;
			}
			inv.getItemStacks()[i] = tempStorage[i];
		}
		subtractEnergy(getRequiredEnergy());
		List<ItemStack> outputs = Lists.newArrayList(recipeOutput.copy());
		for (int i = 0; i < internalPlayer.inventory.mainInventory.length; i++) {
			if (internalPlayer.inventory.mainInventory[i] != null) {
				outputs.add(internalPlayer.inventory.mainInventory[i]);
				internalPlayer.inventory.mainInventory[i] = null;
			}
		}
		for (ItemStack output : outputs) {
			output.stackSize -= Transactor.getTransactorFor(invOutput).add(output, ForgeDirection.UP, true).stackSize;
			if (output.stackSize > 0) {
				output.stackSize -= Utils.addToRandomInventoryAround(worldObj, xCoord, yCoord, zCoord, output);
			}
			if (output.stackSize > 0) {
				InvUtils.dropItems(worldObj, output, xCoord, yCoord + 1, zCoord);
			}
		}
	}

	private void searchNeighborsForIngredients() {
		if (cache == null) {
			cache = TileBuffer.makeBuffer(worldObj, xCoord, yCoord, zCoord, false);
		}
		for (IInvSlot slot : InventoryIterator.getIterable(craftingSlots, ForgeDirection.UP)) {
			ItemStack ingred = slot.getStackInSlot();
			if (ingred == null) {
				continue;
			}
			IStackFilter filter = new CraftingFilter(ingred);
			if (InvUtils.countItems(invInput, ForgeDirection.UP, filter) < InvUtils.countItems(craftingSlots, ForgeDirection.UP, filter)) {
				for (ForgeDirection side : SEARCH_SIDES) {
					TileEntity tile = cache[side.ordinal()].getTile();
					if (tile instanceof IInventory) {
						IInventory inv = InvUtils.getInventory(((IInventory) tile));
						ItemStack result = InvUtils.moveOneItem(inv, side.getOpposite(), invInput, side, filter);
						if (result != null) {
							return;
						}
					}
				}
			}
		}
	}

	public void updateCraftingMatrix(int slot, ItemStack stack) {
		craftingSlots.setInventorySlotContents(slot, stack);
		updateRecipe();
		if (worldObj.isRemote) {
			PacketSlotChange packet = new PacketSlotChange(PacketIds.ADVANCED_WORKBENCH_SETSLOT, xCoord, yCoord, zCoord, slot, stack);
			BuildCraftSilicon.instance.sendToServer(packet);
		}
	}

	private void updateRecipe() {
		if (internalInventoryCrafting == null) {
			return;
		}
		internalInventoryCrafting.recipeUpdate(true);
		if (this.currentRecipe == null || !this.currentRecipe.matches(internalInventoryCrafting, worldObj)) {
			currentRecipe = CraftingHelper.findMatchingRecipe(internalInventoryCrafting, worldObj);
		}
		internalInventoryCrafting.recipeUpdate(false);
		markDirty();
	}

	private void updateRecipeOutputDisplay() {
		if (internalInventoryCrafting == null || currentRecipe == null) {
			craftResult.setInventorySlotContents(0, null);
			return;
		}
		ItemStack resultStack = getRecipeOutput();
		if (resultStack == null) {
			internalInventoryCrafting.recipeUpdate(true);
			resultStack = getRecipeOutput();
			internalInventoryCrafting.recipeUpdate(false);
		}
		craftResult.setInventorySlotContents(0, resultStack);
		markDirty();
	}

	private ItemStack getRecipeOutput() {
		if (internalInventoryCrafting == null || currentRecipe == null) {
			return null;
		}
		return currentRecipe.getCraftingResult(internalInventoryCrafting);
	}

	public IInventory getCraftingSlots() {
		return craftingSlots;
	}

	public IInventory getOutputSlot() {
		return craftResult;
	}

	@Override
	public boolean canCraft() {
		return craftable && !justCrafted && lastMode != ActionMachineControl.Mode.Off;
	}

	@Override
	public boolean isActive() {
		return requiresLaserEnergy();
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
		return action == BuildCraftCore.actionOn || action == BuildCraftCore.actionOff;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		return SLOTS;
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack stack, int side) {
		return isItemValidForSlot(slot, stack);
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, int side) {
		return slot >= 15;
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		return slot < 15;
	}

	@Override
	public void actionActivated(IAction action) {
		if (action == BuildCraftCore.actionOn) {
			lastMode = ActionMachineControl.Mode.On;
		} else if (action == BuildCraftCore.actionOff) {
			lastMode = ActionMachineControl.Mode.Off;
		}
	}

	@Override
	public boolean hasCustomInventoryName() {
		return false;
	}
}
