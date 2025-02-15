/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.prop;

// 1.18.2: use ModelProperty
@Deprecated(forRemoval = true)
//public class UnlistedNonNullProperty<V> implements IUnlistedProperty<V>
public class UnlistedNonNullProperty<V> {
    public final String name;

    public UnlistedNonNullProperty(String name) {
        this.name = name;
    }

//    @Override
//    public String getName() {
//        return name;
//    }
//
//    @Override
//    public boolean isValid(V value) {
//        return value != null;
//    }
//
//    @SuppressWarnings("unchecked")
//    @Override
//    public Class getType() {
//        return Object.class;
//    }
//
//    @Override
//    public String valueToString(V value) {
//        return value.toString();
//    }
}
