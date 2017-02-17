/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.transport.stripes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandlerItem;

public enum StripesHandlerPlaceBlock implements IStripesHandlerItem {
    INSTANCE;

    @Override
    public boolean handle(World world, BlockPos pos, EnumFacing direction, ItemStack stack, EntityPlayer player, IStripesActivator activator) {
        if (!(stack.getItem() instanceof ItemBlock)) {
            return false;
        }
        pos = pos.offset(direction);
        if (!world.isAirBlock(pos)) {
            return false;
        }

        ItemBlock ib = (ItemBlock) stack.getItem();
        ib.onItemUse(player, world, pos, EnumHand.MAIN_HAND, direction, 0.5f, 0.5f, 0.5f);

        return true;
    }
}
