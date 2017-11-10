/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;

import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.recipes.IIntegrationRecipe;
import buildcraft.core.lib.inventory.SimpleInventory;
import buildcraft.core.lib.inventory.StackHelper;
import buildcraft.core.lib.utils.NetworkUtils;
import buildcraft.core.lib.utils.StringUtils;
import buildcraft.core.lib.utils.Utils;

public class TileIntegrationTable extends TileLaserTableBase implements ISidedInventory {
	public static final int SLOT_OUTPUT = 9;

	private static final int CYCLE_LENGTH = 16;
	private static final int[] SLOTS = Utils.createSlotArray(0, 10);

	public final IInventory clientOutputInv = new SimpleInventory(1, "Preview", 64);

	private int tick = 0;
	private IIntegrationRecipe activeRecipe;
	private boolean activeRecipeValid = false;
	private int maxExpCountClient;

	@Override
	public void initialize() {
		super.initialize();

		updateRecipe();
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (worldObj.isRemote) {
			return;
		}

		if (activeRecipe == null || !activeRecipeValid) {
			setEnergy(0);
			return;
		}

		tick++;
		if (tick % CYCLE_LENGTH != 0) {
			return;
		}

		updateRecipeOutput();

		ItemStack output = clientOutputInv.getStackInSlot(0);
		if (!isRoomForOutput(output)) {
			setEnergy(0);
			return;
		}

		if (getEnergy() >= activeRecipe.getEnergyCost()) {
			setEnergy(0);

			output = activeRecipe.craft(getStackInSlot(0), getExpansions(), false);

			if (output != null) {
				ItemStack input = getStackInSlot(0);

				if (input.stackSize > output.stackSize) {
					input.stackSize -= output.stackSize;
				} else {
					setInventorySlotContents(0, null);
				}

				outputStack(output, this, 9, false);

				for (int i = 1; i < 9; i++) {
					if (getStackInSlot(i) != null && getStackInSlot(i).stackSize == 0) {
						setInventorySlotContents(i, null);
					}
				}
			}
		}
	}

	private List<ItemStack> getExpansions() {
		List<ItemStack> expansions = new ArrayList<ItemStack>();
		for (int i = 1; i < 9; i++) {
			if (getStackInSlot(i) != null) {
				expansions.add(getStackInSlot(i));
			}
		}
		return expansions;
	}

	private void updateRecipeOutput() {
		ItemStack oldClientOutput = clientOutputInv.getStackInSlot(0);

		activeRecipeValid = false;
		clientOutputInv.setInventorySlotContents(0, null);

		if (activeRecipe != null) {
			List<ItemStack> expansions = getExpansions();

			if (expansions.size() > 0) {
				clientOutputInv.setInventorySlotContents(0, activeRecipe.craft(getStackInSlot(0), expansions, true));
			}
		}

		activeRecipeValid = clientOutputInv.getStackInSlot(0) != null;

		if (!StackHelper.isEqualItem(clientOutputInv.getStackInSlot(0), oldClientOutput)) {
			sendNetworkUpdate();
		}
	}

	private void setNewActiveRecipe() {
		ItemStack input = getStackInSlot(0);
		if ((input != null && activeRecipe != null && activeRecipe.isValidInput(input)) || (input == null && activeRecipe == null)) {
			return;
		}

		activeRecipe = null;
		activeRecipeValid = false;

		if (input != null && input.getItem() != null) {
			for (IIntegrationRecipe recipe : BuildcraftRecipeRegistry.integrationTable.getRecipes()) {
				if (recipe.isValidInput(input)) {
					activeRecipe = recipe;
					break;
				}
			}
		}

		sendNetworkUpdate();
	}

	private boolean isRoomForOutput(ItemStack output) {
		ItemStack existingOutput = inv.getStackInSlot(SLOT_OUTPUT);
		if (existingOutput == null) {
			return true;
		}
		if (StackHelper.canStacksMerge(output, existingOutput) && output.stackSize + existingOutput.stackSize <= output.getMaxStackSize()) {
			return true;
		}
		return false;
	}

	@Override
	public void writeData(ByteBuf buf) {
		buf.writeByte((byte) getMaxExpansionCount());
		NetworkUtils.writeStack(buf, clientOutputInv.getStackInSlot(0));
	}

	@Override
	public void readData(ByteBuf buf) {
		maxExpCountClient = buf.readByte();
		clientOutputInv.setInventorySlotContents(0, NetworkUtils.readStack(buf));
	}

	public int getMaxExpansionCount() {
		return worldObj.isRemote ? maxExpCountClient : (activeRecipe != null ? activeRecipe.getMaximumExpansionCount(getStackInSlot(0)) : 0);
	}

	@Override
	public int getRequiredEnergy() {
		return hasWork() ? activeRecipe.getEnergyCost() : 0;
	}

	@Override
	public boolean canCraft() {
		return hasWork();
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		if (slot == 0) {
			return true;
		} else if (activeRecipe == null) {
			return false;
		} else if (slot < 9) {
			int expansionCount = activeRecipe.getMaximumExpansionCount(getStackInSlot(0));
			if (expansionCount > 0 && slot > expansionCount) {
				return false;
			}
			return activeRecipe.isValidExpansion(getStackInSlot(0), stack);
		} else {
			return false;
		}
	}

	@Override
	public int getSizeInventory() {
		return 10;
	}

	@Override
	public String getInventoryName() {
		return StringUtils.localize("tile.integrationTableBlock.name");
	}

	@Override
	public boolean hasCustomInventoryName() {
		return false;
	}

	@Override
	public boolean hasWork() {
		return activeRecipeValid;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		super.setInventorySlotContents(slot, stack);

		if (slot == 0) {
			updateRecipe();
		}
		if (slot < 9) {
			updateRecipeOutput();
		}
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount) {
		ItemStack result = super.decrStackSize(slot, amount);

		if (slot == 0) {
			updateRecipe();
		}
		if (slot < 9) {
			updateRecipeOutput();
		}

		return result;
	}

	@Override
	public void markDirty() {
		super.markDirty();
		updateRecipeOutput();
	}

	private void updateRecipe() {
		setNewActiveRecipe();
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		return SLOTS;
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack stack, int side) {
		if (slot == 0) {
			return true;
		} else if (activeRecipe == null) {
			return false;
		} else if (slot < 9) {
			int expansionCount = activeRecipe.getMaximumExpansionCount(getStackInSlot(0));
			if (expansionCount > 0 && slot > expansionCount) {
				return false;
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, int side) {
		return slot == 9;
	}
}
