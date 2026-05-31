package buildcraft.lib.command;

import com.mojang.brigadier.Command;
import net.minecraft.command.CommandException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.MinecraftServer;

import buildcraft.lib.script.ReloadableRegistryManager;

public class CommandReloadRegistries extends CommandBase {

    public CommandReloadRegistries() {}

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "command.buildcraft.reload";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        ReloadableRegistryManager.DATA_PACKS.reloadAll();
    }
}
