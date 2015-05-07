package buildcraft.core.command;

import buildcraft.core.Version;
import buildcraft.core.lib.commands.SubCommand;
import buildcraft.core.proxy.CoreProxy;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;

public class SubCommandChangelog extends SubCommand {
    public SubCommandChangelog() {
        super("changelog");
    }

    @Override
    public void processSubCommand(ICommandSender sender, String[] args) {
        Version.displayChangelog(sender);
    }
}
