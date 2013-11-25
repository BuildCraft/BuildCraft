/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import buildcraft.api.core.Position;
import java.util.LinkedList;
import net.minecraftforge.common.ForgeDirection;

/**
 * @deprecated This has been replaced by the Pipe Event system.
 */
@Deprecated
public interface IPipeTransportItemsHook {

	public LinkedList<ForgeDirection> filterPossibleMovements(LinkedList<ForgeDirection> possibleOrientations, Position pos, TravelingItem item);

	public void entityEntered(TravelingItem item, ForgeDirection orientation);

	public void readjustSpeed(TravelingItem item);
}
