package buildcraft.core;

import java.util.EnumSet;

import net.minecraft.entity.player.EntityPlayer;
import buildcraft.core.proxy.CoreProxy;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class TickHandlerCoreClient implements ITickHandler {

	private boolean nagged;

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {

		if (nagged)
			return;

		EntityPlayer player = (EntityPlayer) tickData[0];

		// if(!Config.disableVersionCheck) {

		if (Version.needsUpdateNoticeAndMarkAsSeen()) {
			player.sendChatToPlayer(String.format("\u00A7cNew version of BuildCraft available: %s for Minecraft %s", Version.getRecommendedVersion(),
					CoreProxy.proxy.getMinecraftVersion()));
			for (String updateLine : Version.getChangelog()) {
				player.sendChatToPlayer("\u00A79" + updateLine);
			}
			player.sendChatToPlayer("\u00A7cThis message only displays once. Type '/buildcraft version' if you want to see it again.");
		}

		// }

		nagged = true;
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.PLAYER);
	}

	@Override
	public String getLabel() {
		return "BuildCraft - Player update tick";
	}

}
