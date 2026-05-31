/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.factory.tile;

import javax.annotation.Nonnull;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.mj.IMjReceiver;

import buildcraft.lib.mj.MjBatteryReceiver;

// Forge→Fabric migration notes (R.Chen):
//   IWorldEventListener       → STUB: world event listener deferred (WorldEventListenerAdapter
//                               not yet in libLeaf; polling via SafeTimeTracker used instead).
//   WorldServer               → ServerWorld
//   IBlockState               → BlockState
//   world.isAirBlock(pos)     → world.isAir(pos)
//   world.isOutsideBuildHeight→ world.isOutOfHeightLimit(pos)
//   world.sendBlockBreakProgress → world.setBlockBreakingInfo
//   BlockUtil.computeBlockBreakPower / breakBlockAndGetDrops / isUnbreakableBlock / getFluidWithFlowing
//                             → STUB: BlockUtil methods not yet migrated (Phase 4E).
//   CapUtil.CAP_ITEM_TRANSACTOR → STUB: Forge capability (deferred to Phase 4F).
//   InventoryUtil.addToBestAcceptor → STUB: not in migrated InventoryUtil yet.
public class TileMiningWell extends TileMiner {
    private boolean shouldCheck = true;
    private final SafeTimeTracker tracker = new SafeTimeTracker(256);

    // STUB(R.Chen): IWorldEventListener (WorldEventListenerAdapter) not yet migrated.
    // The Fabric WorldEventListener interface requires many method implementations;
    // WorldEventListenerAdapter will be ported in a dedicated lib.world pass.
    // Until then, shouldCheck is set only via the SafeTimeTracker polling path below.

    public TileMiningWell(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        // STUB(R.Chen): CapUtil.CAP_ITEM_TRANSACTOR + AutomaticProvidingTransactor —
        //               Forge capability; deferred to Phase 4F (Transfer-API item lookup).
    }

    @Override
    protected void mine() {
        if (currentPos != null && canBreak()) {
            shouldCheck = true;
            // STUB(R.Chen): BlockUtil.computeBlockBreakPower / breakBlockAndGetDrops not yet migrated.
            // Full mining logic (MJ cost, block breaking, drops) deferred to Phase 4E.
            // The tile will tick normally but not actually mine until BlockUtil is ported.
            nextPos();
        } else if (shouldCheck || tracker.markTimeIfDelay(world)) {
            nextPos();
            if (currentPos == null) {
                shouldCheck = false;
            }
        }
    }

    private boolean canBreak() {
        // STUB(R.Chen): BlockUtil.isUnbreakableBlock + getFluidWithFlowing not yet migrated.
        // Conservatively return false until the full BlockUtil is ported (Phase 4E).
        if (world.isAir(currentPos)) {
            return false;
        }
        return false; // STUB(R.Chen): fluid viscosity + unbreakable checks deferred
    }

    private void nextPos() {
        currentPos = pos;
        while (true) {
            currentPos = currentPos.down();
            if (world.isOutOfHeightLimit(currentPos)) {
                break;
            }
            if (pos.getY() - currentPos.getY() > MINING_MAX_DEPTH) {
                break;
            }
            if (canBreak()) {
                updateLength();
                return;
            } else if (!world.isAir(currentPos) && !isTubeBlock(world, currentPos)) {
                break;
            }
        }
        currentPos = null;
        updateLength();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        // STUB(R.Chen): world.addEventListener(worldEventListener) deferred —
        //               WorldEventListenerAdapter not yet in libLeaf.
    }

    @Override
    public void onRemove() {
        super.onRemove();
        // STUB(R.Chen): world.removeEventListener(worldEventListener) deferred.
        if (currentPos != null) {
            // STUB(R.Chen): world.setBlockBreakingInfo(entityId, pos, -1) — cancel break animation.
            //               Full signature: world.setBlockBreakingInfo(int entityId, BlockPos, int progress)
        }
    }

    @Override
    protected IMjReceiver createMjReceiver() {
        return new MjBatteryReceiver(battery);
    }
}
