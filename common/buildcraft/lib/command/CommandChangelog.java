package buildcraft.lib.command;

import net.minecraft.entity.Entity;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;

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
                        e.sendMessage(new StringTextComponent("TODO: Implement this!"), Util.NIL_UUID);
                    }
                    return 0;
                }
        );
    }
}
