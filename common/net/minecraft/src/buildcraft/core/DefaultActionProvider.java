package net.minecraft.src.buildcraft.core;

import java.util.LinkedList;

import net.minecraft.src.Block;
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.api.Action;
import net.minecraft.src.buildcraft.api.IActionProvider;

public class DefaultActionProvider implements IActionProvider {

	@Override
	public LinkedList<Action> getNeighborActions(Block block, TileEntity tile) {
		LinkedList <Action> res = new LinkedList<Action>();

		res.add(BuildCraftCore.actionRedstone);

		if (tile instanceof IMachine && ((IMachine) tile).allowActions()) {
			res.add(BuildCraftCore.actionOn);
			res.add(BuildCraftCore.actionOff);
			res.add(BuildCraftCore.actionLoop);
		}

		return res;
	}

}
