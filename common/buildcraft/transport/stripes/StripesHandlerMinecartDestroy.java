/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.stripes;

import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandlerBlock;
import buildcraft.lib.misc.StackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.Collections;
import java.util.List;

public enum StripesHandlerMinecartDestroy implements IStripesHandlerBlock {
    INSTANCE;

    @Override
    public boolean handle(Level world, BlockPos pos, Direction direction, Player player, IStripesActivator activator) {
        AABB box = new AABB(pos, pos.offset(1, 1, 1));
//        List<EntityMinecart> minecarts = world.getEntitiesWithinAABB(EntityMinecart.class, box);
        List<AbstractMinecart> minecarts = world.getEntitiesOfClass(AbstractMinecart.class, box);

        if (minecarts.size() > 0) {
            Collections.shuffle(minecarts);
            AbstractMinecart cart = minecarts.get(0);
            if (cart instanceof MinecartChest) {
                // good job, Mojang. :<
//                EntityMinecartContainer container = (EntityMinecartContainer) cart;
                MinecartChest container = (MinecartChest) cart;
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
//            activator.sendItem(StackUtil.asNonNull(cart.getCartItem()), direction);
            activator.sendItem(StackUtil.asNonNull(cart.getPickResult()), direction);
            return true;
        }
        return false;
    }
}
