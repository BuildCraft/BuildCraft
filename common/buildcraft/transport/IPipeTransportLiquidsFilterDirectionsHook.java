/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.transport;

import buildcraft.api.core.Position;
import buildcraft.api.transport.IPipedItem;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.LiquidStack;

import java.util.LinkedList;
import java.util.List;

public interface IPipeTransportLiquidsFilterDirectionsHook {

	public List<ForgeDirection> filterPossibleMovements(List<ForgeDirection> possibleOrientations, Position pos, LiquidStack resource);

}
