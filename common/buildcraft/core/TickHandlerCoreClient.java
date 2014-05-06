/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentTranslation;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import buildcraft.core.proxy.CoreProxy;

public class TickHandlerCoreClient {

	private boolean nagged;

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void tickEnd(PlayerTickEvent evt) {

		if (nagged) {
			return;
		}

		EntityPlayer player = evt.player;

		// if(!Config.disableVersionCheck) {

		if (Version.needsUpdateNoticeAndMarkAsSeen()) {
			player.addChatMessage(new ChatComponentTranslation("bc_update.new_version",
							Version.getRecommendedVersion(),
							CoreProxy.proxy.getMinecraftVersion()));
			player.addChatMessage(new ChatComponentTranslation("bc_update.download"));

			// TODD This takes too much realstate. See how to improve
			// Version.displayChangelog(player);

			player.addChatMessage(new ChatComponentTranslation("bc_update.once"));
			player.addChatMessage(new ChatComponentTranslation("bc_update.again"));
		}

		// }

		nagged = true;
	}
}
