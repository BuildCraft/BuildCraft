/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.stripes;

import java.util.Collections;
import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandlerItem;

public enum StripesHandlerEntityInteract implements IStripesHandlerItem {
    INSTANCE;

    @Override
    public boolean handle(World world,
                          BlockPos pos,
                          EnumFacing direction,
                          ItemStack stack,
                          EntityPlayer player,
                          IStripesActivator activator) {
        List<EntityLivingBase> entities = world.getEntitiesWithinAABB(
            EntityLivingBase.class,
            new AxisAlignedBB(pos.offset(direction))
        );
        Collections.shuffle(entities);
        for (EntityLivingBase entity : entities) {
            if (player.interactOn(entity, EnumHand.MAIN_HAND) == EnumActionResult.SUCCESS) {
                return true;
            }
        }
        return false;
    }
}
