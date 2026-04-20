/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ItemDebugger extends ItemBC_Neptune {
    public ItemDebugger(String id, Item.Properties p) {
        super(id, p );
    }

    @Override
	public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
    	Level level = context.getLevel();
    	if (level.isClientSide()) {
            return InteractionResult.PASS;
        }
        BlockEntity tile = level.getBlockEntity(context.getClickedPos());
        if (tile == null) {
            return InteractionResult.FAIL;
        }
/*        if (tile instanceof IAdvDebugTarget) {
            BCAdvDebugging.setCurrentDebugTarget((IAdvDebugTarget) tile);
            return InteractionResult.SUCCESS;
        }*/
        return InteractionResult.FAIL;
	}


    public static boolean isShowDebugInfo(Player player) {
        return player.isCreative() ||
            player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof ItemDebugger ||
            player.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof ItemDebugger;
    }
}
