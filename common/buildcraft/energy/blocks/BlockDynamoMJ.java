/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): BlockDynamoMJ deferred
package buildcraft.energy.blocks;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import buildcraft.api.blocks.ICustomRotationHandler;

import buildcraft.lib.block.BlockBCTile_Neptune;

import buildcraft.energy.tile.TileDynamoMJ;

public class BlockDynamoMJ extends BlockBCTile_Neptune implements ICustomRotationHandler {

    public BlockDynamoMJ(AbstractBlock.Settings settings, String id) {
        super(settings, id);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TileDynamoMJ(null, pos, state);
    }

    @Override
    public ActionResult attemptRotation(World world, BlockPos pos, BlockState state, Direction side) {
        return ActionResult.PASS;
    }
}
