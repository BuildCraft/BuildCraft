/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.stripes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.crops.CropManager;
import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandlerItem;

public enum StripesHandlerPlant implements IStripesHandlerItem {
    INSTANCE;

    @Override
    public boolean handle(World world,
                          BlockPos pos,
                          EnumFacing direction,
                          ItemStack stack,
                          EntityPlayer player,
                          IStripesActivator activator) {
        return CropManager.plantCrop(world, player, stack, pos.offset(direction).down())
            || CropManager.plantCrop(world, player, stack, pos.offset(direction));
    }
}
