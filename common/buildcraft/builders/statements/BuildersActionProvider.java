/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.statements;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.filler.FillerManager;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.statements.IActionExternal;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IActionProvider;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.builders.TileFiller;
import buildcraft.core.builders.patterns.FillerPattern;

public class BuildersActionProvider implements IActionProvider {
	private final HashMap<String, ActionFiller> actionMap = new HashMap<String, ActionFiller>();

	@Override
	public Collection<IActionInternal> getInternalActions(IStatementContainer container) {
		return null;
	}

	@Override
	public Collection<IActionExternal> getExternalActions(ForgeDirection side, TileEntity tile) {
		LinkedList<IActionExternal> actions = new LinkedList<IActionExternal>();
		if (tile instanceof TileFiller) {
			for (IFillerPattern p : FillerManager.registry.getPatterns()) {
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
