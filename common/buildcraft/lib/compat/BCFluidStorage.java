/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.compat;

import javax.annotation.Nullable;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

/**
 * Fabric Transfer API bridge replacing Forge's {@code IFluidHandler} capability.
 *
 * Provides a single-tank {@link Storage}{@code <FluidVariant>} implementation
 * backed by Fabric's {@link SingleFluidStorage} primitive. Fluid amounts are
 * tracked in <b>droplets</b> ({@code 1 bucket = 81000 droplets =}
 * {@link FluidConstants#BUCKET}).
 *
 * <h2>Why droplets, not mB?</h2>
 * Forge used millibuckets (1 bucket = 1000 mB). Fabric uses droplets (1 bucket =
 * 81000 droplets) so that 1 ingot = exactly 1/9 of a bucket without rounding.
 * BuildCraft tanks were historically multiples of 1000 mB; the conversion is
 * {@code dropletAmount = mBAmount * 81L}.
 *
 * TODO(R.Chen): expose a builder that takes capacity in mB for source-compat
 *               with the legacy {@code Tank} constructor signatures.
 * TODO(R.Chen): add a multi-tank variant when porting {@code TankManager}.
 */
public final class BCFluidStorage {

    /** {@code FluidConstants.BUCKET} re-exported so legacy code reads naturally. */
    public static final long BUCKET = FluidConstants.BUCKET;

    private BCFluidStorage() {}

    /**
     * Build a single-tank fluid storage with the given capacity in droplets.
     * Subclass to override {@link SingleFluidStorage#onFinalCommit()} for sync.
     *
     * <pre>
     *     SingleFluidStorage tank = BCFluidStorage.singleTank(16 * BUCKET);
     * </pre>
     */
    public static SingleFluidStorage singleTank(long capacityInDroplets) {
        return SingleFluidStorage.withFixedCapacity(capacityInDroplets, () -> {});
    }

    /**
     * Side-aware fluid storage lookup. Mirrors the legacy
     * {@code te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side)}
     * call shape.
     *
     * @param world the world the block entity lives in (must be non-null at call time)
     * @param be    the block entity (may be null — returns {@code null})
     * @param side  the face being queried (use {@code null} for unsided access)
     * @return a {@link Storage} view, or {@code null} if nothing is registered
     */
    @Nullable
    public static Storage<FluidVariant> fromBlockEntity(World world, @Nullable BlockEntity be, @Nullable Direction side) {
        if (be == null) return null;
        return FluidStorage.SIDED.find(world, be.getPos(), be.getCachedState(), be, side);
    }

    /** Convert a legacy mB amount into droplets. */
    public static long mBToDroplets(long mB) {
        return mB * (BUCKET / 1000L);
    }
}
