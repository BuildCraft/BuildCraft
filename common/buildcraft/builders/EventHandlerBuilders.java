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
		
		//For some reason, when loading a world this gets called 3 times from the
		//server and one from the client. We don't want the client to clear the
		//list because it happens after the initializations and therefore it re-
		//moves the loaded path markers.
		if (!event.world.getClass().equals(net.minecraft.src.WorldClient.class))
			TilePathMarker.clearAvailableMarkersList();
	}
	
}
