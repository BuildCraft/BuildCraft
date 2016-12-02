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

    /** Creates a stack list that iterates through all of the given stacks. This does NOT check possible variants.
     * 
     * @param stacks The list to iterate through. */
    public ChangingItemStack(NonNullList<ItemStack> stacks) {
        this.stacks = stacks;
    }

    /** Creates a changing item stack that iterates through all {@link OreDictionary} variants of the specified stack.
     * 
     * @param stack the stack to check. */
    public ChangingItemStack(@Nonnull ItemStack stack) {
        if (stack.isEmpty()) {
            stacks = StackUtil.listOf(StackUtil.EMPTY);
        } else if (stack.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
            NonNullList<ItemStack> subs = NonNullList.create();
            stack.getItem().getSubItems(stack.getItem(), null, subs);
            this.stacks = subs;
        } else {
            stacks = StackUtil.listOf(stack);
        }
    }

    /** @return The {@link ItemStack} that should be displayed at the current time. */
    public ItemStack get() {
        long now = System.currentTimeMillis();
        int meta = (int) (now / 1000) % stacks.size();
        return stacks.get(meta);
    }
}
