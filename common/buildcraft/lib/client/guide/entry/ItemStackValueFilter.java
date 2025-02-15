package buildcraft.lib.client.guide.entry;

import buildcraft.lib.misc.ItemStackKey;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public class ItemStackValueFilter {
    public final ItemStackKey stack;

    public final boolean matchNbt;
    public final boolean matchMeta;

    public ItemStackValueFilter(ItemStack stack) {
//        this(new ItemStackKey(stack), stack.getHasSubtypes(), false);
        this(new ItemStackKey(stack), false, false);
    }

    public ItemStackValueFilter(ItemStackKey stack, boolean matchMeta, boolean matchNbt) {
        this.stack = stack;
        this.matchNbt = matchNbt;
        this.matchMeta = matchMeta;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != getClass()) {
            return false;
        }
        ItemStackValueFilter other = (ItemStackValueFilter) obj;
        return stack.equals(other.stack) && matchMeta == other.matchMeta && matchNbt == other.matchNbt;
    }

    @Override
    public int hashCode() {
        return Objects.hash(stack, matchNbt, matchMeta);
    }

    @Override
    public String toString() {
        String matchString;
        if (matchMeta) {
            matchString = matchNbt ? "Matching meta+NBT of " : "Matching meta of ";
        } else if (matchNbt) {
            matchString = "Matching NBT of ";
        } else {
            matchString = "";
        }
        return matchString + stack.baseStack;
    }
}
