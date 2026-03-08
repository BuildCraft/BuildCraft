package buildcraft.lib.command;

import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;
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
                        e.sendMessage(new TextComponent("TODO: Implement this!"), Util.NIL_UUID);
                    }
                    return 0;
                }
        );
    }
}
