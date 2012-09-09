package buildcraft.core;

import java.util.List;

import buildcraft.core.proxy.CoreProxy;

import net.minecraft.src.CommandBase;
import net.minecraft.src.ICommand;
import net.minecraft.src.ICommandSender;
import net.minecraft.src.WrongUsageException;

public class CommandBuildCraft extends CommandBase {

	@Override
	public int compareTo(Object arg0) {
        return this.getCommandName().compareTo(((ICommand)arg0).getCommandName());
	}

	@Override
	public String getCommandName() {
		return "buildcraft";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/" + this.getCommandName() + " help";
	}

	@Override public List getCommandAliases() { return null; }
	
	@Override
	public void processCommand(ICommandSender sender, String[] arguments) {
		
        if (arguments.length <= 0)
        	throw new WrongUsageException("Type '" + this.getCommandUsage(sender) + "' for help.");
        
        if(arguments[0].matches("version")) {
        	commandVersion(sender, arguments);
        	return;
        } else if(arguments[0].matches("help")) {
        	sender.sendChatToPlayer("Format: '"+ this.getCommandName() +" <command> <arguments>'");
        	sender.sendChatToPlayer("Available commands:");
        	sender.sendChatToPlayer("- version : Version information.");
        	return;
        }

    	throw new WrongUsageException(this.getCommandUsage(sender));
	}

	private void commandVersion(ICommandSender sender, String[] arguments) {
    	sender.sendChatToPlayer(String.format("BuildCraft %s for Minecraft %s (Latest: %s).", Version.getVersion(), CoreProxy.proxy.getMinecraftVersion(), Version.getRecommendedVersion()));
	}
	

}
