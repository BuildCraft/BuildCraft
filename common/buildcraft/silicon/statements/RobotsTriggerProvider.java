/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon.statements;

import java.util.Collection;
import java.util.LinkedList;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import buildcraft.BuildCraftSilicon;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.statements.ITriggerProvider;
import buildcraft.transport.TileGenericPipe;

public class RobotsTriggerProvider implements ITriggerProvider {

	@Override
	public Collection<ITriggerInternal> getInternalTriggers(IStatementContainer container) {
		LinkedList<ITriggerInternal> result = new LinkedList<ITriggerInternal>();
		TileEntity tile = container.getTile();
		
		if (!(tile instanceof TileGenericPipe)) {
			return result;
		}

		boolean stationFound = false;

		for (EnumFacing dir : EnumFacing.values()) {
			if (((TileGenericPipe) tile).getStation(dir) != null) {
				stationFound = true;
				break;
			}
		}

		if (!stationFound) {
			return result;
		}

		result.add(BuildCraftSilicon.triggerRobotSleep);

		return result;
	}

	@Override
	public Collection<ITriggerExternal> getExternalTriggers(EnumFacing side, TileEntity tile) {
		return null;
	}

}
