package buildcraft.lib.misc;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.*;

import net.minecraft.item.ItemStack;

public class ArrayUtil {
    public static ItemStack[] toArray(Collection<ItemStack> collection) {
        return collection.toArray(new ItemStack[collection.size()]);
    }

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
        if (a == null) return b == null;
        if (b == null) return false;
        if (a.length != b.length) return false;
        int i = a.length;
        while (i-- > 0) {
            if (!equalityChecker.test(a[i], b[i])) {
                return false;
            }
        }
        return true;
    }
}
