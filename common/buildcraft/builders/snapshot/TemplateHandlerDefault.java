/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import buildcraft.api.template.ITemplateHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public enum TemplateHandlerDefault implements ITemplateHandler {
    INSTANCE;

    @Override
    public boolean handle(World world, BlockPos pos, PlayerEntity player, ItemStack stack) {
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
                new ItemUseContext(
                        world,
                        player,
                        player.getUsedItemHand(),
                        player.getItemInHand(player.getUsedItemHand()),
                        new BlockRayTraceResult(
                                new Vector3d(
                                        0.5F,
                                        0.0F,
                                        0.5F
                                ),
                                Direction.UP,
                                pos,
                                false

                        )
                )
        ) == ActionResultType.SUCCESS;
    }
}
