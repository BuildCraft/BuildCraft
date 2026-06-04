/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.transport.stripes;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandlerItem;


public enum StripesHandlerUse implements IStripesHandlerItem {
    INSTANCE;

    public static final List<Item> ITEMS = new ArrayList<>();

    @Override
    public boolean handle(World world,
                          BlockPos pos,
                          Direction direction,
                          ItemStack stack,
                          PlayerEntity player,
                          IStripesActivator activator) {
        // STUB(R.Chen): BlockUtil.useItemOnBlock removed — needs ItemUsageContext in 1.20.1
        // TODO(R.Chen): replace with item.useOnBlock(new ItemUsageContext(...))
        return false;
    }
}
