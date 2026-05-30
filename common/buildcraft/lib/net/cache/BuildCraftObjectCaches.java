/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.lib.net.cache;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import buildcraft.lib.misc.StackUtil;

// STUB(R.Chen): full implementation in Phase 4E.
// The networked-object-cache layer (NetworkedObjectCache / NetworkedItemStackCache /
// NetworkedFluidStackCache) and the Forge FML lifecycle hooks (Loader / LoaderState / FMLPreInit…)
// are not yet migrated. Only the ItemStack store/retrieve entry points referenced by PipeFlowItems
// are exposed; they currently round-trip the stack locally rather than over the cache.
public class BuildCraftObjectCaches {

    /** STUB(R.Chen): returns a sentinel id; real id allocation is Phase 4E. */
    public static int storeItemStack(@Nonnull ItemStack stack) {
        return 0;
    }

    /** STUB(R.Chen): returns a sentinel id; real id allocation is Phase 4E. */
    public static int getItemStackId(@Nonnull ItemStack stack) {
        return 0;
    }

    /** STUB(R.Chen): returns an empty supplier until the client cache is migrated. */
    public static Supplier<ItemStack> retrieveItemStack(int id) {
        return () -> StackUtil.EMPTY;
    }
}
