package buildcraft.lib.command;

import com.mojang.brigadier.Command;
import net.minecraft.command.CommandException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.MinecraftServer;

import buildcraft.lib.script.ReloadableRegistryManager;

public class CommandReloadRegistries extends CommandBase {

    public CommandReloadRegistries() {}

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public String getName() {
        return "reload";
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public String getUsage(ICommandSender sender) {
        return "command.buildcraft.reload";
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        ReloadableRegistryManager.DATA_PACKS.reloadAll();
    }
}
