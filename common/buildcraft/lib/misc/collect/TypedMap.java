package buildcraft.lib.misc.collect;

import java.util.Map;

/** A specialisation of the {@link Map} interface, where the keys are {@link Class} and the values are instances of that
 * class.
 *
 * @param <V> The base type for all entries - only entries that extend this type are allowed into the map. */
public interface TypedMap<V> {
    <T extends V> T get(Class<T> clazz);

    <T extends V> void put(T value);

    void clear();

    <T extends V> void remove(T value);
}
