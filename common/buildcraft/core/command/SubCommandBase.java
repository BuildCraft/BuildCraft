package buildcraft.core.command;

import net.minecraft.command.ICommandSender;

public abstract class SubCommandBase {
	public abstract String getName();
	public abstract String getDescription();
	public abstract void processCommand(ICommandSender sender, String[] arguments);
}
