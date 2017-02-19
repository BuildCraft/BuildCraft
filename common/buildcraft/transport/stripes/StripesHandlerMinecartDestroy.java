/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.stripes;

import java.util.Collections;
import java.util.List;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityMinecartContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandlerBlock;

import buildcraft.lib.misc.StackUtil;

public enum StripesHandlerMinecartDestroy implements IStripesHandlerBlock {
    INSTANCE;

    @Override
    public boolean handle(World world, BlockPos pos, EnumFacing direction, EntityPlayer player, IStripesActivator activator) {
        AxisAlignedBB box = new AxisAlignedBB(pos, pos.add(1, 1, 1));
        List<EntityMinecart> minecarts = world.getEntitiesWithinAABB(EntityMinecart.class, box);

        if (minecarts.size() > 0) {
            Collections.shuffle(minecarts);
            EntityMinecart cart = minecarts.get(0);
            if (cart instanceof EntityMinecartContainer) {
                // good job, Mojang. :<
                EntityMinecartContainer container = (EntityMinecartContainer) cart;
                for (int i = 0; i < container.getSizeInventory(); i++) {
                    ItemStack s = container.getStackInSlot(i);
                    if (!s.isEmpty()) {
                        container.setInventorySlotContents(i, StackUtil.EMPTY);
                        // Safety check
                        if (container.getStackInSlot(i).isEmpty()) {
                            activator.sendItem(s, direction.getOpposite());
                        }
                    }
                }
            }
            cart.setDead();
            activator.sendItem(StackUtil.asNonNull(cart.getCartItem()), direction.getOpposite());
            return true;
        }
        return false;
    }
}
