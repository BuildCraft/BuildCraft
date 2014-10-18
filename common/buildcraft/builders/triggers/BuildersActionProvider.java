/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.triggers;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import buildcraft.api.filler.FillerManager;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.gates.IAction;
import buildcraft.api.gates.IActionProvider;
import buildcraft.api.transport.IPipeTile;
import buildcraft.builders.TileFiller;
import buildcraft.core.builders.patterns.FillerPattern;

public class BuildersActionProvider implements IActionProvider {
	private final HashMap<String, ActionFiller> actionMap = new HashMap<String, ActionFiller>();
	
	@Override
	public Collection<IAction> getPipeActions(IPipeTile pipe) {
		return null;
	}

	@Override
	public Collection<IAction> getNeighborActions(Block block, TileEntity tile) {
		LinkedList<IAction> actions = new LinkedList<IAction>();
		if (tile instanceof TileFiller) {
			for(IFillerPattern p : FillerManager.registry.getPatterns()) {
				if (p instanceof FillerPattern) {
					if (!actionMap.containsKey(p.getUniqueTag())) {
						actionMap.put(p.getUniqueTag(), new ActionFiller((FillerPattern) p));
					}
					actions.add(actionMap.get(p.getUniqueTag()));
				}
			}
		}
		return actions;
	}
}
