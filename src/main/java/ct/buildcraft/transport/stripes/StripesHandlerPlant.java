/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport.stripes;

import ct.buildcraft.api.crops.CropManager;
import ct.buildcraft.api.transport.IStripesActivator;
import ct.buildcraft.api.transport.IStripesHandlerItem;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public enum StripesHandlerPlant implements IStripesHandlerItem {
    INSTANCE;

    @Override
    public boolean handle(Level world,
                          BlockPos pos,
                          Direction direction,
                          ItemStack stack,
                          Player player,
                          IStripesActivator activator) {
        return CropManager.plantCrop(world, player, stack, pos.offset(direction.getNormal()).below())
            || CropManager.plantCrop(world, player, stack, pos.offset(direction.getNormal()));
    }
}
