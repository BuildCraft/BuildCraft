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
import buildcraft.core.utils.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public class TileIntegrationTable extends TileLaserTableBase implements ISidedInventory {

	private static final int CYCLE_LENGTH = 32;
	private static final int SLOT_INPUT_A = 0;
	private static final int SLOT_INPUT_B = 1;
	private static final int SLOT_OUTPUT = 2;
	private static final int[] SLOTS = Utils.createSlotArray(0, 3);
	private int tick = 0;
	private SimpleInventory inv = new SimpleInventory(3, "integration", 64);
	private InventoryMapper invOutput = new InventoryMapper(inv, SLOT_OUTPUT, 1, false);
	private IIntegrationRecipe currentRecipe;

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (worldObj.isRemote)
			return;

		tick++;
		if (tick % CYCLE_LENGTH != 0)
			return;

		currentRecipe = findMatchingRecipe();

		if (currentRecipe == null)
			return;

		if (getEnergy() >= currentRecipe.getEnergyCost())
			tryCraftItem();
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

	private void tryCraftItem() {
		ItemStack inputA = inv.getStackInSlot(SLOT_INPUT_A);
		ItemStack inputB = inv.getStackInSlot(SLOT_INPUT_B);
		ItemStack output = currentRecipe.getOutputForInputs(inputA, inputB);

		if (isRoomForOutput(output)) {
			setEnergy(0);
			inv.decrStackSize(SLOT_INPUT_A, 1);
			inv.decrStackSize(SLOT_INPUT_B, 1);
			ITransactor trans = Transactor.getTransactorFor(invOutput);
			trans.add(output, ForgeDirection.UP, true);
		}

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
		return 0;
	}

	@Override
	public boolean canCraft() {
		return currentRecipe != null;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		inv.writeToNBT(nbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		inv.readFromNBT(nbt);
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
		for (IIntegrationRecipe recipe : BuildcraftRecipes.integrationTable.getRecipes()) {
			if (recipe.isValidInputA(stack))
				return true;
		}
		return false;
	}

	private boolean isValidInputB(ItemStack stack) {
		for (IIntegrationRecipe recipe : BuildcraftRecipes.integrationTable.getRecipes()) {
			if (recipe.isValidInputB(stack))
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
		return inv.getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return inv.getStackInSlot(slot);
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount) {
		return inv.decrStackSize(slot, amount);
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		return null;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		inv.setInventorySlotContents(slot, stack);
	}

	@Override
	public String getInvName() {
		return "Integration Table";
	}

	@Override
	public int getInventoryStackLimit() {
		return inv.getInventoryStackLimit();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this && !isInvalid();
	}

	@Override
	public void openChest() {
	}

	@Override
	public void closeChest() {
	}
}
