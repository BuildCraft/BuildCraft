/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.builders;

import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.WorldEvent;

public class EventHandlerBuilders {

	@ForgeSubscribe
	public void handleWorldLoad(WorldEvent.Load event) {
		//When a world loads clean the list of available markers
		TilePathMarker.clearAvailableMarkersList();
	}
	
}
