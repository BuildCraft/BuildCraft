package buildcraft.lib.command;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

//public class CommandChangelog extends CommandBase
public class CommandChangelog extends BCSubCommandBase {
    public CommandChangelog() {
        super(
                "changelog",
                "command.buildcraft.buildcraft.changelog.help",
                0,
                (arg) ->
                {
                    Entity e = arg.getSource().getEntity();
                    if (e != null) {
                        e.sendSystemMessage(Component.literal("TODO: Implement this!"));
                    }
                    return 0;
                }
        );
    }
}
