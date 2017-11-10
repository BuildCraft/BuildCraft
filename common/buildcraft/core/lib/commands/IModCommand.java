/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.core.lib.commands;

import java.util.List;
import java.util.SortedSet;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;

public interface IModCommand extends ICommand {

	String getFullCommandString();

	@Override
	List<String> getCommandAliases();

	int getMinimumPermissionLevel();

	SortedSet<SubCommand> getChildren();

	void printHelp(ICommandSender sender);
}