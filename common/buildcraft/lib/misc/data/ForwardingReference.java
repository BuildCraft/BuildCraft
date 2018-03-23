/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc.data;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ForwardingReference<T> implements IReference<T> {

    public final Supplier<T> getter;
    public final Consumer<T> setter;
    public final Class<T> clazz;

    public ForwardingReference(Class<T> clazz, Supplier<T> getter, Consumer<T> setter) {
        this.getter = getter;
        this.setter = setter;
        this.clazz = clazz;
    }

    @Override
    public T get() {
        return getter.get();
    }

    @Override
    public void set(T to) {
        setter.accept(to);
    }

    @Override
    public boolean canSet(T value) {
        return true;
    }

    @Override
    public Class<T> getHeldType() {
        return clazz;
    }
}
