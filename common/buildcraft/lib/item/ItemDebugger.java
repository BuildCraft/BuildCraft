/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.lib.debug.BCAdvDebugging;
import buildcraft.lib.debug.IAdvDebugTarget;

public class ItemDebugger extends ItemBC_Neptune {
    public ItemDebugger(String id) {
        super(id);
    }

    @Override
    public ActionResult onItemUseFirst(PlayerEntity player, World world, BlockPos pos, Direction side, float hitX, float hitY, float hitZ, Hand hand) {
        if (world.isRemote) {
            return ActionResult.PASS;
        }
        BlockEntity tile = world.getTileEntity(pos);
        if (tile == null) {
            return ActionResult.FAIL;
        }
        if (tile instanceof IAdvDebugTarget) {
            BCAdvDebugging.setCurrentDebugTarget((IAdvDebugTarget) tile);
            return ActionResult.SUCCESS;
        }
        return ActionResult.FAIL;
    }

    public static boolean isShowDebugInfo(PlayerEntity player) {
        return player.capabilities.isCreativeMode ||
            player.getHeldItem(Hand.MAIN_HAND).getItem() instanceof ItemDebugger ||
            player.getHeldItem(Hand.OFF_HAND).getItem() instanceof ItemDebugger;
    }
}
