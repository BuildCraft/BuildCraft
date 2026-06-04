/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.lib.inventory;

import javax.annotation.Nonnull;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.Direction;

import buildcraft.api.inventory.IItemTransactor;
import buildcraft.api.transport.IInjectable;

// STUB(R.Chen): full implementation in Phase 4E.
// The Forge ICapabilityProvider / IItemHandler / IInventory / ISidedInventory wrappers and the
// entity transactors (ItemEntity / EntityArrow) all depend on the Transfer-API item layer and the
// inventory wrapper classes which are not yet migrated. Only the three entry points referenced by
// PipeFlowItems are exposed here, returning the no-space fallbacks so callers behave as "nothing
// connected" until the real routing is wired up.
public class ItemTransactorHelper {

    /** STUB(R.Chen): always returns the no-space transactor until BlockApiLookup wiring lands. */
    @Nonnull
    public static IItemTransactor getTransactor(BlockEntity provider, Direction face) {
        return NoSpaceTransactor.INSTANCE;
    }

    /** STUB(R.Chen): always returns the no-space injectable until BlockApiLookup wiring lands. */
    @Nonnull
    public static IInjectable getInjectable(BlockEntity provider, Direction face) {
        return NoSpaceInjectable.INSTANCE;
    }

    public static IItemTransactor wrapInjectable(IInjectable injectable, Direction facing) {
        return new InjectableWrapper(injectable, facing);
    }

    /** STUB(R.Chen): an {@link IInjectable} that never accepts anything. */
    private enum NoSpaceInjectable implements IInjectable {
        INSTANCE;

        @Override
        public boolean canInjectItems(Direction from) {
            return false;
        }

        @Nonnull
        @Override
        public ItemStack injectItem(@Nonnull ItemStack stack, boolean doAdd, Direction from, DyeColor color,
            double speed) {
            return stack;
        }
    }

    /** STUB(R.Chen): wraps an {@link IInjectable} as an insert-only {@link IItemTransactor}. */
    private static class InjectableWrapper implements IItemTransactor.IItemInsertable {
        private final IInjectable injectable;
        private final Direction facing;

        InjectableWrapper(IInjectable injectable, Direction facing) {
            this.injectable = injectable;
            this.facing = facing;
        }

        @Nonnull
        @Override
        public ItemStack insert(@Nonnull ItemStack stack, boolean allOrNone, boolean simulate) {
            return injectable.injectItem(stack, !simulate, facing, null, 0);
        }
    }
}
