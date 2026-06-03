/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
// STUB(R.Chen): BlockMarkerBase deferred — marker/volume connection logic pending
package buildcraft.lib.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import buildcraft.api.blocks.ICustomRotationHandler;

public abstract class BlockMarkerBase extends BlockBCTile_Neptune implements ICustomRotationHandler {

    public BlockMarkerBase(AbstractBlock.Settings settings, String id) {
        super(settings, id);
    }

    @Override
    public ActionResult attemptRotation(World world, BlockPos pos, BlockState state, Direction sideWrenched) {
        return ActionResult.PASS;
    }
}
