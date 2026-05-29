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

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandlerItem;

public enum StripesHandlerEntityInteract implements IStripesHandlerItem {
    INSTANCE;

    @Override
    public boolean handle(World world,
                          BlockPos pos,
                          Direction direction,
                          ItemStack stack,
                          PlayerEntity player,
                          IStripesActivator activator) {
        BlockPos target = pos.offset(direction);
        Box box = new Box(target.getX(), target.getY(), target.getZ(), target.getX() + 1, target.getY() + 1, target.getZ() + 1);
        // TODO(R.Chen): getEntitiesWithinAABB → getEntitiesByClass
        List<LivingEntity> entities = world.getEntitiesByClass(LivingEntity.class, box, e -> true);
        Collections.shuffle(entities);
        for (LivingEntity entity : entities) {
            if (player.interact(entity, Hand.MAIN_HAND).isAccepted()) {
                return true;
            }
        }
        return false;
    }
}
