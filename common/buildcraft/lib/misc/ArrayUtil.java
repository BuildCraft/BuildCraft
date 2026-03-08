/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.*;

public class ArrayUtil {
    public static <F, T> T[] map(F[] from, Function<F, T> mapper, IntFunction<T[]> arrayConstructor) {
        T[] array = arrayConstructor.apply(from.length);
        int i = from.length;
        while (i-- > 0) {
            array[i] = mapper.apply(from[i]);
        }
        return array;
    }

    public static <T> int[] mapToInt(T[] from, ToIntFunction<T> mapper) {
        int[] array = new int[from.length];
        int i = from.length;
        while (i-- > 0) {
            array[i] = mapper.applyAsInt(from[i]);
        }
        return array;
    }

    public static <T> boolean testForAny(T[] array, Predicate<T> predicate) {
        for (T t : array) {
            if (predicate.test(t)) {
                return true;
            }
        }
        return false;
    }

    public static <T> boolean testForAll(T[] array, Predicate<T> predicate) {
        for (T t : array) {
            if (!predicate.test(t)) {
                return false;
            }
        }
        return true;
    }

    public static <T> int manualHash(T[] toHash, ToIntFunction<T> hasher) {
        int[] hashArray = mapToInt(toHash, hasher);
        return Arrays.hashCode(hashArray);
    }

    public static <T> boolean manualEquals(T[] a, T[] b, BiPredicate<T, T> equalityChecker) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        if (a.length != b.length) return false;
        int i = a.length;
        while (i-- > 0) {
            if (!equalityChecker.test(a[i], b[i])) {
                return false;
            }
        }
        return true;
    }

    /** Like {@link Collections#addAll(Collection, Object...)}, but adds the array in reverse order. */
    public static <T> void addAllReversed(Collection<T> dest, T[] src) {
        for (int i = src.length - 1; i >= 0; i--) {
            dest.add(src[i]);
        }
    }
}
