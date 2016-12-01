package buildcraft.lib.client.guide.parts;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.item.ItemStack;

import net.minecraftforge.oredict.OreDictionary;

/** An {@link ItemStack} that has several different possible values. */
public class ChangingItemStack {
    private final ItemStack[] stacks;

    public ChangingItemStack(ItemStack[] possible) {
        this.stacks = possible;
    }

    public ChangingItemStack(ItemStack stack) {
        if (stack == null) {
            stacks = new ItemStack[] { null };
        } else if (stack.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
            // Use a best-effort
            List<ItemStack> possible = Lists.newArrayList();
            stack.getItem().getSubItems(stack.getItem(), null, possible);
            this.stacks = possible.toArray(new ItemStack[possible.size()]);
        } else {
            stacks = new ItemStack[] { stack };
        }
    }

    public ItemStack get() {
        long now = System.currentTimeMillis();
        int meta = (int) (now / 1000) % stacks.length;
        return stacks[meta];
    }
}
