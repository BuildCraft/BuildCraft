/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.transport.stripes;

import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandlerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

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
        stack.onItemUseFirst(
//                player,
//                world,
//                pos.offset(direction),
//                EnumHand.MAIN_HAND,
//                direction,
//                0.5f,
//                0.5f,
//                0.5f
                new UseOnContext(
                        world,
                        player,
                        InteractionHand.MAIN_HAND,
                        stack,
                        new BlockHitResult(
                                new Vec3(0, 0, 0),
                                direction,
                                pos.relative(direction),
                                false
                        )
                )
        );
        return true;
    }
}
