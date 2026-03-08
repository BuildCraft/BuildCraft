/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.transport.stripes;

import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandlerItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public enum StripesHandlerHoe implements IStripesHandlerItem {
    INSTANCE;

    @Override
    public boolean handle(World world,
                          BlockPos pos,
                          Direction direction,
                          ItemStack stack,
                          PlayerEntity player,
                          IStripesActivator activator) {

        if (!(stack.getItem() instanceof HoeItem)) {
            return false;
        }

        pos = pos.relative(direction);
//        if (stack.onItemUse(
        if (stack.onItemUseFirst(
//                player,
//                world,
//                pos,
//                EnumHand.MAIN_HAND,
//                EnumFacing.UP,
//                0.0f,
//                0.0f,
//                0.0f
                new ItemUseContext(
                        world,
                        player,
                        Hand.MAIN_HAND,
                        stack,
                        new BlockRayTraceResult(
                                new Vector3d(0, 0, 0),
                                Direction.UP,
                                pos,
                                false
                        )
                )
        ) != ActionResultType.PASS)
        {
            return true;
        }

//        if (direction != Direction.UP && stack.onItemUse(
        if (direction != Direction.UP && stack.onItemUseFirst(
//                player,
//                world,
//                pos.down(),
//                EnumHand.MAIN_HAND,
//                EnumFacing.UP,
//                0.0f,
//                0.0f,
//                0.0f
                new ItemUseContext(
                        world,
                        player,
                        Hand.MAIN_HAND,
                        stack,
                        new BlockRayTraceResult(
                                new Vector3d(0, 0, 0),
                                Direction.UP,
                                pos.below(),
                                false
                        )
                )
        ) != ActionResultType.PASS)
        {
            return true;
        }

        return false;
    }
}
