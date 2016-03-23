package buildcraft.robotics.path;

import java.util.HashMap;

public class HashMapWithDefault<A, B> extends HashMap<A, B> {
    private static final long serialVersionUID = 2176354621758941734L;
    private final B defaultVal;

    public HashMapWithDefault(B defaultVal) {
        this.defaultVal = defaultVal;
    }

    @Override
    public B get(Object key) {
        if (super.containsKey(key))
            return super.get(key);
        return defaultVal;
    }
}