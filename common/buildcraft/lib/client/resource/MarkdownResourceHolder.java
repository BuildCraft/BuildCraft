package buildcraft.lib.client.resource;

import java.util.*;

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

public class MarkdownResourceHolder extends StringResourceHolder implements GuidePartFactory<GuidePage> {
    public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.markdown") || World.class.getName().contains("World");
    private List<GuidePartFactory<?>> factories = null;

    public MarkdownResourceHolder(ResourceLocation location) {
        super(location);
    }

    @Override
    public void onStringChange() {
        List<GuidePartFactory<?>> newFactories = new ArrayList<>();
        List<String> lines = new ArrayList<>(getLines());
        while (!lines.isEmpty()) {
            String first = lines.remove(0);
            GuidePartFactory<?> factory = turnLineIntoPart(first, lines);
            if (factory != null) {
                newFactories.add(factory);
            }
        }
        factories = newFactories;
    }

    public static GuidePartFactory<?> turnLineIntoPart(final String line, List<String> after) {
        GuidePartFactory<?> factory = null;

        // Ignore comments
        if (line.startsWith("//")) return null;

        factory = loadImageLine(line);
        if (factory != null) return factory;

        factory = loadSpecialLine(line);
        if (factory != null) return factory;

        // Attempt to load a "list" from all of the given lines

        // We failed to find a factory for any of the special, so lets just interpret it as a raw string
        return loadRawString(line);
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

    private static GuidePartFactory<?> loadImageLine(String line) {
        // ![path/to/image]
        // ![path/to/image](width, height)

        if (line.startsWith("![")) {
            if (line.endsWith("]")) {
                return loadDefaultImage(line.substring(2, line.length() - 1));
            } else if (line.endsWith(")")) {
                String middle = line.substring(2, line.length() - 1);
                int index = middle.indexOf("](");
                String loc = middle.substring(0, index);
                String args = middle.substring(index + 2);
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

    private static GuidePartFactory<?> loadSpecialLine(String line) {
        // $[special.new_page]
        // $[special.crafting]...
        // $[special.smelting]...
        if (line.equals("$[special.new_page]")) {
            return GuidePartNewPage::new;
        } else if (line.startsWith("$[special.crafting]")) {
            return loadCraftingLine(line.substring("$[special.crafting]".length()));
        } else if (line.startsWith("$[special.smelting]")) {
            return loadSmeltingLine(line.substring("$[special.smelting]".length()));
        }
        return null;
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
        Item item = Item.getByNameOrId(args[0]);
        if (item != null) {
            stack = new ItemStack(item);
        } else {
            BCLog.logger.warn("[lib.markdown] " + args[0] + " was not a valid item!");
            return null;
        }

        if (args.length == 1) return stack;

        int stackSize = 1;
        try {
            stackSize = Integer.parseInt(args[1]);
        } catch (NumberFormatException nfe) {
            BCLog.logger.warn("[lib.markdown] " + args[1] + " was not a valid number: " + nfe.getLocalizedMessage());
        }
        stack.stackSize = stackSize;

        if (args.length == 2) return stack;

        try {
            int meta = Integer.parseInt(args[2]);
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
        return new GuidePage(gui, parts, this);
    }
}
