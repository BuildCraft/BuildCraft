/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.template.ITemplateHandler;

public enum TemplateHandlerDefault implements ITemplateHandler {
    INSTANCE;

    @Override
    public boolean handle(World world, BlockPos pos, PlayerEntity player, ItemStack stack) {
        return stack.onItemUse(
            player,
            world,
            pos,
            player.getActiveHand(),
            Direction.UP,
            0.5F,
            0.0F,
            0.5F
        ) == ActionResult.SUCCESS;
    }
}
