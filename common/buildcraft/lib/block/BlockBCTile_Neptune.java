/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.block;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

import buildcraft.lib.tile.TileBC_Neptune;

public abstract class BlockBCTile_Neptune extends BlockBCBase_Neptune implements BlockEntityProvider {
    public BlockBCTile_Neptune(AbstractBlock.Settings material, String id) {
        super(material, id);
    }

    // ── Forge-compat hooks (no @Override — these are new methods, called by Fabric bridges below) ────

    /** Forge-compat: subclasses override this. Called by {@link #createBlockEntity(BlockPos, BlockState)}. */
    @Nullable
    public TileBC_Neptune createTileEntity(World world, BlockState state) { return null; }

    /** Forge-compat: no-op in 1.20.1 (BlockEntityProvider is the replacement). */
    public boolean hasTileEntity(BlockState state) { return true; }

    /** Forge-compat: called on explosion. Delegates to Fabric's onDestroyedByExplosion. */
    public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileBC_Neptune) {
            ((TileBC_Neptune) tile).onExplode(explosion);
        }
    }

    /** Forge-compat: called on break. Delegates to Fabric's onStateReplaced. */
    public void breakBlock(World world, BlockPos pos, BlockState state) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileBC_Neptune) {
            ((TileBC_Neptune) tile).onRemove();
        }
    }

    /** Forge-compat: called on placement. Delegates to Fabric's onPlaced. */
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileBC_Neptune) {
            ((TileBC_Neptune) tile).onPlacedBy(placer, stack);
        }
    }

    /** Forge-compat: called on right-click. Returns true if activated. */
    public boolean onBlockActivated(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand,
            net.minecraft.util.math.Direction facing, float hitX, float hitY, float hitZ) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileBC_Neptune) {
            return ((TileBC_Neptune) tile).onActivated(player, hand, facing, hitX, hitY, hitZ);
        }
        return false;
    }

    // ── Fabric 1.20.1 API bridges ────────────────────────────────────────────

    @Override
    @Nullable
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return createTileEntity(null, state);
    }

    @Override
    public void onDestroyedByExplosion(World world, BlockPos pos, Explosion explosion) {
        onBlockExploded(world, pos, explosion);
        super.onDestroyedByExplosion(world, pos, explosion);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            breakBlock(world, pos, state);
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        onBlockPlacedBy(world, pos, state, placer, stack);
        super.onPlaced(world, pos, state, placer, stack);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player,
            Hand hand, BlockHitResult hit) {
        if (onBlockActivated(world, pos, state, player, hand, hit.getSide(),
                (float) hit.getPos().x - pos.getX(),
                (float) hit.getPos().y - pos.getY(),
                (float) hit.getPos().z - pos.getZ())) {
            return ActionResult.SUCCESS;
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos,
            boolean notify) {
        super.neighborChanged(state, world, pos, block, fromPos, notify);
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileBC_Neptune) {
            ((TileBC_Neptune) tile).onNeighbourBlockChanged(block, fromPos);
        }
    }
}
