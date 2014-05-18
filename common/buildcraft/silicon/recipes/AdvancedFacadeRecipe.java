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

import net.minecraftforge.oredict.OreDictionary;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.JavaTools;
import buildcraft.api.recipes.IIntegrationRecipeManager;
import buildcraft.api.transport.PipeWire;
import buildcraft.silicon.ItemRedstoneChipset;
import buildcraft.transport.ItemFacade;
import buildcraft.transport.ItemPipeWire;

public class AdvancedFacadeRecipe implements IIntegrationRecipeManager.IIntegrationRecipe {

	@Override
	public double getEnergyCost() {
		return 2500;
	}

	@Override
	public boolean isValidInputA(ItemStack inputA) {
		return inputA != null && inputA.getItem() instanceof ItemFacade;
	}

	@Override
	public boolean isValidInputB(ItemStack inputB) {
		return inputB != null && (inputB.getItem() instanceof ItemFacade && ItemFacade.getType(inputB) == ItemFacade.FacadeType.Basic ||
				inputB.getItem() == BuildCraftTransport.plugItem);
	}

	@Override
	public ItemStack getOutputForInputs(ItemStack inputA, ItemStack inputB, ItemStack[] components) {
		if (!isValidInputA(inputA)) {
			return null;
		}

		if (!isValidInputB(inputB)) {
			return null;
		}

		PipeWire wire = null;

		for (ItemStack stack : components) {
			if (stack != null && stack.getItem() instanceof ItemPipeWire) {
				wire = PipeWire.fromOrdinal(stack.getItemDamage());
				break;
			}
		}

		if (wire != null) {
			ItemFacade.FacadeState[] states = ItemFacade.getFacadeStates(inputA);
			ItemFacade.FacadeState additionalState;
			if (inputB.getItem() == BuildCraftTransport.plugItem) {
				additionalState = ItemFacade.FacadeState.createTransparent(wire);
			} else {
				additionalState = ItemFacade.getFacadeStates(inputB)[0];
				additionalState = ItemFacade.FacadeState.create(additionalState.block, additionalState.metadata, wire);
			}

			// if in states array exists state with the same wire just override it
			for (int i = 0; i < states.length; i++) {
				if (states[i].wire == wire) {
					states[i] = additionalState;
					return ItemFacade.getFacade(states);
				}
			}
			// otherwise concat all states into one facade
			return ItemFacade.getFacade(JavaTools.concat(states, new ItemFacade.FacadeState[] {additionalState}));
		} else {
			return null;
		}
	}

	@Override
	public ItemStack[] getComponents() {
		// Any pipe wire and redstone chipset
		return new ItemStack[] {new ItemStack(BuildCraftTransport.pipeWire, 1, OreDictionary.WILDCARD_VALUE), ItemRedstoneChipset.Chipset.RED.getStack()};
	}

	@Override
	public ItemStack[] getExampleInputsA() {
		return new ItemStack[0];
	}

	@Override
	public ItemStack[] getExampleInputsB() {
		return new ItemStack[0];
	}

}
