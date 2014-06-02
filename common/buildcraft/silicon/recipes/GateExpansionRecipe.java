/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon.recipes;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.IFluidHandler;

import buildcraft.BuildCraftTransport;
import buildcraft.api.gates.IGateExpansion;
import buildcraft.api.recipes.CraftingResult;
import buildcraft.api.recipes.IIntegrationRecipeFactory;
import buildcraft.core.inventory.StackHelper;
import buildcraft.core.recipes.FlexibleRecipe;
import buildcraft.silicon.TileIntegrationTable;
import buildcraft.transport.gates.ItemGate;

public class GateExpansionRecipe extends FlexibleRecipe implements IIntegrationRecipeFactory {

	private final IGateExpansion expansion;
	private final ItemStack chipset;

	public GateExpansionRecipe(String id, IGateExpansion expansion, ItemStack chipset) {
		this.expansion = expansion;
		this.chipset = chipset.copy();

		setContents(id, BuildCraftTransport.pipeGate, 10000, BuildCraftTransport.pipeGate, chipset);
	}

	@Override
	public boolean isValidInputA(ItemStack inputA) {
		if (inputA == null) {
			return false;
		} else if (!(inputA.getItem() instanceof ItemGate)) {
			return false;
		} else {
			return !ItemGate.hasGateExpansion(inputA, expansion);
		}
	}

	@Override
	public boolean isValidInputB(ItemStack inputB) {
		return StackHelper.isMatchingItem(inputB, chipset);
	}

	@Override
	public CraftingResult craft(IInventory items, IFluidHandler fluids) {
		ItemStack inputA = items.getStackInSlot(TileIntegrationTable.SLOT_INPUT_A);

		if (inputA == null) {
			return null;
		}

		inputA = inputA.copy();

		CraftingResult result = super.craft(items, fluids);

		if (result == null) {
			return null;
		}

		ItemStack output = inputA;

		output.stackSize = 1;
		ItemGate.addGateExpansion(output, expansion);

		result.crafted = output;

		return result;
	}
}
