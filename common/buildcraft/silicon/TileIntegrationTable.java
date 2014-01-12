/*
 * Copyright (c) SpaceToad, 2011-2012
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon;

import buildcraft.api.recipes.BuildcraftRecipes;
import buildcraft.api.recipes.IIntegrationRecipeManager.IIntegrationRecipe;
import buildcraft.core.inventory.ITransactor;
import buildcraft.core.inventory.InventoryMapper;
import buildcraft.core.inventory.SimpleInventory;
import buildcraft.core.inventory.StackHelper;
import buildcraft.core.inventory.Transactor;
import buildcraft.core.utils.StringUtils;
import buildcraft.core.utils.Utils;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public class TileIntegrationTable extends TileLaserTableBase implements ISidedInventory {

	public static final int SLOT_INPUT_A = 0;
	public static final int SLOT_INPUT_B = 1;
	public static final int SLOT_OUTPUT = 2;
	private static final int CYCLE_LENGTH = 32;
	private static final int[] SLOTS = Utils.createSlotArray(0, 3);
	private int tick = 0;
	private SimpleInventory invRecipeOutput = new SimpleInventory(1, "integrationOutput", 64);
	private InventoryMapper invOutput = new InventoryMapper(inv, SLOT_OUTPUT, 1, false);
	private IIntegrationRecipe currentRecipe;
	private boolean canCraft = false;

	public IInventory getRecipeOutput() {
		return invRecipeOutput;
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (worldObj.isRemote)
			return;

		tick++;
		if (tick % CYCLE_LENGTH != 0)
			return;
		
		canCraft = false;

		currentRecipe = findMatchingRecipe();

		if (currentRecipe == null) {
			setEnergy(0);
			return;
		}

		ItemStack inputA = inv.getStackInSlot(SLOT_INPUT_A);
		ItemStack inputB = inv.getStackInSlot(SLOT_INPUT_B);
		ItemStack output = currentRecipe.getOutputForInputs(inputA, inputB);
		invRecipeOutput.setInventorySlotContents(0, output);

		if (!isRoomForOutput(output)) {
			setEnergy(0);
			return;
		}

		canCraft = true;

		if (getEnergy() >= currentRecipe.getEnergyCost()) {
			setEnergy(0);
			inv.decrStackSize(SLOT_INPUT_A, 1);
			inv.decrStackSize(SLOT_INPUT_B, 1);
			ITransactor trans = Transactor.getTransactorFor(invOutput);
			trans.add(output, ForgeDirection.UP, true);
		}
	}

	private IIntegrationRecipe findMatchingRecipe() {
		ItemStack inputA = inv.getStackInSlot(SLOT_INPUT_A);
		ItemStack inputB = inv.getStackInSlot(SLOT_INPUT_B);

		for (IIntegrationRecipe recipe : BuildcraftRecipes.integrationTable.getRecipes()) {
			if (recipe.isValidInputA(inputA) && recipe.isValidInputB(inputB))
				return recipe;
		}
		return null;
	}

	private boolean isRoomForOutput(ItemStack output) {
		ItemStack existingOutput = inv.getStackInSlot(SLOT_OUTPUT);
		if (existingOutput == null)
			return true;
		if (StackHelper.instance().canStacksMerge(output, existingOutput) && output.stackSize + existingOutput.stackSize <= output.getMaxStackSize())
			return true;
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
		return canCraft;
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
			if (recipe.isValidInputA(stack) && (inputB == null || recipe.isValidInputB(inputB)))
				return true;
		}
		return false;
	}

	private boolean isValidInputB(ItemStack stack) {
		ItemStack inputA = inv.getStackInSlot(SLOT_INPUT_A);
		for (IIntegrationRecipe recipe : BuildcraftRecipes.integrationTable.getRecipes()) {
			if (recipe.isValidInputB(stack) && (inputA == null || recipe.isValidInputA(inputA)))
				return true;
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
		return 3;
	}

	@Override
	public String getInvName() {
		return StringUtils.localize("tile.integrationTableBlock");
	}
}
