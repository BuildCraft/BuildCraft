package ct.buildcraft.lib.misc.collect;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.Nullable;

public class TypedKeyMap<K, V> {
    private final Supplier<TypedMap<V>> mapType;
    private final Map<K, TypedMap<V>> internalMap = new HashMap<>();

    public static <K, V> TypedKeyMap<K, V> createDirect() {
        return new TypedKeyMap<>(TypedMapDirect::new);
    }

    public static <K, V> TypedKeyMap<K, V> createHierachy() {
        return new TypedKeyMap<>(TypedMapHierarchy::new);
    }

    private TypedKeyMap(Supplier<TypedMap<V>> mapType) {
        this.mapType = mapType;
    }

    public void put(K key, V value) {
        internalMap.computeIfAbsent(key, k -> mapType.get()).put(value);
    }

    @Nullable
    public <T extends V> T get(K key, Class<T> clazz) {
        TypedMap<V> m = internalMap.get(key);
        if (m == null) {
            return null;
        }
        return m.get(clazz);
    }

    public Set<K> getKeys() {
        return internalMap.keySet();
    }

    public TypedMap<V> getAll(K key) {
        return internalMap.get(key);
    }
}
