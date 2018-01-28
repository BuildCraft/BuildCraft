package buildcraft.lib.client.guide.loader.entry;

import buildcraft.api.items.BCStackHelper;
import buildcraft.lib.client.guide.loader.MarkdownPageLoader;
import buildcraft.lib.gui.GuiStack;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.misc.GuiUtil;
import buildcraft.lib.misc.ItemStackKey;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;

public class EntryTypeItem extends PageEntryType<ItemStackValueFilter> {

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
            if (!BCStackHelper.isEmpty(stack)) {
                stack.stackSize = 1;
                matchMeta = true;
                matchNbt = stack.hasTagCompound();
            }
            else{
                matchMeta = false;
                matchNbt = false;
            }
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
        if (BCStackHelper.isEmpty(stack)) {
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
            if (BCStackHelper.isEmpty(base) || BCStackHelper.isEmpty(test)) {
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
            return !target.matchNbt || ItemStack.areItemStackTagsEqual(base, test);
        }
        return false;
    }

    @Override
    @Nullable
    public ISimpleDrawable createDrawable(ItemStackValueFilter value) {
        return new GuiStack(value.stack.baseStack);
    }
}
