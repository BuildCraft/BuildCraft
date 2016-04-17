package buildcraft.lib.guide;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import buildcraft.api.core.BCLog;
import buildcraft.lib.guide.node.NodePageLine;
import buildcraft.lib.guide.parts.*;

public class MarkdownLoader extends LocationLoader {
    public static GuidePartFactory<GuidePageBase> loadMarkdown(ResourceLocation location) {
        String[] lineArray = asString(location).split("\n");
        List<GuidePartFactory<?>> parts = getParts(lineArray);
        return new GuidePageFactory(parts);
    }

    private static List<GuidePartFactory<?>> getParts(String[] lineArray) {
        List<GuidePartFactory<?>> parts = Lists.newArrayList();

        for (String singleLine : lineArray) {
            parts.add(getPart(singleLine));
        }
        return parts;
    }

    private static GuidePartFactory<?> getPart(String singleLine) {
        String line = singleLine;
        GuidePartFactory<?> factory = null;
        // A link to something else. It might not be, but you never know
        if (line.startsWith("[")) {

        }

        // An image of something
        if (line.startsWith("![")) {
            line = line.substring(2);
            if (line.indexOf("]") > 2) {
                String location = line.substring(0, line.indexOf("]"));
                if (line.endsWith("]")) {
                    line = line.substring(0, line.length() - 1);
                    factory = ImageLoader.loadImage(new ResourceLocation(location + ".png"), -1, -1);
                } else if (line.endsWith(")") && line.indexOf("](") > 2) {
                    String meta = line.substring(line.indexOf("]("), line.length() - 1);
                    String[] args = meta.split(",");
                    if (args.length == 2) {
                        try {
                            int width = Integer.parseInt(args[0]);
                            int height = Integer.parseInt(args[1]);
                            factory = ImageLoader.loadImage(new ResourceLocation(location + ".png"), width, height);
                        } catch (NumberFormatException nfe) {
                            BCLog.logger.warn(nfe);
                            line = "![" + line;
                        }
                    } else {
                        line = "![" + line;
                    }
                } else {
                    line = "![" + line;
                }
            } else {
                line = "![" + line;
            }
        }

        // Something special and custom to BuildCraft (not in standard markdown)
        if (line.startsWith("$[special.")) {
            line = line.substring("$[special.".length());
            if (line.startsWith("crafting]")) {
                line = line.substring("crafting]".length());
                // Recipe for a simple item
                if (line.startsWith("(") && line.endsWith(")")) {
                    String itemText = line.substring(1, line.length() - 1);
                    Item item = Item.getByNameOrId(itemText);
                    if (item != null) {
                        GuideCraftingFactory craftingFactory = GuideCraftingFactory.create(item);
                        if (craftingFactory != null) {
                            factory = craftingFactory;
                        } else {
                            BCLog.logger.warn("Didn't find a recipe for " + item);
                            // Unwrap it to what it was before
                            line = "$[special.crafting]" + line;
                        }
                    } else {
                        BCLog.logger.warn("Didn't find an item for " + itemText);
                        // Unwrap it to what it was before
                        line = "$[special.crafting]" + line;
                    }
                }
                // Recipe for a complex item
                else if (line.startsWith("{") && line.endsWith("}")) {
                    line = line.substring(1, line.length() - 1);
                    ItemStack stack = convertComplexItemStack(line);
                    if (stack != null) {
                        GuideCraftingFactory craftingFactory = GuideCraftingFactory.create(stack);
                        if (craftingFactory != null) {
                            factory = craftingFactory;
                        } else {
                            BCLog.logger.warn("Didn't find a recipe for " + stack);
                            // Unwrap it to what it was before
                            line = "$[special.crafting]" + line;
                        }
                    } else {
                        BCLog.logger.warn("Didn't find an item for " + line);
                        // Unwrap it to what it was before
                        line = "$[special.crafting]{" + line + "}";
                    }
                } else {
                    // Unwrap back what was there before.
                    line = "crafting]" + line;
                }
            } else if (line.startsWith("smelting]")) {
                line = line.substring("smelting]".length());
                // Recipe for a simple item
                if (line.startsWith("(") && line.endsWith(")")) {
                    String itemText = line.substring(1, line.length() - 1);
                    Item item = Item.getByNameOrId(itemText);
                    if (item != null) {
                        GuideSmeltingFactory craftingFactory = GuideSmeltingFactory.create(item);
                        if (craftingFactory != null) {
                            factory = craftingFactory;
                        } else {
                            BCLog.logger.warn("Didn't find a recipe for " + item);
                            // Unwrap it to what it was before
                            line = "$[special.smelting]" + line;
                        }
                    } else {
                        BCLog.logger.warn("Didn't find an item for " + itemText);
                        // Unwrap it to what it was before
                        line = "$[special.smelting]" + line;
                    }
                }
                // Recipe for a complex item
                else if (line.startsWith("{") && line.endsWith("}")) {
                    line = line.substring(1, line.length() - 1);
                    ItemStack stack = convertComplexItemStack(line);
                    if (stack != null) {
                        GuideSmeltingFactory craftingFactory = GuideSmeltingFactory.create(stack);
                        if (craftingFactory != null) {
                            factory = craftingFactory;
                        } else {
                            BCLog.logger.warn("Didn't find a recipe for " + stack);
                            // Unwrap it to what it was before
                            line = "$[special.smelting]" + line;
                        }
                    } else {
                        BCLog.logger.warn("Didn't find an item for " + line);
                        // Unwrap it to what it was before
                        line = "$[special.smelting]{" + line + "}";
                    }
                } else {
                    // Unwrap back what was there before.
                    line = "smelting]" + line;
                }
            } else if (line.equals("new_page]")) {
                factory = GuidePartNewLine::new;
            } else {
                line = "$[special." + line;
            }
        }
        if (factory == null) {
            if (line.length() == 0) {
                line = " ";
            }

            // Make the entire line have an underline, like a title
            if (line.startsWith("# ")) {
                line = TextFormatting.UNDERLINE + line.substring(2);
            }
            // Just use it as a normal text line
            NodePageLine node = new NodePageLine(null, null);
            node.addChild(new PageLine(0, line, false));
            factory = (gui) -> new GuideText(gui, node);
        }
        return factory;
    }

    private static ItemStack convertComplexItemStack(String line) {
        String[] args = line.split(",");
        if (args.length == 0) {
            BCLog.logger.warn(line + " was not a valid item string!");
            return null;
        }
        ItemStack stack = null;
        Item item = Item.getByNameOrId(args[0]);
        if (item != null) {
            BCLog.logger.warn(args[0] + " was not a valid item!");
            stack = new ItemStack(item);
        } else {
            return null;
        }

        if (args.length == 1) return stack;

        int stackSize = 1;
        try {
            stackSize = Integer.parseInt(args[1]);
        } catch (NumberFormatException nfe) {
            BCLog.logger.warn(args[1] + " was not a valid number: " + nfe.getLocalizedMessage());
        }
        stack.stackSize = stackSize;

        if (args.length == 2) return stack;

        try {
            int meta = Integer.parseInt(args[2]);
            stack = new ItemStack(stack.getItem(), stack.stackSize, meta);
        } catch (NumberFormatException nfe) {
            BCLog.logger.warn(args[2] + " was not a valid number: " + nfe.getLocalizedMessage());
        }

        if (args.length == 3) return stack;

        String nbtString = args[3];
        try {
            stack.setTagCompound(JsonToNBT.getTagFromJson(nbtString));
        } catch (NBTException e) {
            BCLog.logger.warn(nbtString + " was not a valid nbt tag: " + e.getLocalizedMessage());
        }
        return stack;
    }
}
