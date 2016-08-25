package buildcraft.lib;

import java.util.HashMap;

public class CachedMap<K, V> extends HashMap<K, V> {
    private HashMap<K, Long> timesMap = new HashMap<>();
    private final int timeout;

    public CachedMap(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public V put(K key, V value) {
        timesMap.put(key, System.currentTimeMillis() + timeout);
        return super.put(key, value);
    }

    @Override
    public boolean containsKey(Object key) {
        if(timesMap.containsKey(key) && timesMap.get(key) < System.currentTimeMillis()) {
            remove(key);
            timesMap.remove(key);
            return false;
        }
        return super.containsKey(key);
    }

    @Override
    public V get(Object key) {
        containsKey(key);
        return super.get(key);
    }
}
