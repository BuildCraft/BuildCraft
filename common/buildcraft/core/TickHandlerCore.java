/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
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

public class TickHandlerCore {
	private boolean nagged;

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void checkUpToDate(PlayerTickEvent evt) {
		if (nagged) {
			return;
		}

		EntityPlayer player = evt.player;

		if (Version.needsUpdateNoticeAndMarkAsSeen()) {
			player.addChatMessage(new ChatComponentTranslation("bc_update.new_version",
					Version.getRecommendedVersion(),
					CoreProxy.proxy.getMinecraftVersion()));
			player.addChatMessage(new ChatComponentTranslation("bc_update.download"));
			player.addChatMessage(new ChatComponentTranslation("bc_update.once"));
			player.addChatMessage(new ChatComponentTranslation("bc_update.again"));
			player.addChatMessage(new ChatComponentTranslation("bc_update.changelog"));
		}

		nagged = true;
	}
}
