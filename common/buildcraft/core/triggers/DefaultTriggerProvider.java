package buildcraft.core.triggers;

import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.ITankContainer;
import buildcraft.BuildCraftCore;
import buildcraft.api.gates.IOverrideDefaultTriggers;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.ITriggerProvider;
import buildcraft.api.transport.IPipe;
import buildcraft.core.IMachine;

public class DefaultTriggerProvider implements ITriggerProvider {

	@Override
	public LinkedList<ITrigger> getNeighborTriggers(Block block, TileEntity tile) {
		if (tile instanceof IOverrideDefaultTriggers) {
			return ((IOverrideDefaultTriggers) tile).getTriggers();
		}

		LinkedList<ITrigger> res = new LinkedList<ITrigger>();

		if (tile instanceof IInventory && ((IInventory) tile).getSizeInventory() > 0) {
			res.add(BuildCraftCore.triggerEmptyInventory);
			res.add(BuildCraftCore.triggerContainsInventory);
			res.add(BuildCraftCore.triggerSpaceInventory);
			res.add(BuildCraftCore.triggerFullInventory);
		}

		if (tile instanceof ITankContainer) {
			ILiquidTank[] tanks = ((ITankContainer) tile).getTanks(ForgeDirection.UNKNOWN);
			if (tanks != null && tanks.length > 0) {
				res.add(BuildCraftCore.triggerEmptyLiquid);
				res.add(BuildCraftCore.triggerContainsLiquid);
				res.add(BuildCraftCore.triggerSpaceLiquid);
				res.add(BuildCraftCore.triggerFullLiquid);
			}
		}

		if (tile instanceof IMachine) {
			res.add(BuildCraftCore.triggerMachineActive);
			res.add(BuildCraftCore.triggerMachineInactive);
		}

		if (block != null && block.canProvidePower()) {
			// res.add(BuildCraftCore.triggerRedstoneActive);
			// res.add(BuildCraftCore.triggerRedstoneInactive);
		}

		return res;
	}

	@Override
	public LinkedList<ITrigger> getPipeTriggers(IPipe pipe) {
		return null;
	}
}
