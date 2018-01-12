package buildcraft.lib.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CommandChangelog extends CommandBase {
    @Override
    public String getName() {
        return "changelog";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "command.buildcraft.buildcraft.changelog.help";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        sender.sendMessage(new TextComponentString("TODO: Implement this!"));
    }
}
