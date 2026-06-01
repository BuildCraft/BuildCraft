// STUB(R.Chen): Forge CommandTreeBase — compile shim. TODO: replace with Fabric command system.
package net.minecraftforge.server.command;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.command.ServerCommandSource;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

public abstract class CommandTreeBase {
    private final List<Object> subCommands = new ArrayList<>();

    public void addSubcommand(Object command) {
        subCommands.add(command);
    }

    public List<Object> getSubCommands() {
        return subCommands;
    }

    public abstract String getName();
    public abstract String getUsage(ServerCommandSource source);
}
