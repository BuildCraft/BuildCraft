package buildcraft.lib.client.guide.parts;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import net.minecraftforge.oredict.OreDictionary;

import buildcraft.lib.misc.StackUtil;

/** Defines an {@link ItemStack} that changes between a specified list of stacks. Useful for displaying possible inputs
 * or outputs for recipes that use the oredictionary, or recipes that vary the output depending on the metadata of the
 * input (for example a pipe colouring recipe) */
public class ChangingItemStack {
    private final NonNullList<ItemStack> stacks;

    /**
     * Creates a changing item stack that iterates through 
     * @param stack
     */
    public ChangingItemStack(@Nonnull ItemStack stack) {
        if (stack.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
            NonNullList<ItemStack> subs = NonNullList.create();
            stack.getItem().getSubItems(stack.getItem(), null, subs);
            this.stacks = subs;
        } else {
            stacks = StackUtil.listOf(stack);
        }
    }

    public ItemStack get() {
        long now = System.currentTimeMillis();
        int meta = (int) (now / 1000) % stacks.size();
        return stacks.get(meta);
    }
}
