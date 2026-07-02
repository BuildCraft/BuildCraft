/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package ct.buildcraft.transport.stripes;

import ct.buildcraft.api.transport.IStripesActivator;
import ct.buildcraft.api.transport.IStripesHandlerItem;

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
        if (!world.isEmptyBlock(pos.offset(direction.getNormal()))) {
            return false;
        }
        stack.getItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(new Vec3(0.5d,0.5d,0.5d), direction, pos.offset(direction.getNormal()), false))
        );
        return true;
    }
}
