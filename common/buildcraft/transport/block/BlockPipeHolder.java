/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.transport.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.block.AbstractBlock;

import buildcraft.lib.block.BlockBCBase_Neptune;

import buildcraft.transport.tile.TilePipeHolder;

// STUB(R.Chen): full VoxelShape + render-attachment Phase 4F. This is the 1,115-LOC finale of the transport
// block migration (pipe collision/raytrace VoxelShapes, pluggable hit-boxes, BlockEntityProvider wiring,
// the BlockApiLookup registration of pipe capabilities, item-drop + activation handling). For now this is a
// minimal BlockBCBase_Neptune subclass exposing only what TilePipeHolder references: a Block handle
// (BCTransportBlocks.pipeHolder) and the client-side landing-particle hook.
public class BlockPipeHolder extends BlockBCBase_Neptune {

    public BlockPipeHolder(AbstractBlock.Settings settings, String id) {
        super(settings, id);
    }

    /** Called from {@link TilePipeHolder#readPayload} when a NET_CREATE_LANDING_PARTICLE message arrives. */
    @Environment(EnvType.CLIENT)
    public static void spawnLandingParticles(TilePipeHolder tile, double x, double y, double z, int number) {
        // STUB(R.Chen): pipe-flow landing particle spawn restored with the client render layer in Phase 4F.
    }
}
