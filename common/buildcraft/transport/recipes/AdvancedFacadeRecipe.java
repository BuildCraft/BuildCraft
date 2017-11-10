/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.recipes;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.JavaTools;
import buildcraft.api.facades.FacadeType;
import buildcraft.api.facades.IFacadeItem;
import buildcraft.api.transport.PipeWire;
import buildcraft.core.recipes.IntegrationRecipeBC;
import buildcraft.transport.ItemFacade;
import buildcraft.transport.ItemPipeWire;

public class AdvancedFacadeRecipe extends IntegrationRecipeBC {
	public AdvancedFacadeRecipe() {
		super(25000, 2);
	}

	@Override
	public List<ItemStack> generateExampleInput() {
		return ItemFacade.allFacades;
	}

	@Override
	public List<ItemStack> generateExampleOutput() {
		return ItemFacade.allFacades;
	}

	@Override
	public List<List<ItemStack>> generateExampleExpansions() {
		List<List<ItemStack>> list = new ArrayList<List<ItemStack>>();
		list.add(ItemFacade.allFacades);
		List<ItemStack> pipeWires = new ArrayList<ItemStack>();
		for (PipeWire wire : PipeWire.values()) {
			pipeWires.add(wire.getStack());
		}
		list.add(pipeWires);
		return list;
	}

	@Override
	public boolean isValidInput(ItemStack input) {
		return input.getItem() instanceof ItemFacade;
	}

	@Override
	public boolean isValidExpansion(ItemStack input, ItemStack expansion) {
		return (expansion.getItem() instanceof ItemFacade &&
				((IFacadeItem) expansion.getItem()).getFacadeType(expansion) == FacadeType.Basic) ||
				expansion.getItem() == BuildCraftTransport.plugItem ||
				expansion.getItem() == BuildCraftTransport.pipeWire;
	}

	@Override
	public ItemStack craft(ItemStack input, List<ItemStack> expansions, boolean preview) {
		PipeWire wire = null;
		ItemStack facade = null;

		for (ItemStack stack : expansions) {
			if (wire == null && stack.getItem() instanceof ItemPipeWire) {
				wire = PipeWire.fromOrdinal(stack.getItemDamage());
				if (!preview) {
					stack.stackSize--;
				}
			} else if (facade == null && (stack.getItem() instanceof ItemFacade || stack.getItem() == BuildCraftTransport.pipeWire)) {
				facade = stack;
				if (!preview) {
					stack.stackSize--;
				}
			}
		}

		if (wire != null && facade != null) {
			ItemFacade.FacadeState[] states = ItemFacade.getFacadeStates(input);
			ItemFacade.FacadeState additionalState;

			if (facade.getItem() == BuildCraftTransport.plugItem) {
				additionalState = ItemFacade.FacadeState.createTransparent(wire);
			} else {
				additionalState = ItemFacade.getFacadeStates(facade)[0];
				additionalState = new ItemFacade.FacadeState(additionalState.block, additionalState.metadata, wire, additionalState.hollow);
			}

			// if in states array exists state with the same wire just override it
			for (int i = 0; i < states.length; i++) {
				if (states[i].wire == wire) {
					states[i] = additionalState;
					return ItemFacade.getFacade(states);
				}
			}

			return ItemFacade.getFacade(JavaTools.concat(states,
					new ItemFacade.FacadeState[]{additionalState}));
		} else {
			return null;
		}
	}
}
