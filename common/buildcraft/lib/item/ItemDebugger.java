/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.item;

import buildcraft.lib.debug.BCAdvDebugging;
import buildcraft.lib.debug.IAdvDebugTarget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemDebugger extends ItemBC_Neptune {
    public ItemDebugger(String idBC, Item.Properties properties) {
        super(idBC, properties);
    }

    @Override
//    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand)
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext ctx) {
        World world = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();

        if (world.isClientSide()) {
            return ActionResultType.PASS;
        }
        TileEntity tile = world.getBlockEntity(pos);
        if (tile == null) {
            return ActionResultType.FAIL;
        }
        if (tile instanceof IAdvDebugTarget) {
            BCAdvDebugging.setCurrentDebugTarget((IAdvDebugTarget) tile);
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.FAIL;
    }

    public static boolean isShowDebugInfo(PlayerEntity player) {
        return player.abilities.instabuild ||
                player.getItemInHand(Hand.MAIN_HAND).getItem() instanceof ItemDebugger ||
                player.getItemInHand(Hand.OFF_HAND).getItem() instanceof ItemDebugger;
    }
}
