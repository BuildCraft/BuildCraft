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
import buildcraft.api.recipes.CraftingResult;
import buildcraft.api.recipes.IIntegrationRecipeFactory;
import buildcraft.core.inventory.StackHelper;
import buildcraft.core.recipes.FlexibleRecipe;
import buildcraft.silicon.ItemRedstoneChipset;
import buildcraft.silicon.TileIntegrationTable;
import buildcraft.transport.gates.GateDefinition.GateLogic;
import buildcraft.transport.gates.GateDefinition.GateMaterial;
import buildcraft.transport.gates.ItemGate;

public class GateLogicSwapRecipe extends FlexibleRecipe implements IIntegrationRecipeFactory {

	public GateLogicSwapRecipe(String id) {
		setContents(id, BuildCraftTransport.pipeGate, 2000, BuildCraftTransport.pipeGate);
	}

	@Override
	public boolean isValidInputA(ItemStack inputA) {
		return inputA != null && inputA.getItem() instanceof ItemGate && ItemGate.getMaterial(inputA) != GateMaterial.REDSTONE;
	}

	@Override
	public boolean isValidInputB(ItemStack inputB) {
		return StackHelper.isMatchingItem(inputB, ItemRedstoneChipset.Chipset.RED.getStack());
	}

	@Override
	public CraftingResult craft(IInventory items, IFluidHandler fluids) {
		ItemStack inputA = items.getStackInSlot(TileIntegrationTable.SLOT_INPUT_A).copy();

		CraftingResult result = super.craft(items, fluids);

		if (result == null) {
			return null;
		}

		ItemStack output = (ItemStack) result.crafted;

		output.stackSize = 1;
		ItemGate.setLogic(output, ItemGate.getLogic(output) == GateLogic.AND ? GateLogic.OR : GateLogic.AND);

		result.crafted = output;

		return result;
	}
}
