/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): fastutil custom hash strategy compile issue deferred
package buildcraft.lib.net.cache;

import net.minecraft.item.ItemStack;

import buildcraft.lib.misc.StackUtil;

public class NetworkedItemStackCache extends NetworkedObjectCache<ItemStack> {

    public NetworkedItemStackCache() {
        super(StackUtil.EMPTY);
    }
}
