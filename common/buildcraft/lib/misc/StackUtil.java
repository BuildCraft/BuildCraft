package buildcraft.lib.misc;

import java.util.Collection;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.oredict.OreDictionary;

/** Provides various utils for interacting with {@link ItemStack}, and multiples. */
public class StackUtil {
    /** Checks to see if the two input stacks are equal in all but stack size. Note that this doesn't check anything
     * todo with stack size, so if you pass in two stacks of 64 cobblestone this will return true. If you pass in null
     * (at all) then this will only return true if both are null. */
    public static boolean canMerge(ItemStack a, ItemStack b) {
        // Checks item, damage
        if (!ItemStack.areItemsEqual(a, b)) {
            return false;
        }
        // checks tags and caps
        return ItemStack.areItemStackTagsEqual(a, b);
    }

    /** Attempts to get an item stack that might place down the given blockstate. Obviously this isn't perfect, and so
     * cannot be relied on for anything more than simple blocks. */
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

    /** Checks to see if the given required stack is contained fully in the given container stack. */
    public static boolean contains(ItemStack required, ItemStack container) {
        if (canMerge(required, container)) {
            return container.stackSize >= required.stackSize;
        }
        return false;
    }

    /** Checks to see if the given required stack is contained fully in a single stack in a list. */
    public static boolean contains(ItemStack required, Collection<ItemStack> containers) {
        for (ItemStack possible : containers) {
            if (contains(required, possible)) {
                return true;
            }
        }
        return false;
    }

    /** Checks to see if the given required stacks are all contained within the collection of containers. Note that this
     * assumes that all of the required stacks are different. */
    public static boolean containsAll(Collection<ItemStack> required, Collection<ItemStack> containers) {
        for (ItemStack req : required) {
            if (!contains(req, containers)) {
                return false;
            }
        }
        return true;
    }

    public static NBTTagCompound stripNonFunctionNbt(ItemStack from) {
        NBTTagCompound nbt = NBTUtils.getItemData(from).copy();
        if (nbt.getSize() == 0) {
            return nbt;
        }
        // TODO: Remove all of the non functional stuff (name, desc, etc)
        return nbt;
    }

    public static boolean doesStackNbtMatch(ItemStack target, ItemStack with) {
        NBTTagCompound nbtTarget = stripNonFunctionNbt(target);
        NBTTagCompound nbtWith = stripNonFunctionNbt(with);
        return nbtTarget.equals(nbtWith);
    }

    public static boolean doesEitherStackMatch(ItemStack stackA, ItemStack stackB) {
        return OreDictionary.itemMatches(stackA, stackB, false) || OreDictionary.itemMatches(stackB, stackA, false);
    }
}
