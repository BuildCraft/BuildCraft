/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraftforge.event.world.WorldEvent;

public class EventHandlerBuilders {

	@SubscribeEvent
	public void handleWorldLoad(WorldEvent.Load event) {
		// Temporary solution
		// Please remove the world Load event when world Unload event gets implimented
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
			TilePathMarker.clearAvailableMarkersList(event.world);
		}
	}

	@SubscribeEvent
	public void handleWorldUnload(WorldEvent.Unload event) {
		// When a world unloads clean from the list of available markers the ones
		// that were on the unloaded world
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
			TilePathMarker.clearAvailableMarkersList(event.world);
		}
	}

}
