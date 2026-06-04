/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.transport.stripes;

import java.util.Collections;
import java.util.List;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.StorageMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandlerBlock;

import buildcraft.lib.misc.StackUtil;

public enum StripesHandlerMinecartDestroy implements IStripesHandlerBlock {
    INSTANCE;

    @Override
    public boolean handle(World world, BlockPos pos, Direction direction, PlayerEntity player, IStripesActivator activator) {
        Box box = new Box(pos, pos.add(1, 1, 1));
        List<AbstractMinecartEntity> minecarts = world.getEntitiesByClass(AbstractMinecartEntity.class, box, e -> true);

        if (minecarts.size() > 0) {
            Collections.shuffle(minecarts);
            AbstractMinecartEntity cart = minecarts.get(0);
            if (cart instanceof StorageMinecartEntity) {
                // good job, Mojang. :<
                StorageMinecartEntity container = (StorageMinecartEntity) cart;
                for (int i = 0; i < container.size(); i++) {
                    ItemStack s = container.getStack(i);
                    if (!s.isEmpty()) {
                        container.setStack(i, StackUtil.EMPTY);
                        // Safety check
                        if (container.getStack(i).isEmpty()) {
                            activator.sendItem(s, direction);
                        }
                    }
                }
            }
            cart.kill();
            activator.sendItem(StackUtil.asNonNull(cart.getPickBlockStack()), direction);
            return true;
        }
        return false;
    }
}
