package buildcraft.lib.misc;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;

public class StackUtil {
    public static boolean canMerge(ItemStack a, ItemStack b) {
        // Checks item, damage
        if (!ItemStack.areItemsEqual(a, b)) {
            return false;
        }
        // checks tags and caps
        return ItemStack.areItemStackTagsEqual(a, b);
    }

    public static ItemStack getItemStackForState(IBlockState state) {
        Block b = state.getBlock();
        ItemStack stack = new ItemStack(b);
        if (stack.getItem() == null) {
            return null;
        }
        if (stack.getHasSubtypes()) {
            stack = new ItemStack(stack.getItem(), 1, b.getMetaFromState(state));
        }
        return stack;
    }
}
