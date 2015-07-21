/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.stripes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandler;
import buildcraft.core.lib.utils.Utils;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.transport.BuildCraftTransport;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.block.BlockGenericPipe;
import buildcraft.transport.item.ItemPipe;

public class StripesHandlerPipes implements IStripesHandler {

    @Override
    public StripesHandlerType getType() {
        return StripesHandlerType.ITEM_USE;
    }

    @Override
    public boolean shouldHandle(ItemStack stack) {
        return stack.getItem() instanceof ItemPipe;
    }

    @Override
    public boolean handle(World world, BlockPos pos, EnumFacing direction, ItemStack stack, EntityPlayer player, IStripesActivator activator) {

        if (!(stack.getItem() instanceof ItemPipe) || (stack.getItem() == BuildCraftTransport.pipeItemsStripes)) {
            return false;
        }

        Vec3 p = Utils.convert(pos).add(Utils.convert(direction, -1));

        Pipe<?> pipe = BlockGenericPipe.createPipe(stack.getItem());
        if (pipe.transport instanceof PipeTransportItems) {
            // Checks done, request extension
            BuildCraftTransport.pipeExtensionListener.requestPipeExtension(stack, world, Utils.convertFloor(p), direction, activator);
        } else {
            // Fluid/power pipe, place in front instead

            stack.getItem().onItemUse(stack, CoreProxy.proxy.getBuildCraftPlayer((WorldServer) world, Utils.convertFloor(p)).get(), world, Utils
                    .convertFloor(p), EnumFacing.UP, 0, 0, 0);
        }
        return true;
    }
}
