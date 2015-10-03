package buildcraft.core.command;

import net.minecraft.command.ICommandSender;

import buildcraft.core.Version;
import buildcraft.core.lib.commands.SubCommand;

public class SubCommandChangelog extends SubCommand {
	public SubCommandChangelog() {
		super("changelog");
	}

	@Override
	public void processSubCommand(ICommandSender sender, String[] args) {
		Version.displayChangelog(sender);
	}
}
