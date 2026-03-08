/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc.collect;

import javax.annotation.Nullable;

/** An indexed map that stores enum values.
 *
 * @param <E> The key of the map. */
public class OrderedEnumMap<E extends Enum<E>> {
    private final byte[] indexes;
    private final E[] order;

    public OrderedEnumMap(Class<E> clazz, E... order) {
        this.order = order;
        E[] values = clazz.getEnumConstants();
        indexes = new byte[values.length];
        int max = order.length;
        for (byte i = 0; i < max; i++) {
            indexes[order[i].ordinal()] = i;
        }
    }

    public int indexOf(@Nullable E val) {
        return indexes[val == null ? 0 : val.ordinal()];
    }

    public E get(int index) {
        return order[index];
    }

    public E[] getOrder() {
        return order;
    }

    public int getOrderLength() {
        return order.length;
    }

    public E next(E val) {
        int index = indexOf(val) + 1;
        return get(index % order.length);
    }

    public E previous(E val) {
        int index = indexOf(val) - 1;
        return get((index + order.length) % order.length);
    }
}
