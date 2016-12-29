package buildcraft.core;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import buildcraft.core.proxy.CoreProxy;

public class CommandBuildCraft extends CommandBase {

	@Override
	public int compareTo(Object arg0) {
		return this.getCommandName().compareTo(((ICommand) arg0).getCommandName());
	}

	@Override
	public String getCommandName() {
		return "buildcraft";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/" + this.getCommandName() + " help";
	}

	@Override
	public List getCommandAliases() {
		return null;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] arguments) {

		if (arguments.length <= 0)
			throw new WrongUsageException("Type '" + this.getCommandUsage(sender) + "' for help.");

		if (arguments[0].matches("version")) {
			commandVersion(sender, arguments);
			return;
		} else if (arguments[0].matches("help")) {
			sender.sendChatToPlayer("Format: '" + this.getCommandName() + " <command> <arguments>'");
			sender.sendChatToPlayer("Available commands:");
			sender.sendChatToPlayer("- version : Version information.");
			return;
		}

		throw new WrongUsageException(this.getCommandUsage(sender));
	}

	private void commandVersion(ICommandSender sender, String[] arguments) {
		String colour = Version.isOutdated() ? "\u00A7c" : "\u00A7a";

		sender.sendChatToPlayer(String.format(colour + "BuildCraft %s for Minecraft %s (Latest: %s).", Version.getVersion(),
				CoreProxy.proxy.getMinecraftVersion(), Version.getRecommendedVersion()));
	}

}
