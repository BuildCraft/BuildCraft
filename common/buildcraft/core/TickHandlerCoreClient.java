package buildcraft.core;

import buildcraft.core.proxy.CoreProxy;

import java.util.EnumSet;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;

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
			player.addChatMessage(String.format("\u00A7cNew version of BuildCraft available: %s for Minecraft %s", Version.getRecommendedVersion(),
					CoreProxy.proxy.getMinecraftVersion()));
			for (String updateLine : Version.getChangelog()) {
				player.addChatMessage("\u00A79" + updateLine);
			}
			player.addChatMessage("\u00A7cThis message only displays once. Type '/buildcraft version' if you want to see it again.");
		}

		// }

		nagged = true;
	}
}
