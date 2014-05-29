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

import buildcraft.api.recipes.IIntegrationRecipe;
import buildcraft.core.inventory.StackHelper;
import buildcraft.silicon.ItemRedstoneChipset;
import buildcraft.transport.gates.GateDefinition.GateLogic;
import buildcraft.transport.gates.GateDefinition.GateMaterial;
import buildcraft.transport.gates.ItemGate;

public class GateLogicSwapRecipe implements IIntegrationRecipe {
	@Override
	public boolean isValidInputA(ItemStack inputA) {
		return inputA != null && inputA.getItem() instanceof ItemGate && ItemGate.getMaterial(inputA) != GateMaterial.REDSTONE;
	}

	@Override
	public boolean isValidInputB(ItemStack inputB) {
		return StackHelper.isMatchingItem(inputB, ItemRedstoneChipset.Chipset.RED.getStack());
	}

	@Override
	public IntegrationResult integrate(ItemStack inputA, ItemStack inputB, ItemStack[] components) {
		ItemStack output = inputA.copy();
		output.stackSize = 1;
		ItemGate.setLogic(output, ItemGate.getLogic(output) == GateLogic.AND ? GateLogic.OR : GateLogic.AND);
		return IntegrationResult.create(2000, output);
	}
}
