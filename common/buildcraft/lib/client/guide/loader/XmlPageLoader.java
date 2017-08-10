package buildcraft.lib.client.guide.loader;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.util.text.TextFormatting;

import net.minecraftforge.oredict.OreDictionary;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.InvalidInputDataException;

import buildcraft.lib.client.guide.PageEntry;
import buildcraft.lib.client.guide.PageLine;
import buildcraft.lib.client.guide.parts.GuideChapterWithin;
import buildcraft.lib.client.guide.parts.GuidePage;
import buildcraft.lib.client.guide.parts.GuidePageFactory;
import buildcraft.lib.client.guide.parts.GuidePart;
import buildcraft.lib.client.guide.parts.GuidePartFactory;
import buildcraft.lib.client.guide.parts.GuidePartMulti;
import buildcraft.lib.client.guide.parts.GuidePartNewPage;
import buildcraft.lib.client.guide.parts.GuideText;
import buildcraft.lib.client.guide.parts.recipe.IStackRecipes;
import buildcraft.lib.client.guide.parts.recipe.RecipeLookupHelper;
import buildcraft.lib.expression.Tokenizer.ITokenizingContext;
import buildcraft.lib.expression.Tokenizer.ResultConsume;
import buildcraft.lib.expression.Tokenizer.TokenResult;
import buildcraft.lib.expression.TokenizerDefaults;

// This isn't a proper XML loader - there isn't a root tag.
// Instead it just assumes everything is a paragraph, unless more specific tags are given
public enum XmlPageLoader implements IPageLoaderText {
    INSTANCE;

    public static final Map<String, SpecialParser> TAG_FACTORIES = new HashMap<>();
    public static final Map<String, MultiPartJoiner> GUIDE_PART_MULTIS = new HashMap<>();
    
    public static boolean SHOW_LORE = true;
    public static boolean SHOW_HINTS = false;

    @FunctionalInterface
    public interface SpecialParser {
        List<GuidePartFactory> parse(XmlTag tag);
    }

    @FunctionalInterface
    public interface SpecialParserSingle extends SpecialParser {
        @Override
        default List<GuidePartFactory> parse(XmlTag tag) {
            GuidePartFactory single = parseSingle(tag);
            if (single == null) return null;
            return ImmutableList.of(single);
        }

        GuidePartFactory parseSingle(XmlTag tag);
    }

    @FunctionalInterface
    public interface MultiPartJoiner {
        GuidePartFactory join(List<GuidePartFactory> factories);
    }

    static {
        // Note that text is done seperatly, so its not registered here
        putDuelMultiPartType("lore", () -> SHOW_LORE);
        putDuelMultiPartType("description", () -> true);
        putDuelMultiPartType("detail", () -> false);
        putDuelMultiPartType("hint", () -> SHOW_HINTS);
        putSingle("new_page", attr -> GuidePartNewPage::new);
        putSingle("chapter", XmlPageLoader::loadChapter);
        putSingle("recipe", XmlPageLoader::loadRecipe);
        putMulti("recipes", XmlPageLoader::loadAllRecipes);
        putMulti("usages", XmlPageLoader::loadAllUsages);
        putMulti("recipes_usages", XmlPageLoader::loadAllRecipesAndUsages);
    }

    private static void putDuelMultiPartType(String name, BooleanSupplier isVisible) {
        putSimpleMultiPartType(name, isVisible);
        putSimpleMultiPartType("no_" + name, () -> !isVisible.getAsBoolean());
    }

    private static void putSimpleMultiPartType(String name, BooleanSupplier isVisible) {
        putMultiPartType(name, (factories) -> (gui) -> {
            List<GuidePart> subParts = new ArrayList<>(factories.size());
            for (GuidePartFactory factory : factories) {
                subParts.add(factory.createNew(gui));
            }
            return new GuidePartMulti(gui, subParts, isVisible);
        });
    }

    private static void putMultiPartType(String name, MultiPartJoiner joiner) {
        GUIDE_PART_MULTIS.put(name, joiner);
    }

    private static void putSingle(String string, SpecialParserSingle parser) {
        putMulti(string, parser);
    }

    private static void putMulti(String string, SpecialParser parser) {
        TAG_FACTORIES.put(string, parser);
    }

    @Override
    public GuidePageFactory loadPage(BufferedReader reader, PageEntry entry) throws IOException {
        // Needs to support:
        // - start/end tags (such as <lore></lore>)
        // - nested tags (such as <lore>Spooky<bold> Skeletens</bold></lore>)
        // full tag set:
        // - Multi-nestling
        // - - <lore>
        // - - <description>
        // - - <hint>
        // - - <note>
        // - - <recipes>
        // - Compressed into single (text)
        // - - <bold>
        // - - <italic>
        // - - <underline>
        // - - <strikethrough>
        // - Other
        // - - <new_page/>
        // - - <chapter name="some.i18n.name"/>
        // - - <recipe stack="" type=""/>
        // - - <recipes stack=""/>
        // - - <usages stack=""/>
        // - - <recipes_usages stack=""/>
        // throw new AbstractMethodError("// TODO: Implement this!");

        Deque<List<GuidePartFactory>> nestedParts = new ArrayDeque<>();
        Deque<String> nestedTags = new ArrayDeque<>();
        nestedParts.push(new ArrayList<>());
        String line;
        while ((line = reader.readLine()) != null) {
            XmlTag tag = parseTag(line);
            if (tag != null) {
                if (tag.state == XmlTagState.COMPLETE) {
                    SpecialParser parser = TAG_FACTORIES.get(tag.name);
                    if (parser != null) {
                        nestedParts.peek().addAll(parser.parse(tag));
                        continue;
                    }
                } else if (tag.state == XmlTagState.START) {
                    MultiPartJoiner joiner = GUIDE_PART_MULTIS.get(tag.name);
                    if (joiner != null) {
                        nestedTags.push(tag.name);
                        nestedParts.push(new ArrayList<>());
                        line = line.substring(tag.originalString.length());
                    }
                } else /* tag.state == XmlTagState.END */ {
                    MultiPartJoiner joiner = GUIDE_PART_MULTIS.get(tag.name);
                    if (joiner != null) {
                        if (nestedTags.isEmpty()) {
                            throw new InvalidInputDataException("Tried to close " + tag.name + " before openining it!");
                        }
                        String name = nestedTags.pop();
                        if (!tag.name.equals(name)) {
                            throw new InvalidInputDataException(
                                "Tried to close " + tag.name + " before instead of " + name + "!");
                        }
                        List<GuidePartFactory> subParts = nestedParts.pop();
                        nestedParts.peek().add(joiner.join(subParts));
                        line = line.substring(tag.originalString.length());
                    }
                }
                if (line.length() == 0) {
                    continue;
                }
            }
            // Last: add remaining elements as text
            if (line.length() == 0) {
                line = " ";
            }
            Set<TextFormatting> formattingElements = EnumSet.noneOf(TextFormatting.class);
            Deque<TextFormatting> formatColours = new ArrayDeque<>();
            String completeLine = "";
            int i = 0;
            while (i < line.length()) {
                XmlTag currentTag = parseTag(line.substring(i));
                if (currentTag != null) {
                    TextFormatting formatting = TextFormatting.getValueByName(currentTag.name.replace("_", ""));
                    if (formatting != null) {
                        if (currentTag.state == XmlTagState.END) {
                            formattingElements.remove(formatting);
                            if (formatColours.peek() == formatting) {
                                formatColours.remove();
                            }
                        } else if (currentTag.state == XmlTagState.START) {
                            if (formatting.isColor()) {
                                formatColours.push(formatting);
                            } else {
                                formattingElements.add(formatting);
                            }
                        }
                        completeLine += TextFormatting.RESET;
                        if (formatColours.peek() != null) {
                            completeLine += formatColours.peek();
                        }
                        for (TextFormatting format : formattingElements) {
                            completeLine += format;
                        }
                        i += currentTag.originalString.length();
                        continue;
                    }
                }
                completeLine += line.charAt(i);
                i++;
            }

            final String modLine = completeLine;
            nestedParts.peek().add((gui) -> new GuideText(gui, modLine));
        }
        List<GuidePartFactory> factories = nestedParts.pop();
        if (nestedParts.size() != 0) {
            throw new InvalidInputDataException("We haven't closed " + nestedTags);
        }
        return (gui) -> {
            List<GuidePart> parts = new ArrayList<>();
            for (GuidePartFactory factory : factories) {
                parts.add(factory.createNew(gui));
            }
            String title = I18n.format(entry.title);
            return new GuidePage(gui, parts, title);
        };
    }

    /** Parses a single tag. Note that the tag might not be the length of the whole string. */
    @Nullable
    public static XmlTag parseTag(String string) throws InvalidInputDataException {
        if (!string.startsWith("<")) {
            return null;
        }

        // Its a tag, hopefully its complete
        int end = string.indexOf('>');
        if (end < 0) {
            throw new InvalidInputDataException("Didn't find an end tag for " + string);
        }
        String tagContents = string.substring(1, end);
        boolean hasStart = tagContents.startsWith("/");
        if (hasStart) {
            tagContents = tagContents.substring(1);
        }
        boolean hasEnd = tagContents.endsWith("/");
        if (hasEnd) {
            tagContents = tagContents.substring(0, tagContents.length() - 1);
        }
        int paramStart = tagContents.indexOf(' ');
        String tag;
        Map<String, String> attributes;
        if (paramStart < 0) {
            tag = tagContents;
            attributes = ImmutableMap.of();
        } else {
            tag = tagContents.substring(0, paramStart);
            attributes = new HashMap<>();
            String attribs = tagContents.substring(paramStart + 1);
            while (attribs.length() > 0) {
                attribs = attribs.trim();
                int index = attribs.indexOf('=');
                if (index < 0) {
                    break;
                }
                String key = attribs.substring(0, index);
                String after = attribs.substring(index + 1);
                ITokenizingContext tokenCtx = ITokenizingContext.createFromString(after);
                TokenResult result = TokenizerDefaults.GOBBLER_QUOTE.tokenizePart(tokenCtx);
                String value;
                if (result instanceof ResultConsume) {
                    value = tokenCtx.get(((ResultConsume) result).length);
                    value = value.substring(1, value.length() - 1);
                } else {
                    result = TokenizerDefaults.GOBBLER_WORD.tokenizePart(tokenCtx);
                    if (result instanceof ResultConsume) {
                        value = tokenCtx.get(((ResultConsume) result).length);
                    } else {
                        throw new InvalidInputDataException("Not a valid tag value " + after);
                    }
                }
                attributes.put(key, value);
                attribs = attribs.substring(key.length() + value.length() + 1);
            }
        }
        XmlTagState state;
        if (hasEnd) {
            state = XmlTagState.COMPLETE;
        } else if (hasStart) {
            state = XmlTagState.END;
        } else {
            state = XmlTagState.START;
        }
        return new XmlTag(tag, attributes, state, string.substring(0, end + 1));
    }

    public enum XmlTagState {
        /** {@code <tag>} */
        START,
        /** {@code <tag/>} */
        COMPLETE,
        /** {@code </tag>} */
        END;
    }

    public static class XmlTag {
        public final String name;
        public final Map<String, String> attributes;
        public final XmlTagState state;
        public final String originalString;

        public XmlTag(String name, Map<String, String> attributes, XmlTagState state, String originalString) {
            this.name = name;
            this.attributes = attributes;
            this.state = state;
            this.originalString = originalString;
        }

        @Nullable
        public String get(String key) {
            return attributes.get(key);
        }

        @Override
        public String toString() {
            return originalString;
        }
    }

    private static GuidePartFactory loadChapter(XmlTag tag) {
        String name = tag.get("name");
        if (name == null) {
            BCLog.logger.warn("[lib.guide.loader.xml] Found a chapter tag without a name!" + tag);
            return null;
        }
        return chapter(name);
    }

    private static GuidePartFactory loadRecipe(XmlTag tag) {
        ItemStack stack = loadItemStack(tag);
        if (stack == null) {
            return null;
        }
        String type = tag.get("type");
        if (type == null) {
            IStackRecipes recipes = RecipeLookupHelper.handlerTypes.get(type);
            if (recipes == null) {
                BCLog.logger.warn("[lib.guide.loader.xml] Unknown recipe type " + type + " - must be one of "
                    + RecipeLookupHelper.handlerTypes.keySet());
            } else {
                List<GuidePartFactory> list = recipes.getRecipes(stack);
                if (list.size() > 0) {
                    return list.get(0);
                }
            }
        }
        List<GuidePartFactory> list = RecipeLookupHelper.getAllRecipes(stack);
        if (list.isEmpty()) {
            return null;
        } else {
            return list.get(0);
        }
    }

    private static List<GuidePartFactory> loadAllRecipes(XmlTag tag) {
        ItemStack stack = loadItemStack(tag);
        if (stack == null) {
            return null;
        }
        return RecipeLookupHelper.getAllRecipes(stack);
    }

    private static List<GuidePartFactory> loadAllUsages(XmlTag tag) {
        ItemStack stack = loadItemStack(tag);
        if (stack == null) {
            return null;
        }
        return RecipeLookupHelper.getAllUsages(stack);
    }

    private static List<GuidePartFactory> loadAllRecipesAndUsages(XmlTag tag) {
        ItemStack stack = loadItemStack(tag);
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

    public static ItemStack loadItemStack(XmlTag tag) {
        String id = tag.get("stack");
        String count = tag.get("count");
        String data = tag.get("data");
        String nbt = tag.get("nbt");
        if (id == null) {
            BCLog.logger.warn("[lib.guide.loader.xml] Missing 'stack' for an itemstack from " + tag);
            return null;
        }
        ItemStack stack = null;
        Item item = Item.getByNameOrId(id.trim());
        if (item != null) {
            stack = new ItemStack(item);
        } else {
            BCLog.logger.warn("[lib.guide.loader.xml] " + id + " was not a valid item!");
            return null;
        }

        if (count != null) {
            int stackSize = 1;
            try {
                stackSize = Integer.parseInt(count.trim());
            } catch (NumberFormatException nfe) {
                BCLog.logger.warn("[lib.guide.loader.xml] " + count + " was not a valid number: " + nfe.getMessage());
            }
            stack.setCount(stackSize);
        }

        if (data != null) {
            try {
                int meta = Integer.parseInt(data.trim());
                if (meta == -1) {
                    // Use oredict
                    meta = OreDictionary.WILDCARD_VALUE;
                }
                stack = new ItemStack(stack.getItem(), stack.getCount(), meta);
            } catch (NumberFormatException nfe) {
                BCLog.logger.warn("[lib.guide.loader.xml] " + data + " was not a valid number: " + nfe.getMessage());
            }
        }

        if (nbt != null) {
            try {
                stack.setTagCompound(JsonToNBT.getTagFromJson(nbt));
            } catch (NBTException e) {
                BCLog.logger.warn("[lib.guide.loader.xml] " + nbt + " was not a valid nbt tag: " + e.getMessage());
            }
        }
        return stack;
    }
}
