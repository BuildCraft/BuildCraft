package buildcraft.core.command;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import buildcraft.core.Version;
import buildcraft.core.lib.commands.SubCommand;
import buildcraft.core.proxy.CoreProxy;

public class SubCommandVersion extends SubCommand {
	public SubCommandVersion() {
		super("version");
	}

	@Override
	public void processSubCommand(ICommandSender sender, String[] args) {
		sender.addChatMessage(new ChatComponentTranslation("command.buildcraft.version", Version.getVersion(),
				CoreProxy.proxy.getMinecraftVersion(), Version.getRecommendedVersion())
				.setChatStyle(new ChatStyle().setColor(Version.isOutdated() ? EnumChatFormatting.RED : EnumChatFormatting.GREEN)));

		if (Version.needsUpdateNoticeAndMarkAsSeen()) {
			sender.addChatMessage(new ChatComponentTranslation("bc_update.new_version",
					Version.getRecommendedVersion(),
					CoreProxy.proxy.getMinecraftVersion()));
			sender.addChatMessage(new ChatComponentTranslation("bc_update.download"));
			sender.addChatMessage(new ChatComponentTranslation("bc_update.changelog"));
		}
	}
}
