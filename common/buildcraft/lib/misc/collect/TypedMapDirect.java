/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc.collect;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

/** A {@link TypedMap} instance that only maps classes directly to the classes - that is {@link #get(Class)} will return
 * either null, or an object whose {@link #getClass()} equals the argument class. */
public class TypedMapDirect<V> implements TypedMap<V> {

    private final Map<Class<?>, V> internalMap = new HashMap<>();

    @Override
    @Nullable
    public <T extends V> T get(Class<T> clazz) {
        T val = clazz.cast(internalMap.get(clazz));
        if (val != null) {
            return val;
        }
        return null;
    }

    @Override
    public void put(V value) {
        internalMap.put(value.getClass(), value);
    }

    @Override
    public void clear() {
        internalMap.clear();
    }

    @Override
    public void remove(V value) {
        internalMap.remove(value.getClass(), value);
    }
}
