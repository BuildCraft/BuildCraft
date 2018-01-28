/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc.data;

import javax.annotation.Nullable;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;

/** Defines a non-null 2 dimensional matrix, where the width and height are known at creation time. Note that this
 * matrix cannot be resized. */
public class NonNullMatrix<T> extends AbstractList<T> {
    private final List<T> internalList;
    private final int width, height;

    public NonNullMatrix(int width, int height, @Nullable T fill) {
        this.width = width;
        this.height = height;
        internalList = listWithSize(width * height, fill);
    }

    public NonNullMatrix(int width, int height, IEntryFiller<T> filler) {
        this.width = width;
        this.height = height;
        internalList = listWithSize(width * height, filler.getEntry(0, 0));
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                internalList.set(flatIndexOf(x, y), filler.getEntry(x, y));
            }
        }
    }

    private List<T> listWithSize(int size, T fill){
        Object[] aobject = new Object[size];
        Arrays.fill(aobject, fill);
        return Arrays.asList((T[])aobject);
    }

    public interface IEntryFiller<T> {
        @Nullable
        T getEntry(int x, int y);
    }

    /** Creates a {@link NonNullMatrix} from the given 2-dim array, replacing all null values with the given nonnull
     * replacement. */
    public NonNullMatrix(T[][] from, T nullReplacer) {
        this.width = from.length;
        this.height = width == 0 ? 0 : from[0].length;
        internalList = listWithSize(width * height, nullReplacer);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                T val = from[x][y];
                if (val == null) {
                    set(x, y, nullReplacer);
                } else {
                    set(x, y, val);
                }
            }
        }
    }

    private int flatIndexOf(int x, int y) {
        return x * height + y;
    }

    @Nullable
    public T get(int x, int y) {
        return get(flatIndexOf(x, y));
    }

    @Nullable
    public T set(int x, int y, @Nullable T element) {
        return set0(flatIndexOf(x, y), element);
    }

    @Override
    @Nullable
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
    @Nullable
    public T set(int index, T element) {
        if (element == null) {
            throw new NullPointerException("Element was null!");
        }
        return set0(index, element);
    }

    @Nullable
    private T set0(int flatIndex, @Nullable T element) {
        return internalList.set(flatIndex, element);
    }
}
