package buildcraft.lib.client.guide.entry;

import java.util.List;

import javax.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.registry.ForgeRegistries;

import buildcraft.api.registry.IScriptableRegistry.OptionallyDisabled;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.client.guide.data.JsonTypeTags;
import buildcraft.lib.client.guide.loader.MarkdownPageLoader;
import buildcraft.lib.client.guide.loader.XmlPageLoader;
import buildcraft.lib.client.guide.parts.GuidePart;
import buildcraft.lib.client.guide.parts.contents.PageLinkItemStack;
import buildcraft.lib.gui.GuiStack;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.misc.GuiUtil;
import buildcraft.lib.misc.ItemStackKey;
import buildcraft.lib.registry.RegistryConfig;

public class PageEntryItemStack extends PageValueType<ItemStackValueFilter> {

    public static final PageEntryItemStack INSTANCE = new PageEntryItemStack();
    private static final JsonTypeTags TAGS = new JsonTypeTags("buildcraft.guide.contents.item_stacks");

    @Override
    public Class<ItemStackValueFilter> getEntryClass() {
        return ItemStackValueFilter.class;
    }

    @Override
    public void iterateAllDefault(IEntryLinkConsumer consumer) {
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

    @Override
    public OptionallyDisabled<PageEntry<ItemStackValueFilter>> deserialize(ResourceLocation name, JsonObject json,
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
        return new OptionallyDisabled<>(new PageEntry<>(this, name, json, filter));
    }

    @Override
    public String getTitle(ItemStackValueFilter value) {
        return value.stack.baseStack.getDisplayName();
    }

    @Override
    public List<String> getTooltip(ItemStackValueFilter value) {
        return GuiUtil.getFormattedTooltip(value.stack.baseStack);
    }

    @Override
    public boolean matches(ItemStackValueFilter entry, Object obj) {
        if (obj instanceof ItemStackValueFilter) {
            obj = ((ItemStackValueFilter) obj).stack.baseStack;
        }
        if (obj instanceof ItemStackKey) {
            obj = ((ItemStackKey) obj).baseStack;
        }
        if (obj instanceof ItemStack) {
            ItemStack base = entry.stack.baseStack;
            ItemStack test = (ItemStack) obj;
            if (base.isEmpty() || test.isEmpty()) {
                return false;
            }
            if (base.getItem() != test.getItem()) {
                return false;
            }
            if (entry.matchMeta) {
                if (base.getMetadata() != test.getMetadata()) {
                    return false;
                }
            }
            if (entry.matchNbt) {
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
    public ISimpleDrawable createDrawable(ItemStackValueFilter value) {
        return new GuiStack(value.stack.baseStack);
    }

    @Override
    public Object getBasicValue(ItemStackValueFilter value) {
        return value.stack.baseStack.getItem();
    }

    @Override
    public void addPageEntries(ItemStackValueFilter value, GuiGuide gui, List<GuidePart> parts) {
        XmlPageLoader.appendAllCrafting(value.stack.baseStack, parts, gui);
    }
}
