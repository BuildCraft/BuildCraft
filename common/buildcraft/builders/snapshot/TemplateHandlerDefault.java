/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import buildcraft.api.template.ITemplateHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public enum TemplateHandlerDefault implements ITemplateHandler {
    INSTANCE;

    @Override
    public boolean handle(World world, BlockPos pos, EntityPlayer player, ItemStack stack) {
        return stack.onItemUse(
            player,
            world,
            pos,
            player.getActiveHand(),
            EnumFacing.UP,
            0.5F,
            0.0F,
            0.5F
        ) == EnumActionResult.SUCCESS;
    }
}
