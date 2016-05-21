/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

public final class CommandHelpers {
    private CommandHelpers() {

    }

    public static World getWorld(ICommandSender sender, IModCommand command, String[] args, int worldArgIndex) throws CommandException {
        // Handle passed in world argument
        if (worldArgIndex < args.length) {
            try {
                int dim = Integer.parseInt(args[worldArgIndex]);
                World world = sender.getServer().worldServerForDimension(dim);
                if (world != null) {
                    return world;
                }
            } catch (Exception ex) {
                throwWrongUsage(sender, command);
            }
        }
        return getWorld(sender, command);
    }

    public static World getWorld(ICommandSender sender, IModCommand command) {
        if (sender instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) sender;
            return player.worldObj;
        }
        return sender.getServer().worldServerForDimension(0);
    }

    public static String[] getPlayers(ICommandSender sender) {
        return sender.getServer().getAllUsernames();
    }

    public static void sendLocalizedChatMessage(ICommandSender sender, String locTag, Object... args) {
        sender.addChatMessage(new TextComponentTranslation(locTag, args));
    }

    public static void sendLocalizedChatMessage(ICommandSender sender, Style chatStyle, String locTag, Object... args) {
        TextComponentTranslation chat = new TextComponentTranslation(locTag, args);
        chat.setStyle(chatStyle);
        sender.addChatMessage(chat);
    }

    /** Avoid using this function if at all possible. Commands are processed on the server, which has no localization
     * information.
     * 
     * @param sender
     * @param message */
    public static void sendChatMessage(ICommandSender sender, String message) {
        sender.addChatMessage(new TextComponentString(message));
    }

    public static void throwWrongUsage(ICommandSender sender, IModCommand command) throws WrongUsageException {
        throw new WrongUsageException(String.format(I18n.translateToLocal("command.buildcraft.help"), command.getCommandUsage(sender)));
    }

    public static void processChildCommand(ICommandSender sender, SubCommand child, String[] args) throws CommandException {
        if (!sender.canCommandSenderUseCommand(child.getMinimumPermissionLevel(), child.getFullCommandString())) {
            throw new WrongUsageException(I18n.translateToLocal("command.buildcraft.noperms"));
        }
        String[] newargs = new String[args.length - 1];
        System.arraycopy(args, 1, newargs, 0, newargs.length);
        child.execute(sender.getServer(), sender, newargs);
    }

    public static void printHelp(ICommandSender sender, IModCommand command) {
        Style header = new Style();
        header.setColor(TextFormatting.GRAY);
        header.setBold(true);
        sendLocalizedChatMessage(sender, header, "command.buildcraft." + command.getFullCommandString().replace(" ", ".") + ".format", command.getFullCommandString());
        Style body = new Style();
        body.setColor(TextFormatting.GRAY);
        if (command.getCommandAliases().size() > 0) {
            sendLocalizedChatMessage(sender, body, "command.buildcraft.aliases", command.getCommandAliases().toString().replace("[", "").replace("]", ""));
        }
        if (command.getMinimumPermissionLevel() > 0) {
            sendLocalizedChatMessage(sender, body, "command.buildcraft.permlevel", command.getMinimumPermissionLevel());
        }
        sendLocalizedChatMessage(sender, body, "command.buildcraft." + command.getFullCommandString().replace(" ", ".") + ".help");
        if (!command.getChildren().isEmpty()) {
            sendLocalizedChatMessage(sender, "command.buildcraft.list");
            for (SubCommand child : command.getChildren()) {
                sendLocalizedChatMessage(sender, "command.buildcraft." + child.getFullCommandString().replace(" ", ".") + ".desc", child.getCommandName());
            }
        }
    }

    public static boolean processStandardCommands(ICommandSender sender, IModCommand command, String[] args) throws CommandException {
        if (args.length >= 1) {
            if ("help".equals(args[0])) {
                if (args.length >= 2) {
                    for (SubCommand child : command.getChildren()) {
                        if (matches(args[1], child)) {
                            child.printHelp(sender);
                            return true;
                        }
                    }
                }
                command.printHelp(sender);
                return true;
            }
            for (SubCommand child : command.getChildren()) {
                if (matches(args[0], child)) {
                    processChildCommand(sender, child, args);
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean matches(String commandName, IModCommand command) {
        if (commandName.equals(command.getCommandName())) {
            return true;
        } else if (command.getCommandAliases() != null) {
            for (String alias : command.getCommandAliases()) {
                if (commandName.equals(alias)) {
                    return true;
                }
            }
        }
        return false;
    }
}
