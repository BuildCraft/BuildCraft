package buildcraft.core;

import java.util.LinkedList;

import buildcraft.BuildCraftCore;
import buildcraft.api.gates.IAction;
import buildcraft.api.gates.IActionProvider;

import net.minecraft.src.Block;
import net.minecraft.src.TileEntity;

public class DefaultActionProvider implements IActionProvider {

	@Override
	public LinkedList<IAction> getNeighborActions(Block block, TileEntity tile) {
		LinkedList<IAction> res = new LinkedList<IAction>();

		res.add(BuildCraftCore.actionRedstone);

		if (tile instanceof IMachine && ((IMachine) tile).allowActions()) {
			res.add(BuildCraftCore.actionOn);
			res.add(BuildCraftCore.actionOff);
			res.add(BuildCraftCore.actionLoop);
		}

		return res;
	}

}
