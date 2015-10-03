/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.transport.stripes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandler;
import buildcraft.core.lib.utils.Utils;

public class StripesHandlerPlaceBlock implements IStripesHandler {

    @Override
    public StripesHandlerType getType() {
        return StripesHandlerType.ITEM_USE;
    }

    @Override
    public boolean shouldHandle(ItemStack stack) {
        return stack.getItem() instanceof ItemBlock;
    }

    @Override
    public boolean handle(World world, BlockPos pos, EnumFacing direction, ItemStack stack, EntityPlayer player, IStripesActivator activator) {
        if (!world.isAirBlock(pos) && stack.onItemUse(player, world, pos, EnumFacing.UP, 0.0f, 0.0f, 0.0f)) {
            return true;
        } else if (world.isAirBlock(pos)) {
            Vec3 src = Utils.convert(pos).add(Utils.convert(direction, -1));
            if (stack.onItemUse(player, world, Utils.convertFloor(src), direction, 0.0f, 0.0f, 0.0f)) {
                return true;
            }
        }
        return false;
    }
}
