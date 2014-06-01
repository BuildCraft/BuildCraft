/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon.recipes;

import net.minecraft.item.ItemStack;
import buildcraft.api.gates.IGateExpansion;
import buildcraft.api.recipes.IIntegrationRecipe;
import buildcraft.core.inventory.StackHelper;
import buildcraft.transport.gates.ItemGate;

public class GateExpansionRecipe implements IIntegrationRecipe {

	private final IGateExpansion expansion;
	private final ItemStack chipset;

	public GateExpansionRecipe(IGateExpansion expansion, ItemStack chipset) {
		this.expansion = expansion;
		this.chipset = chipset.copy();
	}

	@Override
	public boolean isValidInputA(ItemStack inputA) {
		if (inputA == null) {
			return false;
		}
		if (!(inputA.getItem() instanceof ItemGate)) {
			return false;
		}
		return !ItemGate.hasGateExpansion(inputA, expansion);
	}

	@Override
	public boolean isValidInputB(ItemStack inputB) {
		return StackHelper.isMatchingItem(inputB, chipset);
	}

	@Override
	public IntegrationResult integrate(ItemStack inputA, ItemStack inputB, ItemStack[] components) {
		ItemStack output = inputA.copy();
		output.stackSize = 1;
		ItemGate.addGateExpansion(output, expansion);
		return IntegrationResult.create(10000, output);
	}
}
