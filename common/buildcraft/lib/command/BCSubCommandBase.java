package buildcraft.lib.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class BCSubCommandBase {
    private final String name;
    private final String usage;
    private final int permission;
    private final Command<CommandSourceStack> execute;

    BCSubCommandBase(String name, String usage, int permission, Command<CommandSourceStack> execute) {
        this.name = name;
        this.usage = usage;
        this.permission = permission;
        this.execute = execute;
    }

    void addSubcommand(LiteralArgumentBuilder<CommandSourceStack> parentCommand) {
        parentCommand
                .then(
                        Commands.literal(name)
                                .requires((req) -> req.hasPermission(permission))
                                .executes(execute)
                );
    }
}
