/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import java.util.LinkedList;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.Position;

/**
 * @deprecated This has been replaced by the Pipe Event system.
 */
@Deprecated
public interface IPipeTransportItemsHook {
	LinkedList<ForgeDirection> filterPossibleMovements(LinkedList<ForgeDirection> possibleOrientations, Position pos,
			TravelingItem item);

	void entityEntered(TravelingItem item, ForgeDirection orientation);

	void readjustSpeed(TravelingItem item);
}
