/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.recipes.BuildcraftRecipes;
import buildcraft.api.recipes.IIntegrationRecipeManager.IIntegrationRecipe;
import buildcraft.core.inventory.ITransactor;
import buildcraft.core.inventory.InventoryMapper;
import buildcraft.core.inventory.SimpleInventory;
import buildcraft.core.inventory.StackHelper;
import buildcraft.core.inventory.Transactor;
import buildcraft.core.triggers.ActionMachineControl;
import buildcraft.core.utils.StringUtils;
import buildcraft.core.utils.Utils;

public class TileIntegrationTable extends TileLaserTableBase implements ISidedInventory {

	public static final int SLOT_INPUT_A = 0;
	public static final int SLOT_INPUT_B = 1;
	public static final int SLOT_OUTPUT = 2;
	private static final int CYCLE_LENGTH = 32;
	private static final int[] SLOTS = Utils.createSlotArray(0, 3);
	private static final int[] SLOT_COMPONENTS = Utils.createSlotArray(3, 9);
	private int tick = 0;
	private SimpleInventory invRecipeOutput = new SimpleInventory(1, "integrationOutput", 64);
	private InventoryMapper invOutput = new InventoryMapper(inv, SLOT_OUTPUT, 1, false);
	private IIntegrationRecipe currentRecipe;
	private boolean canCraft = false;

	public IInventory getRecipeOutput() {
		return invRecipeOutput;
	}

	private ItemStack[] getComponents() {
		ItemStack[] components = new ItemStack[9];
		for (int i = SLOT_OUTPUT + 1; i < 12; i++) {
			components[i - SLOT_OUTPUT - 1] = inv.getStackInSlot(i);
		}
		return components;
	}

	private boolean containsComponents(IIntegrationRecipe recipe) {
		if (recipe == null) {
			return false;
		}

		ItemStack[] components = recipe.getComponents();
		if (components == null || components.length == 0) {
			return true;
		}

		for (ItemStack stack : components) {
			int found = 0;
			for (int i = SLOT_OUTPUT + 1; i < 12; i++) {
				ItemStack stack1 = inv.getStackInSlot(i);

				if (stack1 != null) {
					if (StackHelper.isMatchingItem(stack, stack1, true, false)) {
						found += stack1.stackSize;
					}
				}
			}
			if (found == 0) {
				return false;
			}
		}

		return true;
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (worldObj.isRemote) {
			return;
		}

		tick++;
		if (tick % CYCLE_LENGTH != 0) {
			return;
		}

		canCraft = false;

		currentRecipe = findMatchingRecipe();

		if (currentRecipe == null) {
			setEnergy(0);
			return;
		}

		ItemStack inputA = inv.getStackInSlot(SLOT_INPUT_A);
		ItemStack inputB = inv.getStackInSlot(SLOT_INPUT_B);
		ItemStack[] components = getComponents();
		ItemStack output = currentRecipe.getOutputForInputs(inputA, inputB, components);
		invRecipeOutput.setInventorySlotContents(0, output);

		if (!isRoomForOutput(output)) {
			setEnergy(0);
			return;
		}

		canCraft = true;

		if (getEnergy() >= currentRecipe.getEnergyCost() && lastMode != ActionMachineControl.Mode.Off) {
			setEnergy(0);
			inv.decrStackSize(SLOT_INPUT_A, 1);
			inv.decrStackSize(SLOT_INPUT_B, 1);

			// For each required component, loop through the component inventory
			for (ItemStack stack : currentRecipe.getComponents()) {
				for (int i = SLOT_OUTPUT + 1; i < 12; i++) {
					ItemStack stack1 = inv.getStackInSlot(i);

					if (stack1 != null) {
						if (StackHelper.isMatchingItem(stack, stack1, true, false)) {
							inv.decrStackSize(i, 1);
							break;
						}
					}
				}
			}

			ITransactor trans = Transactor.getTransactorFor(invOutput);
			trans.add(output, ForgeDirection.UP, true);
		}
	}

	private IIntegrationRecipe findMatchingRecipe() {
		ItemStack inputA = inv.getStackInSlot(SLOT_INPUT_A);
		ItemStack inputB = inv.getStackInSlot(SLOT_INPUT_B);

		for (IIntegrationRecipe recipe : BuildcraftRecipes.integrationTable.getRecipes()) {
			if (recipe.isValidInputA(inputA) && recipe.isValidInputB(inputB) && containsComponents(recipe)) {
				return recipe;
			}
		}
		return null;
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
	public double getRequiredEnergy() {
		if (currentRecipe != null) {
			return currentRecipe.getEnergyCost();
		}
		return 0.0;
	}

	@Override
	public boolean canCraft() {
		return canCraft && isActive();
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		switch (slot) {
			case SLOT_INPUT_A:
				return isValidInputA(stack);
			case SLOT_INPUT_B:
				return isValidInputB(stack);
		}
		return false;
	}

	private boolean isValidInputA(ItemStack stack) {
		ItemStack inputB = inv.getStackInSlot(SLOT_INPUT_B);
		for (IIntegrationRecipe recipe : BuildcraftRecipes.integrationTable.getRecipes()) {
			if (recipe.isValidInputA(stack) && (inputB == null || recipe.isValidInputB(inputB))) {
				return true;
			}
		}
		return false;
	}

	private boolean isValidInputB(ItemStack stack) {
		ItemStack inputA = inv.getStackInSlot(SLOT_INPUT_A);
		for (IIntegrationRecipe recipe : BuildcraftRecipes.integrationTable.getRecipes()) {
			if (recipe.isValidInputB(stack) && (inputA == null || recipe.isValidInputA(inputA))) {
				return true;
			}
		}
		return false;
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
		return slot == SLOT_OUTPUT;
	}

	@Override
	public int getSizeInventory() {
		return 12;
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
    public boolean isActive() {
        return currentRecipe != null && super.isActive();
    }
}
