/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.ChatComponentText;

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
	public boolean canCommandSenderUseCommand(ICommandSender par1ICommandSender) {
		return true;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List getCommandAliases() {
		return new LinkedList<String>();
	}

	@Override
	public void processCommand(ICommandSender sender, String[] arguments) throws WrongUsageException {

		if (arguments.length <= 0) {
			throw new WrongUsageException("Type '" + this.getCommandUsage(sender) + "' for help.");
		}

		if (arguments[0].matches("version")) {
			commandVersion(sender, arguments);
			return;
		} else if (arguments[0].matches("help")) {
			sender.addChatMessage(new ChatComponentText("Format: '" + this.getCommandName() + " <command> <arguments>'"));
			sender.addChatMessage(new ChatComponentText("Available commands:"));
			sender.addChatMessage(new ChatComponentText("- version : Version information."));
			return;
		}

		throw new WrongUsageException(this.getCommandUsage(sender));
	}

	private void commandVersion(ICommandSender sender, String[] arguments) {
		String colour = Version.isOutdated() ? "\u00A7c" : "\u00A7a";

		sender.addChatMessage(new ChatComponentText(String.format(colour + "BuildCraft %s for Minecraft %s (Latest: %s).", Version.getVersion(),
				CoreProxy.proxy.getMinecraftVersion(), Version.getRecommendedVersion())));

		// TODD This takes too much realstate. See how to improve
		// if (Version.isOutdated()) {
		// Version.displayChangelog(sender);
		// }
	}

}
