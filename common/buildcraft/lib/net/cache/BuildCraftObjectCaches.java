/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.lib.net.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;

import net.minecraft.item.ItemStack;

import buildcraft.lib.misc.StackUtil;

// STUB(R.Chen): full implementation in Phase 4E.
public class BuildCraftObjectCaches {

    /** Real NetworkedFluidStackCache — provides typed server()/client() API Tank.java expects. */
    public static final NetworkedFluidStackCache CACHE_FLUIDS = new NetworkedFluidStackCache();
    /** STUB(R.Chen): all caches list — stub for FML lifecycle calls. */
    public static final List<Object> CACHES = new ArrayList<>();

    /** STUB(R.Chen): returns a sentinel id; real id allocation is Phase 4E. */
    public static int storeItemStack(@Nonnull ItemStack stack) { return 0; }

    /** STUB(R.Chen): returns a sentinel id; real id allocation is Phase 4E. */
    public static int getItemStackId(@Nonnull ItemStack stack) { return 0; }

    /** STUB(R.Chen): returns an empty supplier until the client cache is migrated. */
    public static Supplier<ItemStack> retrieveItemStack(int id) { return () -> StackUtil.EMPTY; }

    /** STUB(R.Chen): returns a sentinel id; real id allocation is Phase 4E. */
    public static int storeFluid(@Nonnull FluidVariant fluid) { return 0; }

    /** STUB(R.Chen): returns a sentinel id; real id allocation is Phase 4E. */
    public static int getFluidId(@Nonnull FluidVariant fluid) { return 0; }

    /** STUB(R.Chen): returns a blank supplier until the client cache is migrated. */
    public static Supplier<FluidVariant> retrieveFluid(int id) { return FluidVariant::blank; }
}
