/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.loader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;

import net.minecraftforge.oredict.OreDictionary;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;

import buildcraft.lib.client.guide.PageEntry;
import buildcraft.lib.client.guide.parts.GuidePageFactory;

public enum MarkdownPageLoader implements IPageLoaderText {
    INSTANCE;

    public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.markdown");

    public static ItemStack loadComplexItemStack(String line) {
        String[] args = line.split(",");
        if (args.length == 0) {
            BCLog.logger.warn("[lib.guide.loader.markdown] " + line + " was not a valid complex item string!");
            return null;
        }
        ItemStack stack = null;
        Item item = Item.getByNameOrId(args[0].trim());
        if (item != null) {
            stack = new ItemStack(item);
        } else {
            BCLog.logger.warn("[lib.guide.loader.markdown] " + args[0] + " was not a valid item!");
            return null;
        }

        if (args.length == 1) return stack;

        int stackSize = 1;
        try {
            stackSize = Integer.parseInt(args[1].trim());
        } catch (NumberFormatException nfe) {
            BCLog.logger.warn(
                "[lib.guide.loader.markdown] " + args[1] + " was not a valid number: " + nfe.getLocalizedMessage());
        }
        stack.setCount(stackSize);

        if (args.length == 2) return stack;

        try {
            int meta = Integer.parseInt(args[2].trim());
            if (meta == -1) {
                // Use oredict
                meta = OreDictionary.WILDCARD_VALUE;
            }
            stack = new ItemStack(stack.getItem(), stack.getCount(), meta);
        } catch (NumberFormatException nfe) {
            BCLog.logger.warn(
                "[lib.guide.loader.markdown] " + args[2] + " was not a valid number: " + nfe.getLocalizedMessage());
        }

        if (args.length == 3) return stack;

        String nbtString = args[3];
        try {
            stack.setTagCompound(JsonToNBT.getTagFromJson(nbtString));
        } catch (NBTException e) {
            BCLog.logger.warn(
                "[lib.guide.loader.markdown] " + nbtString + " was not a valid nbt tag: " + e.getLocalizedMessage());
        }
        return stack;
    }

    @Override
    public GuidePageFactory loadPage(BufferedReader reader, PageEntry entry) throws IOException {
        StringBuilder replaced = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            // First replace special tags (at the start of a line) with xml ones
            line = replaceSpecialForXml(line);
            replaced.append(line);
            replaced.append('\n');
        }

        return XmlPageLoader.INSTANCE.loadPage(new BufferedReader(new StringReader(replaced.toString())), entry);
    }

    private static String replaceSpecialForXml(String line) {
        if (line.startsWith("$[special.") && line.indexOf(']') > 0) {
            int end = line.indexOf(']');
            String post = line.substring("$[special.".length(), end);
            switch (post) {
                case "new_page": {
                    return "<new_page/>";
                }
                case "all_crafting": {
                    String stack = line.substring(end + 1);
                    String additional = "";
                    if (stack.startsWith("\"") && stack.endsWith("\"")) {
                        stack = stack.substring(1, stack.length() - 1);
                    }
                    if (stack.startsWith("(") && stack.endsWith(")")) {
                        stack = stack.substring(1, stack.length() - 1);
                    } else if (stack.startsWith("{") && stack.contains("}")) {
                        int curlyStart = stack.indexOf('}');
                        stack = stack.substring(1, curlyStart);
                        String[] split = stack.split(",");
                        if (split.length > 0) {
                            stack = split[0];
                        }
                        if (split.length > 1) {
                            additional += " count=\"" + split[1] + "\"";
                        }
                        if (split.length > 2) {
                            additional += " data=\"" + split[2] + "\"";
                        }
                    }
                    return "<recipes_usages stack=\"" + stack + "\"" + additional + "/>";
                }
                default:
            }
        } else if (line.startsWith("#")) {
            while (line.startsWith("#")) {
                line = line.substring(1);
            }
            line = line.trim();
            return "<chapter name=\"" + line + "\"/>";
        }
        return line;
    }
}
