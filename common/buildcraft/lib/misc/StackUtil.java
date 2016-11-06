package buildcraft.lib.misc;

import java.util.Collection;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.oredict.OreDictionary;

import buildcraft.api.items.IList;

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

    public static boolean canStacksOrListsMerge(ItemStack stack1, ItemStack stack2) {
        if (stack1 == null || stack2 == null) {
            return false;
        }
    
        if (stack1.getItem() instanceof IList) {
            IList list = (IList) stack1.getItem();
            return list.matches(stack1, stack2);
        } else if (stack2.getItem() instanceof IList) {
            IList list = (IList) stack2.getItem();
            return list.matches(stack2, stack1);
        }
    
        if (!stack1.isItemEqual(stack2)) {
            return false;
        }
        if (!ItemStack.areItemStackTagsEqual(stack1, stack2)) {
            return false;
        }
        return true;
    
    }

    /** Merges mergeSource into mergeTarget
     *
     * @param mergeSource - The stack to merge into mergeTarget, this stack is not modified
     * @param mergeTarget - The target merge, this stack is modified if doMerge is set
     * @param doMerge - To actually do the merge
     * @return The number of items that was successfully merged. */
    public static int mergeStacks(ItemStack mergeSource, ItemStack mergeTarget, boolean doMerge) {
        if (!canMerge(mergeSource, mergeTarget)) {
            return 0;
        }
        int mergeCount = Math.min(mergeTarget.getMaxStackSize() - mergeTarget.stackSize, mergeSource.stackSize);
        if (mergeCount < 1) {
            return 0;
        }
        if (doMerge) {
            mergeTarget.stackSize += mergeCount;
        }
        return mergeCount;
    }

    /* ITEM COMPARISONS */
    /** Determines whether the given ItemStack should be considered equivalent for crafting purposes.
     *
     * @param base The stack to compare to.
     * @param comparison The stack to compare.
     * @param oreDictionary true to take the Forge OreDictionary into account.
     * @return true if comparison should be considered a crafting equivalent for base. */
    public static boolean isCraftingEquivalent(ItemStack base, ItemStack comparison, boolean oreDictionary) {
        if (isMatchingItem(base, comparison, true, false)) {
            return true;
        }
    
        if (oreDictionary) {
            int[] idBase = OreDictionary.getOreIDs(base);
            if (idBase.length > 0) {
                for (int id : idBase) {
                    for (ItemStack itemstack : OreDictionary.getOres(OreDictionary.getOreName(id))) {
                        if (comparison.getItem() == itemstack.getItem() && (itemstack.getItemDamage() == OreDictionary.WILDCARD_VALUE || comparison.getItemDamage() == itemstack.getItemDamage())) {
                            return true;
                        }
                    }
                }
            }
        }
    
        return false;
    }

    public static boolean isCraftingEquivalent(int[] oreIDs, ItemStack comparison) {
        if (oreIDs.length > 0) {
            for (int id : oreIDs) {
                for (ItemStack itemstack : OreDictionary.getOres(OreDictionary.getOreName(id))) {
                    if (comparison.getItem() == itemstack.getItem() && (itemstack.getItemDamage() == OreDictionary.WILDCARD_VALUE || comparison.getItemDamage() == itemstack.getItemDamage())) {
                        return true;
                    }
                }
            }
        }
    
        return false;
    }

    public static boolean isMatchingItemOrList(final ItemStack base, final ItemStack comparison) {
        if (base == null || comparison == null) {
            return false;
        }
    
        if (base.getItem() instanceof IList) {
            IList list = (IList) base.getItem();
            return list.matches(base, comparison);
        } else if (comparison.getItem() instanceof IList) {
            IList list = (IList) comparison.getItem();
            return list.matches(comparison, base);
        }
    
        return isMatchingItem(base, comparison, true, false);
    }

    /** Compares item id, damage and NBT. Accepts wildcard damage. Ignores damage entirely if the item doesn't have
     * subtypes.
     *
     * @param base The stack to compare to.
     * @param comparison The stack to compare.
     * @return true if id, damage and NBT match. */
    public static boolean isMatchingItem(final ItemStack base, final ItemStack comparison) {
        return isMatchingItem(base, comparison, true, true);
    }

    /** This variant also checks damage for damaged items. */
    public static boolean isEqualItem(final ItemStack base, final ItemStack comparison) {
        if (isMatchingItem(base, comparison, false, true)) {
            return isWildcard(base) || isWildcard(comparison) || base.getItemDamage() == comparison.getItemDamage();
        } else {
            return false;
        }
    }

    /** Compares item id, and optionally damage and NBT. Accepts wildcard damage. Ignores damage entirely if the item
     * doesn't have subtypes.
     *
     * @param base ItemStack
     * @param comparison ItemStack
     * @param matchDamage
     * @param matchNBT
     * @return true if matches */
    public static boolean isMatchingItem(final ItemStack base, final ItemStack comparison, final boolean matchDamage, final boolean matchNBT) {
        if (base == null || comparison == null) {
            return false;
        }
    
        if (base.getItem() != comparison.getItem()) {
            return false;
        }
        if (matchDamage && base.getHasSubtypes()) {
            if (!isWildcard(base) && !isWildcard(comparison)) {
                if (base.getItemDamage() != comparison.getItemDamage()) {
                    return false;
                }
            }
        }
        if (matchNBT) {
            if (base.getTagCompound() != null && !base.getTagCompound().equals(comparison.getTagCompound())) {
                return false;
            }
        }
        return true;
    }

    public static boolean isWildcard(ItemStack stack) {
        return isWildcard(stack.getItemDamage());
    }

    public static boolean isWildcard(int damage) {
        return damage == -1 || damage == OreDictionary.WILDCARD_VALUE;
    }
}
