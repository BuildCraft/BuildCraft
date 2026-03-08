package buildcraft.lib.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;

//public class CommandBuildCraft extends CommandTreeBase
public class CommandBuildCraft {
    private static final String NAME = "buildcraft";
    private static final String USAGE = "command.buildcraft.help";

    public static final LiteralArgumentBuilder<CommandSource> COMMAND =
            LiteralArgumentBuilder.<CommandSource>literal(NAME)
                    .requires((req) -> req.hasPermission(0));

//    public CommandBuildCraft() {
//        addSubcommand(new CommandVersion());
//        addSubcommand(new CommandChangelog());
//        addSubcommand(new CommandReloadRegistries());
//    }

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        new CommandVersion().addSubcommand(COMMAND);
        new CommandChangelog().addSubcommand(COMMAND);
        new CommandReloadRegistries().addSubcommand(COMMAND);
        dispatcher.register(COMMAND);
    }
}
