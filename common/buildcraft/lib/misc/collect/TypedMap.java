/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc.collect;

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
