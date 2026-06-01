package buildcraft.lib.command;

import com.mojang.brigadier.Command;
import net.minecraft.command.CommandException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;

public class CommandChangelog extends CommandBase {
    // @Override -- removed: method does not exist in Fabric 1.20.1
    public String getName() {
        return "changelog";
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public String getUsage(ICommandSender sender) {
        return "command.buildcraft.buildcraft.changelog.help";
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public int getRequiredPermissionLevel() {
        return 0;
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        sender.sendMessage(Text.literal("TODO: Implement this!"));
    }
}
