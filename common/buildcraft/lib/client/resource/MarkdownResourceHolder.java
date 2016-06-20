package buildcraft.lib.client.resource;

import java.util.*;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.PageLine;
import buildcraft.lib.client.guide.node.NodePageLine;
import buildcraft.lib.client.guide.parts.*;
import buildcraft.lib.client.guide.parts.recipe.RecipeLookupHelper;

public class MarkdownResourceHolder extends StringResourceHolder implements GuidePartFactory<GuidePage> {
    public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.markdown") || World.class.getName().contains("World");
    public static final Map<String, SpecialParser> SPECIAL_FACTORIES = new HashMap<>();

    @FunctionalInterface
    public interface SpecialParser {
        List<GuidePartFactory<?>> parse(String after);
    }

    @FunctionalInterface
    public interface SpecialParserSingle extends SpecialParser {
        @Override
        default List<GuidePartFactory<?>> parse(String after) {
            GuidePartFactory<?> single = parseSingle(after);
            if (single == null) return null;
            return ImmutableList.of(single);
        }

        GuidePartFactory<?> parseSingle(String after);
    }

    static {
        putSingle("special.new_page", (after) -> GuidePartNewPage::new);
        putSingle("special.crafting", MarkdownResourceHolder::loadCraftingLine);
        putSingle("special.smelting", MarkdownResourceHolder::loadSmeltingLine);
        putMulti("special.all_crafting", MarkdownResourceHolder::loadAllCrafting);
        putMulti("special.recipe", MarkdownResourceHolder::loadRecipes);
        putMulti("special.usage", MarkdownResourceHolder::loadUsages);
    }

    private List<GuidePartFactory<?>> factories = null;
    private final String title;

    public MarkdownResourceHolder(ResourceLocation location, String title) {
        super(location);
        this.title = title;
    }

    private static void putSingle(String string, SpecialParserSingle parser) {
        SPECIAL_FACTORIES.put(string, parser);
    }

    private static void putMulti(String string, SpecialParser parser) {
        SPECIAL_FACTORIES.put(string, parser);
    }

    @Override
    public void onStringChange() {
        List<GuidePartFactory<?>> newFactories = new ArrayList<>();
        List<String> lines = new ArrayList<>(getLines());
        while (!lines.isEmpty()) {
            String first = lines.remove(0);
            List<GuidePartFactory<?>> factories = turnLineIntoPart(first, lines);
            if (factories != null) {
                newFactories.addAll(factories);
            }
        }
        factories = newFactories;
    }

    public static List<GuidePartFactory<?>> turnLineIntoPart(final String line, List<String> after) {
        List<GuidePartFactory<?>> factories = null;

        // Ignore comments
        if (line.startsWith("//")) return null;

        factories = loadImageLine(line);
        if (factories != null) return factories;

        factories = loadSpecialLine(line);
        if (factories != null) return factories;

        // Attempt to load a "list" from all of the given lines

        // We failed to find a factory for any of the special, so lets just interpret it as a raw string
        return ImmutableList.of(loadRawString(line));
    }

    private static GuidePartFactory<?> loadRawString(final String line) {
        String modLine = line;
        if (modLine.length() == 0) {
            modLine = " ";
        }

        Set<TextFormatting> enabledFormattings = EnumSet.noneOf(TextFormatting.class);

        // Make the entire line have an underline, like a title
        if (modLine.startsWith("# ")) {
            modLine = TextFormatting.UNDERLINE + modLine.substring(2);
            enabledFormattings.add(TextFormatting.UNDERLINE);
        }
        // TODO: Add support for stuff like *italic* __bold__ ~~strikethrough~~ %%Obfuscated%%

        // And lists

        // Just use it as a normal text line
        NodePageLine node = new NodePageLine(null, null);
        node.addChild(new PageLine(0, modLine, false));
        return (gui) -> new GuideText(gui, node);
    }

    private static List<GuidePartFactory<?>> loadImageLine(String line) {
        // ![path/to/image]
        // ![path/to/image](width, height)

        if (line.startsWith("![")) {
            String substring = line.substring(2, line.length() - 1);
            if (line.endsWith("]")) {
                return ImmutableList.of(loadDefaultImage(substring));
            } else if (line.endsWith(")")) {
                int index = substring.indexOf("](");
                String loc = substring.substring(0, index);
                String args = substring.substring(index + 2);
                String[] argsSplit = args.split(",");
                BCLog.logger.info("[lib.markdown] Load image " + loc + ", " + args + " -> " + Arrays.toString(argsSplit));
            }
        }

        // if (line.startsWith("![")) {
        // line = line.substring(2);
        // if (line.indexOf("]") > 2) {
        // String location = line.substring(0, line.indexOf("]"));
        // if (line.endsWith("]")) {
        // line = line.substring(0, line.length() - 1);
        // return ImageLoader.loadImage(new ResourceLocation(location + ".png"), -1, -1);
        // } else if (line.endsWith(")") && line.indexOf("](") > 2) {
        // String meta = line.substring(line.indexOf("]("), line.length() - 1);
        // String[] args = meta.split(",");
        // if (args.length == 2) {
        // try {
        // int width = Integer.parseInt(args[0]);
        // int height = Integer.parseInt(args[1]);
        // return ImageLoader.loadImage(new ResourceLocation(location + ".png"), width, height);
        // } catch (NumberFormatException nfe) {
        // BCLog.logger.warn(nfe);
        // line = "![" + line;
        // }
        // }
        // }
        // }
        // }
        return null;
    }

    private static GuidePartFactory<?> loadDefaultImage(String location) {
        BCLog.logger.info("[lib.markdown] Load default image " + location);
        ResourceLocation resLoc = new ResourceLocation(location);
        TextureResourceHolder holder = new TextureResourceHolder(resLoc);
        return ResourceRegistry.INSTANCE.register(holder, TextureResourceHolder.class);
    }

    private static GuidePartFactory<?> loadSizedImage(String location, int width, int height) {
        BCLog.logger.info("[lib.markdown] Load sized image " + location + ", " + width + ", " + height);
        ResourceLocation resLoc = new ResourceLocation(location);
        TextureResourceHolder holder = new TextureResourceHolder(resLoc, width, height);
        return ResourceRegistry.INSTANCE.register(holder, TextureResourceHolder.class);
    }

    private static List<GuidePartFactory<?>> loadSpecialLine(String line) {
        int endIndex = line.indexOf("]");
        if (line.startsWith("$[") && endIndex > 0) {
            String inner = line.substring(2, endIndex);
            String after = line.substring(endIndex + 1);
            BCLog.logger.info("Changed \"" + line + " to [\"" + inner + "\", \"" + after + "\"]");
            SpecialParser factory = SPECIAL_FACTORIES.get(inner);
            if (factory != null) {
                return factory.parse(after);
            }
        }
        return null;
    }

    private static List<GuidePartFactory<?>> createNewPage(String after) {
        return ImmutableList.of(GuidePartNewPage::new);
    }

    private static GuidePartFactory<?> loadCraftingLine(String substring) {
        ItemStack stack = loadItemStack(substring);
        if (stack == null) {
            return null;
        }
        return GuideCraftingFactory.create(stack);
    }

    private static GuidePartFactory<?> loadSmeltingLine(String substring) {
        ItemStack stack = loadItemStack(substring);
        if (stack == null) {
            return null;
        }
        return GuideSmeltingFactory.create(stack);
    }

    private static List<GuidePartFactory<?>> loadRecipes(String substring) {
        ItemStack stack = loadItemStack(substring);
        if (stack == null) {
            return null;
        }
        return RecipeLookupHelper.getAllRecipes(stack);
    }

    private static List<GuidePartFactory<?>> loadUsages(String substring) {
        ItemStack stack = loadItemStack(substring);
        if (stack == null) {
            return null;
        }
        return RecipeLookupHelper.getAllUsages(stack);
    }

    private static List<GuidePartFactory<?>> loadAllCrafting(String substring) {
        ItemStack stack = loadItemStack(substring);
        if (stack == null) {
            return null;
        }
        return loadAllCrafting(stack);
    }

    public static List<GuidePartFactory<?>> loadAllCrafting(ItemStack stack) {
        List<GuidePartFactory<?>> list = new ArrayList<>();
        List<GuidePartFactory<?>> part = RecipeLookupHelper.getAllRecipes(stack);
        boolean addedNew = false;
        if (part.size() > 0) {
            list.add(GuidePartNewPage::new);
            addedNew = true;
            if (part.size() == 1) {
                list.add(translate("buildcraft.guide.recipe.create"));
            } else {
                list.add(translate("buildcraft.guide.recipe.create.plural"));
            }
            list.addAll(part);
        }
        part = RecipeLookupHelper.getAllUsages(stack);
        if (part.size() > 0) {
            if (!addedNew) list.add(GuidePartNewPage::new);
            if (part.size() == 1) {
                list.add(translate("buildcraft.guide.recipe.use"));
            } else {
                list.add(translate("buildcraft.guide.recipe.use.plural"));
            }
            list.addAll(part);
        }
        return list;
    }

    public static GuidePartFactory<?> translate(String text) {
        return (gui) -> {
            NodePageLine node = new NodePageLine(null, null);
            node.addChild(new PageLine(0, I18n.format(text), false));
            return new GuideText(gui, node);
        };
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
            BCLog.logger.warn("[lib.markdown] " + substring + " was not a valid item!");
            return null;
        }

    }

    private static ItemStack loadComplexItemStack(String line) {
        String[] args = line.split(",");
        if (args.length == 0) {
            BCLog.logger.warn("[lib.markdown] " + line + " was not a valid complex item string!");
            return null;
        }
        ItemStack stack = null;
        Item item = Item.getByNameOrId(args[0].trim());
        if (item != null) {
            stack = new ItemStack(item);
        } else {
            BCLog.logger.warn("[lib.markdown] " + args[0] + " was not a valid item!");
            return null;
        }

        if (args.length == 1) return stack;

        int stackSize = 1;
        try {
            stackSize = Integer.parseInt(args[1].trim());
        } catch (NumberFormatException nfe) {
            BCLog.logger.warn("[lib.markdown] " + args[1] + " was not a valid number: " + nfe.getLocalizedMessage());
        }
        stack.stackSize = stackSize;

        if (args.length == 2) return stack;

        try {
            int meta = Integer.parseInt(args[2].trim());
            stack = new ItemStack(stack.getItem(), stack.stackSize, meta);
        } catch (NumberFormatException nfe) {
            BCLog.logger.warn("[lib.markdown] " + args[2] + " was not a valid number: " + nfe.getLocalizedMessage());
        }

        if (args.length == 3) return stack;

        String nbtString = args[3];
        try {
            stack.setTagCompound(JsonToNBT.getTagFromJson(nbtString));
        } catch (NBTException e) {
            BCLog.logger.warn("[lib.markdown] " + nbtString + " was not a valid nbt tag: " + e.getLocalizedMessage());
        }
        return stack;
    }

    @Override
    public GuidePage createNew(GuiGuide gui) {
        List<GuidePart> parts = new ArrayList<>();
        for (GuidePartFactory<?> factory : factories) {
            parts.add(factory.createNew(gui));
        }
        return new GuidePage(gui, parts, this, I18n.format(title));
    }
}
