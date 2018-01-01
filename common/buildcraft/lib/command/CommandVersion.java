package buildcraft.lib.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.ForgeVersion.Status;

import buildcraft.api.core.BCLog;

import buildcraft.lib.BCLib;

public class CommandVersion extends CommandBase {

    @Override
    public String getName() {
        return "version";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "command.buildcraft.buildcraft.version.help";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        ForgeVersion.CheckResult result = ForgeVersion.getResult(BCLib.MOD_CONTAINER);
        if (result.status == Status.FAILED) {
            sender.sendMessage(new TextComponentTranslation("command.buildcraft.version.failed"));
            return;
        }

        Style style = new Style();
        if (result.status == Status.OUTDATED) {
            style.setColor(TextFormatting.RED);
        } else {
            style.setColor(TextFormatting.GREEN);
        }

        BCLog.logger.info("[lib.command.version] Result status = " + result.status);
        BCLog.logger.info("[lib.command.version] Result url = " + result.url);
        BCLog.logger.info("[lib.command.version] Result target = " + result.target);
        BCLog.logger.info("[lib.command.version] Result changes = " + result.changes);

        String currentVersion = BCLib.VERSION;
        if (currentVersion.startsWith("$")) {
            currentVersion = "?.??.??";
            style.setColor(TextFormatting.GRAY);
        }

        Object[] textArgs = { currentVersion, ForgeVersion.mcVersion, result.target.toString() };
        sender.sendMessage(new TextComponentTranslation("command.buildcraft.version", textArgs).setStyle(style));

        if (currentVersion.contains("-pre")) {
            sender.sendMessage(new TextComponentTranslation("command.buildcraft.version.prerelease"));
        }
    }
}
