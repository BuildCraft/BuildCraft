package buildcraft.lib.client.guide.loader.entry;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.registry.ForgeRegistries;

import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.client.guide.data.JsonTypeTags;
import buildcraft.lib.client.guide.loader.MarkdownPageLoader;
import buildcraft.lib.client.guide.parts.contents.PageLinkItemStack;
import buildcraft.lib.gui.GuiStack;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.misc.GuiUtil;
import buildcraft.lib.misc.ItemStackKey;

public class EntryTypeItem extends PageEntryType<ItemStackValueFilter> {

    private static final JsonTypeTags TAGS = new JsonTypeTags("buildcraft.guide.contents.item_stacks");

    public static final String ID = "minecraft:item_stack";
    public static final EntryTypeItem INSTANCE = new EntryTypeItem();

    @Override
    @Nullable
    public ItemStackValueFilter deserialise(String source) {
        final ItemStack stack;
        final boolean matchMeta;
        final boolean matchNbt;
        if (source.startsWith("{") && source.endsWith("}")) {
            stack = MarkdownPageLoader.loadComplexItemStack(source.substring(1, source.length() - 1));
            stack.setCount(1);
            matchMeta = true;
            matchNbt = stack.hasTagCompound();
        } else {
            if (source.startsWith("(") && source.endsWith(")")) {
                source = source.substring(1, source.length() - 1);
            }
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(source));
            if (item == null) {
                return null;
            }
            stack = new ItemStack(item);
            matchMeta = false;
            matchNbt = false;
        }
        if (stack.isEmpty()) {
            return null;
        }
        return new ItemStackValueFilter(new ItemStackKey(stack), matchMeta, matchNbt);
    }

    @Override
    public List<String> getTooltip(ItemStackValueFilter value) {
        return GuiUtil.getFormattedTooltip(value.stack.baseStack);
    }

    @Override
    public boolean matches(ItemStackValueFilter target, Object value) {
        if (value instanceof ItemStackKey) {
            value = ((ItemStackKey) value).baseStack;
        }
        if (value instanceof ItemStack) {
            ItemStack base = target.stack.baseStack;
            ItemStack test = (ItemStack) value;
            if (base.isEmpty() || test.isEmpty()) {
                return false;
            }
            if (base.getItem() != test.getItem()) {
                return false;
            }
            if (target.matchMeta) {
                if (base.getMetadata() != test.getMetadata()) {
                    return false;
                }
            }
            if (target.matchNbt) {
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
}
