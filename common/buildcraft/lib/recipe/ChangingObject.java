package buildcraft.lib.recipe;

import java.util.Arrays;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import net.minecraftforge.oredict.OreDictionary;

import buildcraft.lib.misc.ArrayUtil;
import buildcraft.lib.misc.StackUtil;

public class ChangingObject<T> {
    protected final T[] options;
    private final int hash;

    public ChangingObject(T[] options) {
        this.options 
         = options;
        hash = computeHash();
    }

    protected int computeHash() {
        return Arrays.hashCode(options);
    }

    /** @return The {@link ItemStack} that should be displayed at the current time. */
    public T get() {
        return get(0);
    }

    public T get(int indexOffset) {
        long now = System.currentTimeMillis();
        int i = (int) (now / 1000) + indexOffset;
        return options[i % options.length];
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != getClass()) return false;
        ChangingObject<?> other = (ChangingObject<?>) obj;
        if (hash != other.hash) return false;
        return Arrays.equals(options, other.options);
    }
}
