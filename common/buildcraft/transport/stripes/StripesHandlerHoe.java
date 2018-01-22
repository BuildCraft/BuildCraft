/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.transport.stripes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandlerItem;

public enum StripesHandlerHoe implements IStripesHandlerItem {
    INSTANCE;

    @Override
    public boolean handle(World world,
                          BlockPos pos,
                          EnumFacing direction,
                          ItemStack stack,
                          EntityPlayer player,
                          IStripesActivator activator) {

        if (!(stack.getItem() instanceof ItemHoe)) {
            return false;
        }

        pos = pos.offset(direction);
        return stack.onItemUse(player, world, pos, EnumHand.MAIN_HAND, EnumFacing.UP, 0.0f, 0.0f, 0.0f) != EnumActionResult.PASS
                || direction != EnumFacing.UP && stack.onItemUse(player, world, pos.down(), EnumHand.MAIN_HAND, EnumFacing.UP, 0.0f, 0.0f, 0.0f) != EnumActionResult.PASS;

    }

}
