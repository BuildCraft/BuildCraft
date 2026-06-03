/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.mojang.authlib.GameProfile;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

// STUB(R.Chen): the full Forge BlockUtil (block breaking, BreakEvent firing, fluid drain/fill via the
// Forge fluid API, comparator/double-chest helpers, CompatManager ghost-loading) is deferred. Only the
// block-state / block-entity accessors used by TileBC_Neptune are migrated; the `force` flag — which
// drove CompatManager's chunk-ghost-loading — is now a no-op since Yarn's World loads as needed.
public class BlockUtil {

    public static BlockEntity getTileEntity(World world, BlockPos pos) {
        return getTileEntity(world, pos, false);
    }

    public static BlockEntity getTileEntity(World world, BlockPos pos, boolean force) {
        return world.getBlockEntity(pos);
    }

    public static BlockState getBlockState(World world, BlockPos pos) {
        return getBlockState(world, pos, false);
    }

    public static BlockState getBlockState(World world, BlockPos pos, boolean force) {
        return world.getBlockState(pos);
    }

    /** Pure-logic comparator helper (no Forge deps): orders by the supplied comparator, breaking ties on the
     * (x, y, z) coordinates so that the resulting {@link java.util.TreeSet} never collapses distinct positions. */
    public static Comparator<BlockPos> uniqueBlockPosComparator(Comparator<BlockPos> base) {
        return base
            .thenComparingInt(BlockPos::getX)
            .thenComparingInt(BlockPos::getY)
            .thenComparingInt(BlockPos::getZ);
    }

    // STUB(R.Chen): Forge fluid API (FluidBlock / IFluidBlock / FluidRegistry.lookupFluidForBlock) has no
    // direct Yarn equivalent; the viscosity-aware fluid probing the quarry/pump use is deferred to the
    // Transfer-API fluid migration pass. Returns null (treat as "no fluid") until then.
    public static Fluid getFluidWithFlowing(World world, BlockPos pos) {
        return null;
    }

    // STUB(R.Chen): Forge block-hardness → MJ break-power curve deferred. Returns a flat placeholder cost so
    // the quarry's task scheduler still advances; real per-block hardness scaling restored with the mining pass.
    public static long computeBlockBreakPower(World world, BlockPos pos) {
        return 16 * buildcraft.api.mj.MjAPI.MJ;
    }

    // STUB(R.Chen): Forge breakBlockAndGetDrops (BreakEvent firing, fake-player tool sim, getDrops) deferred.
    // Removes the block server-side and returns an empty drop list so the quarry progresses without dupes.
    public static Optional<List<ItemStack>> breakBlockAndGetDrops(ServerWorld world, BlockPos pos, ItemStack tool,
        GameProfile owner, boolean dropBlock) {
        if (world.isAir(pos)) {
            return Optional.empty();
        }
        world.breakBlock(pos, false);
        return Optional.of(java.util.Collections.emptyList());
    }

    // STUB(R.Chen): missing getFluidWithoutFlowing / getFluid helpers.
    public static Fluid getFluidWithoutFlowing(BlockState state) {
        FluidState fs = state.getFluidState();
        return fs.isEmpty() ? net.minecraft.fluid.Fluids.EMPTY : fs.getFluid().getStill();
    }
    public static Fluid getFluid(World world, BlockPos pos) {
        return getFluidWithFlowing(world, pos);
    }

    /** BlockUtil.getBlockEntity with explicit force flag (no-op, delegates to World). */
    public static BlockEntity getBlockEntity(World world, BlockPos pos, boolean force) {
        return getTileEntity(world, pos, force);
    }

    /** STUB(R.Chen): getFluidWithFlowing(Block) — checks if block is a fluid block. */
    public static Fluid getFluidWithFlowing(net.minecraft.block.Block block) {
        if (block instanceof net.minecraft.block.FluidBlock) {
            return ((net.minecraft.block.FluidBlock) block).getFluidState(block.getDefaultState()).getFluid();
        }
        return null;
    }

    // STUB(R.Chen): direction arrays from Forge BlockUtil — static convenience fields.
    public static final Direction[] HORIZONTALS = Direction.Type.HORIZONTAL.stream().toArray(Direction[]::new);
    public static final int BUCKET_VOLUME = 1000;

    // STUB(R.Chen): Forge useItemOnBlock → Fabric ItemUsageContext migration deferred. Returns false.
    public static boolean useItemOnBlock(World world, net.minecraft.entity.player.PlayerEntity player,
            ItemStack stack, BlockPos pos, Direction side) {
        return false;
    }
}
