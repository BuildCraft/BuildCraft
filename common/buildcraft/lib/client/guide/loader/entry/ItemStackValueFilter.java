package buildcraft.lib.client.guide.loader.entry;

import java.util.Objects;

import buildcraft.lib.misc.ItemStackKey;

public class ItemStackValueFilter {
    public final ItemStackKey stack;

    public final boolean matchNbt;
    public final boolean matchMeta;

    public ItemStackValueFilter(ItemStackKey stack, boolean matchNbt, boolean matchMeta) {
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
}
