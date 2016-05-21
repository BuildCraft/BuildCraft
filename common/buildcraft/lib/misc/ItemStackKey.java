package buildcraft.lib.misc;

import java.util.Objects;

import net.minecraft.item.ItemStack;

public class ItemStackKey {
    private final ItemStack baseStack;
    private final int hash;

    public ItemStackKey(ItemStack stack) {
        if (stack == null) {
            baseStack = null;
            hash = 0;
        } else {
            this.baseStack = stack.copy();
            this.hash = Objects.hash(stack.getItem(), stack.getItemDamage(), stack.getMetadata(), stack.getTagCompound());
        }
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != this.getClass()) return false;
        ItemStackKey other = (ItemStackKey) obj;
        if (baseStack == null) return other.baseStack == null;
        else if (other.baseStack == null) return false;
        else if (baseStack.getItem() != other.baseStack.getItem()) return false;
        else if (baseStack.getItemDamage() != other.baseStack.getItemDamage()) return false;
        else if (baseStack.getMetadata() != other.baseStack.getMetadata()) return false;
        else return ItemStack.areItemStackTagsEqual(baseStack, other.baseStack);
    }

    @Override
    public String toString() {
        return "Stack Key " + baseStack;
    }
}
