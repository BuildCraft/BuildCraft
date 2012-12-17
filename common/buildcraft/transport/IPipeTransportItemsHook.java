/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.transport;

import java.util.LinkedList;

import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.core.Position;
import buildcraft.api.transport.IPipedItem;

public interface IPipeTransportItemsHook {

	public LinkedList<ForgeDirection> filterPossibleMovements(LinkedList<ForgeDirection> possibleOrientations, Position pos, IPipedItem item);

	public void entityEntered(IPipedItem item, ForgeDirection orientation);

	public void readjustSpeed(IPipedItem item);
}
