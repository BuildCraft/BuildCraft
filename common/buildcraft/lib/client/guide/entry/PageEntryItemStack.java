package buildcraft.lib.client.guide.entry;

import java.util.List;

import javax.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import net.minecraftforge.fml.common.registry.ForgeRegistries;

import buildcraft.api.registry.IScriptableRegistry.IEntryDeserializer;
import buildcraft.api.registry.IScriptableRegistry.OptionallyDisabled;

import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.client.guide.data.JsonTypeTags;
import buildcraft.lib.client.guide.loader.MarkdownPageLoader;
import buildcraft.lib.client.guide.parts.contents.PageLinkItemStack;
import buildcraft.lib.gui.GuiStack;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.misc.GuiUtil;
import buildcraft.lib.misc.ItemStackKey;
import buildcraft.lib.registry.RegistryConfig;

public class PageEntryItemStack extends PageEntry<ItemStackValueFilter> {

    private static final JsonTypeTags TAGS = new JsonTypeTags("buildcraft.guide.contents.item_stacks");

    public static final IEntryIterable ITERABLE = PageEntryItemStack::iterateAllDefault;
    public static final IEntryDeserializer<PageEntryItemStack> DESERIALISER = PageEntryItemStack::deserialize;

    private static void iterateAllDefault(IEntryLinkConsumer consumer) {
        for (Item item : ForgeRegistries.ITEMS) {
            if (!GuideManager.INSTANCE.objectsAdded.add(item)) {
                continue;
            }
            NonNullList<ItemStack> stacks = NonNullList.create();
            item.getSubItems(CreativeTabs.SEARCH, stacks);
            for (int i = 0; i < stacks.size(); i++) {
                ItemStack stack = stacks.get(i);

                try {
                    consumer.addChild(TAGS, new PageLinkItemStack(false, stack));
                } catch (RuntimeException e) {
                    throw new Error("Failed to create a page link for " + item.getRegistryName() + " " + item.getClass()
                        + " (" + stack.serializeNBT() + ")", e);
                }
                if (i > 50) {
                    // Woah there, lets not fill up entire pages with what is
                    // most likely the same item
                    break;
                }
            }
        }
    }

    private static OptionallyDisabled<PageEntryItemStack> deserialize(ResourceLocation name, JsonObject json,
        JsonDeserializationContext ctx) {
        JsonElement jStack = json.get("stack");
        if (jStack == null) {
            throw new JsonSyntaxException(
                "Expected either a string or an object for 'stack', but got nothing for " + json);
        }
        final ItemStack stack;
        final boolean matchMeta, matchNbt;
        if (jStack.isJsonPrimitive()) {
            String str = JsonUtils.getString(jStack, "stack");
            if (str.startsWith("{") && str.endsWith("}")) {
                stack = MarkdownPageLoader.loadComplexItemStack(str.substring(1, str.length() - 1));
                stack.setCount(1);
                matchMeta = true;
                matchNbt = stack.hasTagCompound();
            } else {
                if (str.startsWith("(") && str.endsWith(")")) {
                    str = str.substring(1, str.length() - 1);
                }
                ResourceLocation loc = new ResourceLocation(str);
                Item item = ForgeRegistries.ITEMS.getValue(loc);
                if (item == null) {
                    if (RegistryConfig.hasItemBeenDisabled(loc)) {
                        return new OptionallyDisabled<>("The item '" + loc + "' has been disabled.");
                    }
                    throw new JsonSyntaxException("Invalid item: " + str);
                }
                stack = new ItemStack(item);
                matchMeta = false;
                matchNbt = false;
            }
        } else {
            // TODO!
            throw new AbstractMethodError("// TODO: Implement this!");
        }
        if (stack.isEmpty()) {
            throw new JsonSyntaxException("Unknown item " + jStack);
        }
        ItemStackValueFilter filter = new ItemStackValueFilter(new ItemStackKey(stack), matchMeta, matchNbt);
        TextComponentString title = new TextComponentString(stack.getDisplayName());
        return new OptionallyDisabled<>(new PageEntryItemStack(name, json, filter, title, ctx));
    }

    public PageEntryItemStack(ResourceLocation name, JsonObject json, ItemStackValueFilter value, ITextComponent title,
        JsonDeserializationContext ctx) throws JsonParseException {
        super(name, json, value, title, ctx);
    }

    public PageEntryItemStack(ResourceLocation name, JsonObject json, ItemStackValueFilter value,
        JsonDeserializationContext ctx) throws JsonParseException {
        super(name, json, value, ctx);
    }

    public PageEntryItemStack(JsonTypeTags typeTags, ResourceLocation book, ITextComponent title,
        ItemStackValueFilter value) {
        super(typeTags, book, title, value);
    }

    @Override
    public List<String> getTooltip() {
        return GuiUtil.getFormattedTooltip(value.stack.baseStack);
    }

    @Override
    public boolean matches(Object obj) {
        if (obj instanceof ItemStackKey) {
            obj = ((ItemStackKey) obj).baseStack;
        }
        if (obj instanceof ItemStack) {
            ItemStack base = value.stack.baseStack;
            ItemStack test = (ItemStack) obj;
            if (base.isEmpty() || test.isEmpty()) {
                return false;
            }
            if (base.getItem() != test.getItem()) {
                return false;
            }
            if (value.matchMeta) {
                if (base.getMetadata() != test.getMetadata()) {
                    return false;
                }
            }
            if (value.matchNbt) {
                if (!ItemStack.areItemStackTagsEqual(base, test)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    @Nullable
    public ISimpleDrawable createDrawable() {
        return new GuiStack(value.stack.baseStack);
    }

    @Override
    public Object getBasicValue() {
        return value.stack.baseStack.getItem();
    }
}
