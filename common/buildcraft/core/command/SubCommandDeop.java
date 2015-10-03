package buildcraft.core.command;

import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import buildcraft.BuildCraftCore;
import buildcraft.core.lib.commands.CommandHelpers;
import buildcraft.core.lib.commands.SubCommand;

public class SubCommandDeop extends SubCommand {
	public SubCommandDeop() {
		super("deop");
		setPermLevel(PermLevel.SERVER_ADMIN);
	}

	@Override
	public void processSubCommand(ICommandSender sender, String[] args) {
		MinecraftServer.getServer().getConfigurationManager().func_152610_b(BuildCraftCore.gameProfile);
		CommandHelpers.sendLocalizedChatMessage(sender, "commands.deop.success", "[BuildCraft]");
	}
}
