package buildcraft.lib.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class BCSubCommandBase {
    private final String name;
    private final String usage;
    private final int permission;
    private final Command<CommandSource> execute;

    BCSubCommandBase(String name, String usage, int permission, Command<CommandSource> execute) {
        this.name = name;
        this.usage = usage;
        this.permission = permission;
        this.execute = execute;
    }

    void addSubcommand(LiteralArgumentBuilder<CommandSource> parentCommand) {
        parentCommand
                .then(
                        Commands.literal(name)
                                .requires((req) -> req.hasPermission(permission))
                                .executes(execute)
                );
    }
}
