/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.recipes.CraftingResult;
import buildcraft.api.recipes.IFlexibleCrafter;
import buildcraft.api.recipes.IFlexibleRecipe;
import buildcraft.api.recipes.IIntegrationRecipe;
import buildcraft.core.lib.inventory.ITransactor;
import buildcraft.core.lib.inventory.InventoryMapper;
import buildcraft.core.lib.inventory.SimpleInventory;
import buildcraft.core.lib.inventory.StackHelper;
import buildcraft.core.lib.inventory.Transactor;
import buildcraft.core.lib.utils.StringUtils;

public class TileIntegrationTable extends TileLaserTableBase implements IFlexibleCrafter {

	public static final int SLOT_INPUT_A = 0;
	public static final int SLOT_INPUT_B = 1;
	public static final int SLOT_OUTPUT = 2;
	private static final int CYCLE_LENGTH = 32;
	private int tick = 0;
	private SimpleInventory invRecipeOutput = new SimpleInventory(1, "integrationOutput", 64);
	private InventoryMapper invOutput = new InventoryMapper(inv, SLOT_OUTPUT, 1, false);
	private IFlexibleRecipe<ItemStack> activeRecipe;
	private CraftingResult<ItemStack> craftingPreview;

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

		if (activeRecipe == null || craftingPreview == null) {
			setEnergy(0);
			return;
		}

		tick++;
		if (tick % CYCLE_LENGTH != 0) {
			return;
		}

		if (!isRoomForOutput(craftingPreview.crafted)) {
			setEnergy(0);
			return;
		}

		if (getEnergy() >= craftingPreview.energyCost) {
			setEnergy(0);
			craftingPreview = null;

			CraftingResult<ItemStack> craftResult = activeRecipe.craft(this, false);

			if (craftResult != null) {
				ItemStack result = craftResult.crafted.copy();

				ITransactor trans = Transactor.getTransactorFor(invOutput);
				trans.add(result, ForgeDirection.UP, true);
			}
		}
	}

	private void setNewActiveRecipe(ItemStack inputA, ItemStack inputB, ItemStack[] components) {
		craftingPreview = null;

		for (IIntegrationRecipe recipe : BuildcraftRecipeRegistry.integrationTable.getRecipes()) {
			if (recipe.isValidInputA(inputA) && recipe.isValidInputB(inputB)) {
				craftingPreview = recipe.craft(this, true);

				if (craftingPreview != null) {
					activeRecipe = recipe;
					break;
				}
			}
		}
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
	public int getRequiredEnergy() {
		if (craftingPreview != null) {
			return craftingPreview.energyCost;
		} else {
			return 0;
		}
	}

	@Override
	public boolean canCraft() {
		return hasWork();
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
		for (IIntegrationRecipe recipe : BuildcraftRecipeRegistry.integrationTable.getRecipes()) {
			if (recipe.isValidInputA(stack) && (inputB == null || recipe.isValidInputB(inputB))) {
				return true;
			}
		}
		return false;
	}

	private boolean isValidInputB(ItemStack stack) {
		ItemStack inputA = inv.getStackInSlot(SLOT_INPUT_A);

		for (IIntegrationRecipe recipe : BuildcraftRecipeRegistry.integrationTable.getRecipes()) {
			if (recipe.isValidInputB(stack) && (inputA == null || recipe.isValidInputA(inputA))) {
				return true;
			}
		}

		return false;
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
    public boolean hasWork() {
		return craftingPreview != null;
    }

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		super.setInventorySlotContents(slot, stack);

		updateRecipe();
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount) {
		ItemStack result = super.decrStackSize(slot, amount);

		updateRecipe();

		return result;
	}

	private void updateRecipe() {
		ItemStack inputA = inv.getStackInSlot(SLOT_INPUT_A);
		ItemStack inputB = inv.getStackInSlot(SLOT_INPUT_B);
		ItemStack[] components = getComponents();
		setNewActiveRecipe(inputA, inputB, components);

		if (craftingPreview != null) {
			invRecipeOutput.setInventorySlotContents(0, craftingPreview.crafted);
		} else {
			invRecipeOutput.setInventorySlotContents(0, null);
		}
	}

	@Override
	public int getCraftingItemStackSize() {
		return getSizeInventory() - 3;
	}

	@Override
	public ItemStack getCraftingItemStack(int slotid) {
		return getStackInSlot(slotid + 3);
	}

	@Override
	public ItemStack decrCraftingItemStack(int slotid, int val) {
		return decrStackSize(slotid + 3, val);
	}

	@Override
	public FluidStack getCraftingFluidStack(int tankid) {
		return null;
	}

	@Override
	public FluidStack decrCraftingFluidStack(int tankid, int val) {
		return null;
	}

	@Override
	public int getCraftingFluidStackSize() {
		return 0;
	}

}
