/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.recipes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.BiMap;

import net.minecraft.item.ItemStack;

import buildcraft.api.gates.GateExpansions;
import buildcraft.api.gates.IGateExpansion;
import buildcraft.core.lib.inventory.StackHelper;
import buildcraft.core.recipes.IntegrationRecipeBC;
import buildcraft.silicon.ItemRedstoneChipset;
import buildcraft.transport.gates.GateDefinition;
import buildcraft.transport.gates.ItemGate;

public class GateExpansionRecipe extends IntegrationRecipeBC {
	private static final BiMap<IGateExpansion, ItemStack> recipes = (BiMap<IGateExpansion, ItemStack>) GateExpansions.getRecipesForPostInit();

	public GateExpansionRecipe() {
		super(25000);
	}

	@Override
	public boolean isValidInput(ItemStack input) {
		return input.getItem() instanceof ItemGate;
	}

	@Override
	public boolean isValidExpansion(ItemStack expansion) {
		if (StackHelper.isMatchingItem(ItemRedstoneChipset.Chipset.RED.getStack(), expansion, true, true)) {
			return true;
		}
		for (ItemStack s : recipes.values()) {
			if (StackHelper.isMatchingItem(s, expansion, true, true)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public List<ItemStack> generateExampleInput() {
		return Collections.unmodifiableList(ItemGate.getAllGates());
	}

	@Override
	public List<List<ItemStack>> generateExampleExpansions() {
		ArrayList<List<ItemStack>> list = new ArrayList<List<ItemStack>>();
		ArrayList<ItemStack> list2 = new ArrayList<ItemStack>();
		list2.addAll(recipes.values());
		list.add(list2);
		return list;
	}

	@Override
	public ItemStack craft(ItemStack input, List<ItemStack> expansions, boolean preview) {
		ItemStack output = input.copy();
		int expansionsAdded = 0;

		for (ItemStack chipset : expansions) {
			if (StackHelper.isMatchingItem(ItemRedstoneChipset.Chipset.RED.getStack(), chipset, true, true)) {
				ItemGate.setLogic(output, ItemGate.getLogic(output) == GateDefinition.GateLogic.AND ? GateDefinition.GateLogic.OR : GateDefinition.GateLogic.AND);
				expansionsAdded++;
				continue;
			}
			for (ItemStack expansion : recipes.values()) {
				if (StackHelper.isMatchingItem(chipset, expansion, true, true) && !ItemGate.hasGateExpansion(output, recipes.inverse().get(expansion))) {
					if (!preview) {
						chipset.stackSize--;
					}
					ItemGate.addGateExpansion(output, recipes.inverse().get(expansion));
					expansionsAdded++;
					break;
				}
			}
		}

		if (expansionsAdded > 0) {
			return output;
		} else {
			return null;
		}
	}
}
