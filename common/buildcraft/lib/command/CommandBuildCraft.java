package buildcraft.lib.command;

import net.minecraft.server.command.ServerCommandSource;

import net.minecraftforge.server.command.CommandTreeBase;

public class CommandBuildCraft extends CommandTreeBase {

    public CommandBuildCraft() {
        addSubcommand(new CommandVersion());
        addSubcommand(new CommandChangelog());
        addSubcommand(new CommandReloadRegistries());
    }

    @Override
    public String getName() {
        return "buildcraft";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "command.buildcraft.help";
    }
}
