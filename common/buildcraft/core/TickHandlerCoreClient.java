/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import buildcraft.core.proxy.CoreProxy;

import java.util.EnumSet;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

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
			player.addChatMessage(new ChatComponentText(
					String.format(
							"\u00A7cNew version of BuildCraft available: %s for Minecraft %s",
							Version.getRecommendedVersion(),
							CoreProxy.proxy.getMinecraftVersion())));
			for (String updateLine : Version.getChangelog()) {
				player.addChatMessage(new ChatComponentText("\u00A79" + updateLine));
			}
			player.addChatMessage(new ChatComponentText(
					"\u00A7cThis message only displays once. Type '/buildcraft version' if you want to see it again."));
		}

		// }

		nagged = true;
	}
}
