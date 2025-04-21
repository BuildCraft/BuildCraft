/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.transport.stripes;

import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandlerItem;
import buildcraft.lib.misc.VecUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public enum StripesHandlerPlaceBlock implements IStripesHandlerItem {
    INSTANCE;

    @Override
    public boolean handle(Level world,
            BlockPos pos,
            Direction direction,
            ItemStack stack,
            Player player,
            IStripesActivator activator) {
        if (!(stack.getItem() instanceof BlockItem)) {
            return false;
        }
        if (!world.isEmptyBlock(pos.relative(direction))) {
            return false;
        }
//        stack.getItem().onItemUse(
//                player,
//                world,
//                pos.offset(direction),
//                EnumHand.MAIN_HAND,
//                direction,
//                0.5f,
//                0.5f,
//                0.5f
//        );
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        InteractionResult result = ((ServerPlayer) player).gameMode.useItemOn(
                (ServerPlayer) player,
                world,
                stack,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(
                        VecUtil.convertCenter(pos),
                        direction,
                        pos.relative(direction),
                        false
                )
        );
        // return true;
        return result.consumesAction();
    }
}
