package buildcraft.core.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import buildcraft.BuildCraftCore;
import buildcraft.core.lib.commands.CommandHelpers;
import buildcraft.core.lib.commands.SubCommand;

public class SubCommandOp extends SubCommand {
    public SubCommandOp() {
        super("op");
        setPermLevel(PermLevel.SERVER_ADMIN);
    }

    @Override
    public void processSubCommand(ICommandSender sender, String[] args) throws CommandException {
        MinecraftServer server = sender.getServer();
        if (server == null) {
            throw new CommandException("No server!");
        } else {
            server.getPlayerList().addOp(BuildCraftCore.gameProfile);
            CommandHelpers.sendLocalizedChatMessage(sender, "commands.op.success", "[BuildCraft]");
        }
    }
}
