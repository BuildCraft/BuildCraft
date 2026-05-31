/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.transport.net;

import javax.annotation.Nullable;

import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

// STUB(R.Chen): full implementation in Phase 4E.
// The server-side per-player batching relies on Forge's ServerWorld / PlayerChunkMap /
// PlayerChunkMapEntry (replaced by ServerWorld + ServerChunkManager#threadedAnvilChunkStorage in
// Yarn) and on MessageManager, none of which are migrated yet. Calls are accepted and silently
// dropped so the item-flow producer keeps compiling; no packets are sent until Phase 4E.
public class PipeItemMessageQueue {

    public static void serverTick() {
        // STUB(R.Chen): no batched packets are flushed until the Fabric networking layer is wired.
    }

    public static void appendTravellingItem(World world, BlockPos pos, int stackId, byte stackCount, boolean toCenter,
        Direction side, @Nullable DyeColor colour, byte timeToDest) {
        // STUB(R.Chen): no-op until ServerWorld chunk-watcher iteration + MessageManager are migrated.
    }
}
