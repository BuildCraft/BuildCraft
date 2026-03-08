/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.stripes;

import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandlerBlock;
import buildcraft.lib.misc.StackUtil;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.item.minecart.ChestMinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;

public enum StripesHandlerMinecartDestroy implements IStripesHandlerBlock {
    INSTANCE;

    @Override
    public boolean handle(World world, BlockPos pos, Direction direction, PlayerEntity player, IStripesActivator activator) {
        AxisAlignedBB box = new AxisAlignedBB(pos, pos.offset(1, 1, 1));
//        List<EntityMinecart> minecarts = world.getEntitiesWithinAABB(EntityMinecart.class, box);
        List<AbstractMinecartEntity> minecarts = world.getEntitiesOfClass(AbstractMinecartEntity.class, box);

        if (minecarts.size() > 0) {
            Collections.shuffle(minecarts);
            AbstractMinecartEntity cart = minecarts.get(0);
            if (cart instanceof ChestMinecartEntity) {
                // good job, Mojang. :<
//                EntityMinecartContainer container = (EntityMinecartContainer) cart;
                ChestMinecartEntity container = (ChestMinecartEntity) cart;
                for (int i = 0; i < container.getContainerSize(); i++) {
                    ItemStack s = container.getItem(i);
                    if (!s.isEmpty()) {
                        container.setItem(i, StackUtil.EMPTY);
                        // Safety check
                        if (container.getItem(i).isEmpty()) {
                            activator.sendItem(s, direction);
                        }
                    }
                }
            }
            cart.kill();
            activator.sendItem(StackUtil.asNonNull(cart.getCartItem()), direction);
            return true;
        }
        return false;
    }
}
