/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.stripes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandlerItem;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeApi;

public class StripesHandlerPipeWires implements IStripesHandlerItem {

    private static final int PIPES_TO_TRY = 8;

    @Override
    public boolean handle(World world, BlockPos pos, EnumFacing direction, ItemStack stack, EntityPlayer player, IStripesActivator activator) {
        EnumDyeColor pipeWireColor = EnumDyeColor.byMetadata(stack.getMetadata());

        for (int i = PIPES_TO_TRY; i > 0; i--) {
            pos = pos.offset(direction.getOpposite());

            TileEntity tile = world.getTileEntity(pos);
            if (tile != null && tile.hasCapability(PipeApi.CAP_PIPE_HOLDER, null)) {
                IPipeHolder pipeHolder = tile.getCapability(PipeApi.CAP_PIPE_HOLDER, null);

                /*
                if (!pipeHolder.pipe.wireSet[pipeWireColor]) {
                    pipeHolder.pipe.wireSet[pipeWireColor] = true;
                    pipeHolder.pipe.signalStrength[pipeWireColor] = 0;

                    pipeHolder.pipe.updateSignalState();
                    pipeHolder.scheduleRenderUpdate();
                    world.notifyNeighborsOfStateChange(pipeHolder.getPipePos(), tile.getBlockType(), false);
                    */
                //stack.shrink(1);
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
