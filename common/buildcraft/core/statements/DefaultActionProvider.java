/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.statements;

import java.util.Collection;
import java.util.LinkedList;

import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftCore;
import buildcraft.api.core.BCLog;
import buildcraft.api.statements.IActionExternal;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IActionProvider;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.containers.IRedstoneStatementContainer;
import buildcraft.api.tiles.IControllable;

public class DefaultActionProvider implements IActionProvider {

	@Override
	public Collection<IActionInternal> getInternalActions(IStatementContainer container) {
		LinkedList<IActionInternal> res = new LinkedList<IActionInternal>();

		if (container instanceof IRedstoneStatementContainer) {
			res.add(BuildCraftCore.actionRedstone);
		}

		return res;
	}

	@Override
	public Collection<IActionExternal> getExternalActions(ForgeDirection side, TileEntity tile) {
		LinkedList<IActionExternal> res = new LinkedList<IActionExternal>();

		try {
			if (tile instanceof IControllable) {
				for (IControllable.Mode mode : IControllable.Mode.values()) {
					if (mode != IControllable.Mode.Unknown &&
							((IControllable) tile).acceptsControlMode(mode)) {
						res.add(BuildCraftCore.actionControl[mode.ordinal()]);
					}
				}
			}
		} catch (Throwable error) {
			BCLog.logger.error("Outdated API detected, please update your mods!");
		}

		return res;
	}
}
