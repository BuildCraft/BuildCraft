package buildcraft.core.command;

import net.minecraft.command.CommandException;
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
    public void processSubCommand(ICommandSender sender, String[] args) throws CommandException {
        MinecraftServer server = sender.getServer();
        if (server == null) {
            throw new CommandException("No server!");
        } else {
            server.getPlayerList().removeOp(BuildCraftCore.gameProfile);
            CommandHelpers.sendLocalizedChatMessage(sender, "commands.deop.success", "[BuildCraft]");
        }
    }
}
