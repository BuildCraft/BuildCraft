package ct.buildcraft.lib.misc.collect;

import java.util.Map;

/** A specialisation of the {@link Map} interface, where the keys are {@link Class} and the values are instances of that
 * class.
 *
 * @param <V> The base type for all entries - only entries that extend this type are allowed into the map. */
public interface TypedMap<V> {
    <T extends V> T get(Class<T> clazz);

    void put(V value);

    void clear();

    void remove(V value);
}
