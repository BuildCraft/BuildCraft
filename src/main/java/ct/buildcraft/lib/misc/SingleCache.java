/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.misc;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;

public class SingleCache<T> implements Supplier<T> {
    private final Supplier<T> delegate;
    private final long duration;
    private final TimeUnit timeUnit;
    private Supplier<T> cache;

    public SingleCache(Supplier<T> delegate, long duration, TimeUnit timeUnit) {
        this.delegate = delegate;
        this.duration = duration;
        this.timeUnit = timeUnit;
        clear();
    }

    public void clear() {
        cache = Suppliers.memoizeWithExpiration(delegate::get, duration, timeUnit)::get;
    }

    @Override
    public T get() {
        return cache.get();
    }
}
