package buildcraft.lib.command;

import com.mojang.brigadier.Command;
import net.minecraft.command.CommandException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.LiteralText;

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
        sender.sendMessage(new LiteralText("TODO: Implement this!"));
    }
}
