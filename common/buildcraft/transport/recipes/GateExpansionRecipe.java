/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.recipes;

import net.minecraft.item.ItemStack;

import buildcraft.BuildCraftTransport;
import buildcraft.api.gates.IGateExpansion;
import buildcraft.api.recipes.CraftingResult;
import buildcraft.core.inventory.StackHelper;
import buildcraft.silicon.TileIntegrationTable;
import buildcraft.transport.gates.ItemGate;

public class GateExpansionRecipe extends IntegrationTableRecipe {

	private final IGateExpansion expansion;
	private final ItemStack chipset;

	public GateExpansionRecipe(String id, IGateExpansion expansion, ItemStack chipset) {
		this.expansion = expansion;
		this.chipset = chipset.copy();

		setContents(id, BuildCraftTransport.pipeGate, 100000, 0);
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
	public CraftingResult<ItemStack> craft(TileIntegrationTable crafter, boolean preview, ItemStack inputA,
			ItemStack inputB) {

		if (inputA == null) {
			return null;
		}

		CraftingResult<ItemStack> result = super.craft(crafter, preview, inputA, inputB);

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
