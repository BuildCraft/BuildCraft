/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.loader;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
import buildcraft.api.registry.IScriptableRegistry.OptionallyDisabled;
import buildcraft.lib.client.guide.entry.PageEntry;
import buildcraft.lib.client.guide.parts.GuidePageFactory;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

public enum MarkdownPageLoader implements IPageLoaderText {
    INSTANCE;

    public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.markdown");

    public static ItemStack loadComplexItemStack(String line) {
        OptionallyDisabled<ItemStack> stackq = parseItemStack(line);
        if (stackq.isPresent()) {
            return stackq.get();
        }
        BCLog.logger.warn("[lib.guide.loader.markdown] " + stackq.getDisabledReason());
        return ItemStack.EMPTY;
    }

    public static OptionallyDisabled<ItemStack> parseItemStack(String line) {
        String[] args = line.split(",");
        if (args.length == 0) {
            return new OptionallyDisabled<>(line + " was not a valid complex item string!");
        }
        ItemStack stack = null;
//        Item item = Item.getByNameOrId(args[0].trim());
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(args[0].trim()));
        if (item != null) {
            stack = new ItemStack(item);
        } else {
            return new OptionallyDisabled<>(args[0] + " was not a valid item!");
        }

        if (args.length == 1) {
            return new OptionallyDisabled<>(stack);
        }

        int stackSize = 1;
        try {
            stackSize = Integer.parseInt(args[1].trim());
        } catch (NumberFormatException nfe) {
            return new OptionallyDisabled<>(args[1] + " was not a valid number: " + nfe.getLocalizedMessage());
        }
        stack.setCount(stackSize);

        if (args.length == 2) {
            return new OptionallyDisabled<>(stack);
        }

//        try {
//            int meta = Integer.parseInt(args[2].trim());
//            if (meta == -1) {
//                // Use oredict
//                meta = OreDictionary.WILDCARD_VALUE;
//            }
//            stack = new ItemStack(stack.getItem(), stack.getCount(), meta);
//            stack = new ItemStack(stack.getItem(), stack.getCount());
//        } catch (NumberFormatException nfe) {
//            return new OptionallyDisabled<>(args[2] + " was not a valid number: " + nfe.getLocalizedMessage());
//        }
        // Calen
        int meta = Integer.parseInt(args[2].trim());
        if (meta != -1) {
            throw new RuntimeException("[lib.guide.loader.xml] Found meta data [" + meta + "] in line [" + line + "] but meta data is not supported in this ms version.");
        }

        if (args.length == 3) {
            return new OptionallyDisabled<>(stack);
        }

        String nbtString = args[3];
        try {
//            stack.setTag(JsonToNBT.getTagFromJson(nbtString));
            stack.setTag(JsonToNBT.parseTag(nbtString));
        }
//        catch (NBTException e)
        catch (CommandSyntaxException e) {
            return new OptionallyDisabled<>(nbtString + " was not a valid nbt tag: " + e.getLocalizedMessage());
        }
        return new OptionallyDisabled<>(stack);
    }

    @Override
    public GuidePageFactory loadPage(BufferedReader reader, ResourceLocation name, PageEntry<?> entry, IProfiler prof)
            throws IOException {
        prof.push("md");
        StringBuilder replaced = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            // First replace special tags (at the start of a line) with xml ones
            line = replaceSpecialForXml(line);
            replaced.append(line);
            replaced.append('\n');
        }

        BufferedReader nReader = new BufferedReader(new StringReader(replaced.toString()));
        prof.pop();
        return XmlPageLoader.INSTANCE.loadPage(nReader, name, entry, prof);
    }

    private static String replaceSpecialForXml(String line) {
        if (line.startsWith("$[special.") && line.indexOf(']') > 0) {
            int end = line.indexOf(']');
            String post = line.substring("$[special.".length(), end);
            switch (post) {
                case "new_page": {
                    BCLog.logger.warn(
                            "[lib.guide.markdown] Found deprecated element '" + line
                                    + "', it should be replaced with '<new_page/>'"
                    );
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
                    String str = "<recipes_usages stack=\"" + stack + "\"" + additional + "/>";
                    BCLog.logger.warn(
                            "[lib.guide.markdown] Found deprecated element '" + line + "', it should be replaced with '"
                                    + str + "'"
                    );
                    return str;
                }
                default:
            }
        } else if (line.startsWith("#")) {
            int level = -1;
            while (line.startsWith("#")) {
                line = line.substring(1);
                level++;
            }
            line = line.trim();
            if (level == 0) {
                return "<chapter name=\"" + line + "\"/>";
            } else {
                return "<chapter name=\"" + line + "\" level=\"" + level + "\"/>";
            }
        }
        return line;
    }
}
