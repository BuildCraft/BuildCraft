/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

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
                    if (s != null) {
                        container.setInventorySlotContents(i, null);
                        // Safety check
                        if (container.getStackInSlot(i) == null) {
                            activator.sendItem(s, direction);
                        }
                    }
                }
            }
            cart.setDead();
            activator.sendItem(cart.getCartItem(), direction);
            return true;
        }
        return false;
    }
}
