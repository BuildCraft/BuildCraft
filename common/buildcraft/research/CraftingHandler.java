package buildcraft.research;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import eureka.api.EurekaKnowledge;

/**
 * Copyright (c) 2014, AEnterprise
 * http://buildcraftadditions.wordpress.com/
 * Buildcraft Additions is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://buildcraftadditions.wordpress.com/wiki/licensing-stuff/
 */
public class CraftingHandler {

	@SubscribeEvent
	public void onCrafting(PlayerEvent.ItemCraftedEvent event) {
		EurekaKnowledge.makeProgress(event.player, "autoWorkbench");
	}
}
