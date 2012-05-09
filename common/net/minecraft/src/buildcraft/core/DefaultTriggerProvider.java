package net.minecraft.src.buildcraft.core;

import java.util.LinkedList;

import net.minecraft.src.Block;
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.IInventory;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.api.ILiquidContainer;
import net.minecraft.src.buildcraft.api.IOverrideDefaultTriggers;
import net.minecraft.src.buildcraft.api.IPipe;
import net.minecraft.src.buildcraft.api.ITriggerProvider;
import net.minecraft.src.buildcraft.api.Trigger;

public class DefaultTriggerProvider implements ITriggerProvider {

	@Override
	public LinkedList<Trigger> getNeighborTriggers(Block block, TileEntity tile) {
		if (tile instanceof IOverrideDefaultTriggers)
			return ((IOverrideDefaultTriggers) tile).getTriggers();

		LinkedList <Trigger> res = new LinkedList<Trigger>();

		if (tile instanceof IInventory && ((IInventory) tile).getSizeInventory() > 0) {
			res.add(BuildCraftCore.triggerEmptyInventory);
			res.add(BuildCraftCore.triggerContainsInventory);
			res.add(BuildCraftCore.triggerSpaceInventory);
			res.add(BuildCraftCore.triggerFullInventory);
		}

		if (tile instanceof ILiquidContainer && ((ILiquidContainer) tile).getLiquidSlots().length > 0) {
			res.add(BuildCraftCore.triggerEmptyLiquid);
			res.add(BuildCraftCore.triggerContainsLiquid);
			res.add(BuildCraftCore.triggerSpaceLiquid);
			res.add(BuildCraftCore.triggerFullLiquid);
		}

		if (tile instanceof IMachine) {
			res.add(BuildCraftCore.triggerMachineActive);
			res.add(BuildCraftCore.triggerMachineInactive);
		}


		if (block != null && block.canProvidePower()) {
			res.add(BuildCraftCore.triggerRedstoneActive);
			res.add(BuildCraftCore.triggerRedstoneInactive);
		}

		return res;
	}

	@Override
	public LinkedList<Trigger> getPipeTriggers(IPipe pipe) {
		return null;
	}

}
