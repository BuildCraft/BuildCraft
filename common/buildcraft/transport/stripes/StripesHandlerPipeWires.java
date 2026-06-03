/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.transport.stripes;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandlerItem;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeApi;

public class StripesHandlerPipeWires implements IStripesHandlerItem {

    private static final int PIPES_TO_TRY = 8;

    @Override
    public boolean handle(World world, BlockPos pos, Direction direction, ItemStack stack, PlayerEntity player, IStripesActivator activator) {
        // TODO(R.Chen): DyeColor.byMetadata → DyeColor.byId
        DyeColor pipeWireColor = DyeColor.byId(stack.getDamage());

        for (int i = PIPES_TO_TRY; i > 0; i--) {
            pos = pos.offset(direction.getOpposite());

            BlockEntity tile = world.getBlockEntity(pos);
            // STUB(R.Chen): Forge capabilities → instanceof check
            if (tile instanceof IPipeHolder) {
                IPipeHolder pipeHolder = (IPipeHolder) tile;

                /*
                if (!pipeHolder.pipe.wireSet[pipeWireColor]) {
                    pipeHolder.pipe.wireSet[pipeWireColor] = true;
                    pipeHolder.pipe.signalStrength[pipeWireColor] = 0;

                    pipeHolder.pipe.updateSignalState();
                    pipeHolder.scheduleRenderUpdate();
                    world.notifyNeighborsOfStateChange(pipeHolder.getPipePos(), tile.getBlockType(), false);
                    */
                //stack.decrement(1);
                    /*
                    return true;

            }
            */

            } else {
                // Not a pipe, don't follow chain
                return false;
            }
        }

        return false;
    }
}
