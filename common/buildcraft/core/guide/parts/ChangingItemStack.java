package buildcraft.core.guide.parts;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class ChangingItemStack {
    private final ItemStack[] stacks;
    private final int metas;

    public ChangingItemStack(ItemStack stack) {
        if (stack == null) {
            stacks = new ItemStack[] { stack };
            metas = 1;
        } else if (stack.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
            List<ItemStack> stacks = Lists.newArrayList();
            stack.getItem().getSubItems(stack.getItem(), null, stacks);
            this.stacks = stacks.toArray(new ItemStack[stacks.size()]);
            metas = stacks.size();
        } else {
            stacks = new ItemStack[] { stack };
            metas = 1;
        }
    }

    public ItemStack get() {
        long now = System.currentTimeMillis();
        int meta = (int) (now / 1000) % metas;
        return stacks[meta];
    }
}
