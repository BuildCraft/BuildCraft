package buildcraft.lib.misc.data;

import java.util.AbstractList;

import javax.annotation.Nonnull;

import net.minecraft.util.NonNullList;

/** Defines a non-null 2 dimensional matrix, where the width and height are known at creation time. Note that this
 * matrix cannot be resized. */
public class NonNullMatrix<T> extends AbstractList<T> {
    private final NonNullList<T> internalList;
    private final int width, height;

    public NonNullMatrix(int width, int height, @Nonnull T fill) {
        this.width = width;
        this.height = height;
        internalList = NonNullList.withSize(width * height, fill);
    }

    /** Creates a {@link NonNullMatrix} from the given 2-dim array, replacing all null values with the given nonnull
     * replacement. */
    public NonNullMatrix(T[][] from, @Nonnull T nullReplacor) {
        this.width = from.length;
        this.height = width == 0 ? 0 : from[0].length;
        internalList = NonNullList.withSize(width * height, nullReplacor);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                T val = from[x][y];
                if (val == null) {
                    set(x, y, nullReplacor);
                } else {
                    set(x, y, val);
                }
            }
        }
    }

    private int flatIndexOf(int x, int y) {
        return x * height + y;
    }

    @Nonnull
    public T get(int x, int y) {
        return get(flatIndexOf(x, y));
    }

    @Nonnull
    public T set(int x, int y, @Nonnull T element) {
        return set0(flatIndexOf(x, y), element);
    }

    @Override
    @Nonnull
    public T get(int index) {
        return internalList.get(index);
    }

    @Override
    public int size() {
        return internalList.size();
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    @Override
    @Nonnull
    public T set(int index, T element) {
        if (element == null) {
            throw new NullPointerException("Element was null!");
        }
        return set0(index, element);
    }

    @Nonnull
    private T set0(int flatIndex, @Nonnull T element) {
        T val = internalList.set(flatIndex, element);
        if (val == null) throw new IllegalStateException("Somehow a value in the internal list was null!");
        return val;
    }
}
