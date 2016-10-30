package buildcraft.lib.misc;

import net.minecraft.item.ItemStack;

public class ItemStackKey {
    public final ItemStack baseStack;
    private final int hash;

    public ItemStackKey(ItemStack stack) {
        if (stack == null) {
            baseStack = null;
            hash = 0;
        } else {
            this.baseStack = stack.copy();
            this.hash = stack.serializeNBT().hashCode();
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
        if (hash != other.hash) return false;
        if (baseStack == null) return other.baseStack == null;
        if (other.baseStack == null) return false;
        return baseStack.serializeNBT().equals(other.baseStack.serializeNBT());
    }

    @Override
    public String toString() {
        return "Stack Key " + baseStack;
    }
}
