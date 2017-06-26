package buildcraft.lib.misc.collect;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

/** An {@link TypedMap} instance that only maps classes directly to the classes - that is {@link #get(Class)} will
 * return either null, or an object whose {@link #getClass()} equals the argument class. */
public class TypedMapDirect<V> implements TypedMap<V> {

    private final Map<Class<?>, V> internalMap = new HashMap<>();

    @Override
    @Nullable
    public <T extends V> T get(Class<T> clazz) {
        T val = (T) internalMap.get(clazz);
        if (val != null) {
            return val;
        }
        return null;
    }

    @Override
    public <T extends V> void put(T value) {
        internalMap.put(value.getClass(), value);
    }

    @Override
    public void clear() {
        internalMap.clear();
    }

    @Override
    public <T extends V> void remove(T value) {
        internalMap.remove(value.getClass(), value);
    }

    @Override
    public Collection<V> getValues() {
        return internalMap.values();
    }
}
