/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import buildcraft.api.template.ITemplateHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public enum TemplateHandlerDefault implements ITemplateHandler {
    INSTANCE;

    @Override
    public boolean handle(Level world, BlockPos pos, Player player, ItemStack stack) {
//        return stack.onItemUse(
//            player,
//            world,
//            pos,
//            player.getActiveHand(),
//            Direction.UP,
//            0.5F,
//            0.0F,
//            0.5F
//        ) == EnumActionResult.SUCCESS;
        return stack.useOn(
                new UseOnContext(
                        world,
                        player,
                        player.getUsedItemHand(),
                        player.getItemInHand(player.getUsedItemHand()),
                        new BlockHitResult(
                                new Vec3(
                                        0.5F,
                                        0.0F,
                                        0.5F
                                ),
                                Direction.UP,
                                pos,
                                false

                        )
                )
        ) == InteractionResult.SUCCESS;
    }
}
