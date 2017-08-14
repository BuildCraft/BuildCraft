/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.loader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.util.text.TextFormatting;

import net.minecraftforge.oredict.OreDictionary;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;

import buildcraft.lib.client.guide.PageEntry;
import buildcraft.lib.client.guide.PageLine;
import buildcraft.lib.client.guide.parts.GuideChapterWithin;
import buildcraft.lib.client.guide.parts.GuidePageFactory;
import buildcraft.lib.client.guide.parts.GuidePartFactory;
import buildcraft.lib.client.guide.parts.GuidePartNewPage;
import buildcraft.lib.client.guide.parts.GuideText;
import buildcraft.lib.client.guide.parts.recipe.GuideCraftingFactory;
import buildcraft.lib.client.guide.parts.recipe.GuideSmeltingFactory;
import buildcraft.lib.client.guide.parts.recipe.RecipeLookupHelper;

public enum MarkdownPageLoader implements IPageLoaderText {
    INSTANCE;

    // TODO: Make this change markdown syntax to XML, then pass
    // the resulting string to XmlPageLoader
    // Perhaps recipes should be forced xml?

    public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.markdown");

    @Deprecated
    public static final Map<String, SpecialParser> SPECIAL_FACTORIES = new HashMap<>();

    @Deprecated
    @FunctionalInterface
    public interface SpecialParser {
        List<GuidePartFactory> parse(String after);
    }

    @Deprecated
    @FunctionalInterface
    public interface SpecialParserSingle extends SpecialParser {
        @Override
        default List<GuidePartFactory> parse(String after) {
            GuidePartFactory single = parseSingle(after);
            if (single == null) return null;
            return ImmutableList.of(single);
        }

        GuidePartFactory parseSingle(String after);
    }

    static {
        putSingle("special.new_page", (after) -> GuidePartNewPage::new);
        putSingle("special.chapter", MarkdownPageLoader::chapter);
        putSingle("special.crafting", MarkdownPageLoader::loadCraftingLine);
        putSingle("special.smelting", MarkdownPageLoader::loadSmeltingLine);
        putMulti("special.all_crafting", MarkdownPageLoader::loadAllCrafting);
        putMulti("special.recipe", MarkdownPageLoader::loadRecipes);
        putMulti("special.usage", MarkdownPageLoader::loadUsages);
    }

    private static void putSingle(String string, SpecialParserSingle parser) {
        SPECIAL_FACTORIES.put(string, parser);
    }

    private static void putMulti(String string, SpecialParser parser) {
        SPECIAL_FACTORIES.put(string, parser);
    }

    @Deprecated
    public static List<GuidePartFactory> turnLineIntoPart(final String line) {
        List<GuidePartFactory> factories = null;

        // Ignore comments
        if (line.startsWith("//")) return null;

        // factories = loadImageLine(line);
        // if (factories != null) return factories;

        factories = loadSpecialLine(line);
        if (factories != null) return factories;

        // Attempt to load a "list" from all of the given lines

        // We failed to find a factory for any of the special, so lets just interpret it as a raw string
        return ImmutableList.of(loadRawString(line));
    }

    private static GuidePartFactory loadRawString(final String line) {
        String modLine = line;
        if (modLine.length() == 0) {
            modLine = " ";
        }

        boolean chapter = false;

        Set<TextFormatting> enabledFormattings = EnumSet.noneOf(TextFormatting.class);

        // Make the entire line have an underline, like a title
        if (modLine.startsWith("# ")) {
            modLine = modLine.substring(2);
            chapter = true;
        }
        // TODO: Add support for stuff like *italic* __bold__ ~~strikethrough~~ %%Obfuscated%%

        // And lists

        // Just use it as a normal text line
        final String text = modLine;
        if (chapter) {
            return (gui) -> new GuideChapterWithin(gui, text);
        } else {
            return (gui) -> new GuideText(gui, text);
        }
    }

    private static List<GuidePartFactory> loadSpecialLine(String line) {
        int endIndex = line.indexOf("]");
        if (line.startsWith("$[") && endIndex > 0) {
            String inner = line.substring(2, endIndex);
            String after = line.substring(endIndex + 1);
            SpecialParser factory = SPECIAL_FACTORIES.get(inner);
            if (factory != null) {
                return factory.parse(after);
            }
        }
        return null;
    }

    private static List<GuidePartFactory> createNewPage(String after) {
        return ImmutableList.of(GuidePartNewPage::new);
    }

    private static GuidePartFactory loadCraftingLine(String substring) {
        ItemStack stack = loadItemStack(substring);
        if (stack == null) {
            return null;
        }
        return GuideCraftingFactory.create(stack);
    }

    private static GuidePartFactory loadSmeltingLine(String substring) {
        ItemStack stack = loadItemStack(substring);
        if (stack == null) {
            return null;
        }
        return GuideSmeltingFactory.create(stack);
    }

    private static List<GuidePartFactory> loadRecipes(String substring) {
        ItemStack stack = loadItemStack(substring);
        if (stack == null) {
            return null;
        }
        return RecipeLookupHelper.getAllRecipes(stack);
    }

    private static List<GuidePartFactory> loadUsages(String substring) {
        ItemStack stack = loadItemStack(substring);
        if (stack == null) {
            return null;
        }
        return RecipeLookupHelper.getAllUsages(stack);
    }

    private static List<GuidePartFactory> loadAllCrafting(String substring) {
        ItemStack stack = loadItemStack(substring);
        if (stack == null) {
            return null;
        }
        return loadAllCrafting(stack);
    }

    public static List<GuidePartFactory> loadAllCrafting(@Nonnull ItemStack stack) {
        List<GuidePartFactory> list = new ArrayList<>();
        List<GuidePartFactory> recipeParts = RecipeLookupHelper.getAllRecipes(stack);
        if (recipeParts.size() > 0) {
            list.add(GuidePartNewPage::new);
            if (recipeParts.size() == 1) {
                list.add(chapter("buildcraft.guide.recipe.create"));
            } else {
                list.add(chapter("buildcraft.guide.recipe.create.plural"));
            }
            list.addAll(recipeParts);
        }
        List<GuidePartFactory> usageParts = RecipeLookupHelper.getAllUsages(stack);
        // Ensure we don't have any duplicate recipes
        usageParts.removeAll(recipeParts);
        if (usageParts.size() > 0) {
            if (recipeParts.size() != 1) {
                list.add(GuidePartNewPage::new);
            }
            if (usageParts.size() == 1) {
                list.add(chapter("buildcraft.guide.recipe.use"));
            } else {
                list.add(chapter("buildcraft.guide.recipe.use.plural"));
            }
            list.addAll(usageParts);
        }
        return list;
    }

    public static GuidePartFactory chapter(String after) {
        return (gui) -> new GuideChapterWithin(gui, I18n.format(after));
    }

    public static GuidePartFactory translate(String text) {
        return gui -> new GuideText(gui, new PageLine(0, I18n.format(text), false));
    }

    public static ItemStack loadItemStack(String line) {
        if (line == null) return null;
        if (line.startsWith("(") && line.endsWith(")")) {
            return loadSimpleItemStack(line.substring(1, line.length() - 1));
        } else if (line.startsWith("{") && line.endsWith("}")) {
            return loadComplexItemStack(line.substring(1, line.length() - 1));
        }
        return null;
    }

    private static ItemStack loadSimpleItemStack(String substring) {
        Item item = Item.getByNameOrId(substring);
        if (item != null) {
            return new ItemStack(item);
        } else {
            BCLog.logger.warn("[lib.guide.loader.markdown] " + substring + " was not a valid item!");
            return null;
        }
    }

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
                    return "<recipes_usages stack=\"" + stack + "\"/>";
                }
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
