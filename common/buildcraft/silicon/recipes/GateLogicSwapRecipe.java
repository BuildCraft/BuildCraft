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
import buildcraft.api.recipes.IIntegrationRecipeManager.IIntegrationRecipe;
import buildcraft.core.inventory.StackHelper;
import buildcraft.silicon.ItemRedstoneChipset;
import buildcraft.transport.gates.GateDefinition.GateLogic;
import buildcraft.transport.gates.GateDefinition.GateMaterial;
import buildcraft.transport.gates.ItemGate;

public class GateLogicSwapRecipe implements IIntegrationRecipe {

	private final GateMaterial material;
	private final GateLogic logicIn, logicOut;
	private final ItemStack chipset;
	private final ItemStack[] exampleA;
	private final ItemStack[] exampleB;

	public GateLogicSwapRecipe(GateMaterial material, GateLogic logicIn, GateLogic logicOut) {
		this.material = material;
		this.logicIn = logicIn;
		this.logicOut = logicOut;
		this.chipset = ItemRedstoneChipset.Chipset.RED.getStack();
		exampleA = new ItemStack[]{ItemGate.makeGateItem(material, logicIn)};
		exampleB = new ItemStack[]{chipset};
	}

	@Override
	public double getEnergyCost() {
		return 2000;
	}

	@Override
	public boolean isValidInputA(ItemStack inputA) {
		if (inputA == null) {
			return false;
		}
		if (!(inputA.getItem() instanceof ItemGate)) {
			return false;
		}
		if (ItemGate.getMaterial(inputA) != material) {
			return false;
		}
		if (ItemGate.getLogic(inputA) != logicIn) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isValidInputB(ItemStack inputB) {
		return StackHelper.isMatchingItem(inputB, chipset);
	}

	@Override
	public ItemStack getOutputForInputs(ItemStack inputA, ItemStack inputB, ItemStack[] components) {
		if (!isValidInputA(inputA)) {
			return null;
		}
		if (!isValidInputB(inputB)) {
			return null;
		}
		ItemStack output = inputA.copy();
		output.stackSize = 1;
		ItemGate.setLogic(output, logicOut);
		return output;
	}

	@Override
	public ItemStack[] getComponents() {
		return new ItemStack[0];
	}

	@Override
	public ItemStack[] getExampleInputsA() {
		return exampleA;
	}

	@Override
	public ItemStack[] getExampleInputsB() {
		return exampleB;
	}
}
