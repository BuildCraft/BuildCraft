package buildcraft.core.command;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;

import buildcraft.core.Version;
import buildcraft.core.lib.commands.SubCommand;
import buildcraft.core.proxy.CoreProxy;

public class SubCommandVersion extends SubCommand {
    public SubCommandVersion() {
        super("version");
    }

    @Override
    public void processSubCommand(ICommandSender sender, String[] args) {
        String colour = Version.isOutdated() ? "\u00A7c" : "\u00A7a";

        sender.addChatMessage(new ChatComponentText(String.format(colour + StatCollector.translateToLocal("command.buildcraft.version"), Version
                .getVersion(), CoreProxy.proxy.getMinecraftVersion(), Version.getRecommendedVersion())));
    }
}
