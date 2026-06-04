/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.transport.stripes;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandlerItem;

public enum StripesHandlerHoe implements IStripesHandlerItem {
    INSTANCE;

    @Override
    public boolean handle(World world,
                          BlockPos pos,
                          Direction direction,
                          ItemStack stack,
                          PlayerEntity player,
                          IStripesActivator activator) {

        if (!(stack.getItem() instanceof HoeItem)) {
            return false;
        }

        pos = pos.offset(direction);
        // STUB(R.Chen): stack.onItemUse(...) → item.useOnBlock(ItemUsageContext) in 1.20.1
        // TODO(R.Chen): replace with ItemUsageContext-based call once ItemUsageContext is available
        if (stack.getItem() instanceof HoeItem hoe) {
            // placeholder — actual hoe tillage requires ItemUsageContext
            return false;
        }
        return false;
    }

}
