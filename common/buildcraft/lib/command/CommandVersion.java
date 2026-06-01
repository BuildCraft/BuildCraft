package buildcraft.lib.command;

import com.mojang.brigadier.Command;
import net.minecraft.command.CommandException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.ForgeVersion.Status;

import buildcraft.api.core.BCLog;

import buildcraft.lib.BCLib;
import net.minecraft.text.Text;

public class CommandVersion extends CommandBase {

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public String getName() {
        return "version";
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public String getUsage(ICommandSender sender) {
        return "command.buildcraft.buildcraft.version.help";
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public int getRequiredPermissionLevel() {
        return 0;
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        ForgeVersion.CheckResult result = ForgeVersion.getResult(BCLib.MOD_CONTAINER);
        if (result.status == Status.FAILED) {
            sender.sendMessage(Text.translatable("command.buildcraft.version.failed"));
            return;
        }

        Style style = new Style();
        if (result.status == Status.OUTDATED) {
            style.setColor(Formatting.RED);
        } else {
            style.setColor(Formatting.GREEN);
        }

        BCLog.logger.info("[lib.command.version] Result status = " + result.status);
        BCLog.logger.info("[lib.command.version] Result url = " + result.url);
        BCLog.logger.info("[lib.command.version] Result target = " + result.target);
        BCLog.logger.info("[lib.command.version] Result changes = " + result.changes);

        String currentVersion = BCLib.VERSION;
        if (currentVersion.startsWith("$")) {
            currentVersion = "?.??.??";
            style.setColor(Formatting.GRAY);
        }

        Object[] textArgs = { currentVersion, ForgeVersion.mcVersion, result.target.toString() };
        sender.sendMessage(Text.translatable("command.buildcraft.version", textArgs).setStyle(style));

        if (currentVersion.contains("-pre")) {
            sender.sendMessage(Text.translatable("command.buildcraft.version.prerelease"));
        }
    }
}
