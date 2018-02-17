/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.item;

import buildcraft.lib.debug.BCAdvDebugging;
import buildcraft.lib.debug.IAdvDebugTarget;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemDebugger extends ItemBC_Neptune {
    public ItemDebugger(String id) {
        super(id);
    }

    @Override
    public EnumActionResult onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        if (world.isRemote) {
            return EnumActionResult.PASS;
        }
        TileEntity tile = world.getTileEntity(pos);
        if (tile == null) {
            return EnumActionResult.FAIL;
        }
        if (tile instanceof IAdvDebugTarget) {
            BCAdvDebugging.setCurrentDebugTarget((IAdvDebugTarget) tile);
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.FAIL;
    }

    public static boolean isShowDebugInfo(EntityPlayer player) {
        return player.capabilities.isCreativeMode ||
                player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemDebugger ||
                player.getHeldItem(EnumHand.OFF_HAND).getItem() instanceof ItemDebugger;
    }

}
