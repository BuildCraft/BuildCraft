/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc.data;

import buildcraft.api.core.IConvertable;

import javax.annotation.Nullable;

/** Defines a simple reference to an object, that can be retrieved or changed at any time. */
public interface IReference<T> {
    T get();

    void set(T to);

    boolean canSet(@Nullable T value);

    Class<T> getHeldType();

    default void setIfCan(Object value) {

        T obj = convertToType(value);

        if (obj == null && value != null) {
            return;
        }

        if (canSet(obj)) {
            set(obj);
        }
    }

    default T convertToType(Object value) {
        if (getHeldType().isInstance(value)) {
            return getHeldType().cast(value);
        }
        if (value instanceof IConvertable) {
            return ((IConvertable) value).convertTo(getHeldType());
        }
        return null;
    }
}
