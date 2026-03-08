package buildcraft.lib.command;

import buildcraft.api.core.BCLog;
import buildcraft.lib.BCLib;
import net.minecraft.entity.Entity;
import net.minecraft.util.Util;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.VersionChecker;
import net.minecraftforge.fml.VersionChecker.CheckResult;
import net.minecraftforge.fml.VersionChecker.Status;
import net.minecraftforge.versions.mcp.MCPVersion;

//public class CommandVersion extends CommandBase
public class CommandVersion extends BCSubCommandBase {
    CommandVersion() {
        super(
                "version",
                "command.buildcraft.buildcraft.version.help",
                0,
                (arg) ->
                {
                    Entity sender = arg.getSource().getEntity();
                    if (sender == null) {
                        return 0;
                    }
//                    ForgeVersion.CheckResult result = ForgeVersion.getResult(BCLib.MOD_CONTAINER);
                    CheckResult result = VersionChecker.getResult(BCLib.MOD_CONTAINER.getModInfo());
                    if (result.status == Status.FAILED) {
                        sender.sendMessage(new TranslationTextComponent("command.buildcraft.version.failed"), Util.NIL_UUID);
                        return 0;
                    }

//                    Style style = new Style();
                    Style style = Style.EMPTY;
                    if (result.status == Status.OUTDATED) {
//                        style.setColor(TextFormatting.RED);
                        style.withColor(TextFormatting.RED);
                    } else {
//                        style.setColor(TextFormatting.GREEN);
                        style.withColor(TextFormatting.GREEN);
                    }

                    BCLog.logger.info("[lib.command.version] Result status = " + result.status);
                    BCLog.logger.info("[lib.command.version] Result url = " + result.url);
                    BCLog.logger.info("[lib.command.version] Result target = " + result.target);
                    BCLog.logger.info("[lib.command.version] Result changes = " + result.changes);

                    String currentVersion = BCLib.VERSION;
                    if (currentVersion.startsWith("$")) {
                        currentVersion = "?.??.??";
//                        style.setColor(TextFormatting.GRAY);
                        style.withColor(TextFormatting.GRAY);
                    }

                    Object[] textArgs = { currentVersion, MCPVersion.getMCVersion(), result.target };
//                    sender.sendMessage(new TextComponentTranslation("command.buildcraft.version", textArgs).setStyle(style));
                    sender.sendMessage(new TranslationTextComponent("command.buildcraft.version", textArgs).setStyle(style), Util.NIL_UUID);

                    if (currentVersion.contains("-pre")) {
//                        sender.sendMessage(new TextComponentTranslation("command.buildcraft.version.prerelease"));
                        sender.sendMessage(new TranslationTextComponent("command.buildcraft.version.prerelease"), Util.NIL_UUID);
                    }
                    return 0;
                }
        );
    }
}
