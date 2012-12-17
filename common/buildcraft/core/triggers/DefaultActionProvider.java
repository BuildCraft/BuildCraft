package buildcraft.core.triggers;

import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import buildcraft.BuildCraftCore;
import buildcraft.api.gates.IAction;
import buildcraft.api.gates.IActionProvider;
import buildcraft.core.IMachine;

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
