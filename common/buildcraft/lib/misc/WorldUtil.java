/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class WorldUtil {
//    public static boolean isWorldCreative(World world) {
//        return world.getWorldInfo().getGameType().isCreative();
//    }

    public static boolean mayPlace(Level world, Block blockIn, BlockPos pos, boolean skipCollisionCheck, Direction sidePlacedOn, @Nullable Player placer) {
        BlockState iblockstate1 = world.getBlockState(pos);
        VoxelShape axisalignedbb = skipCollisionCheck ? null : blockIn.defaultBlockState().getCollisionShape(world, pos);

//        if (axisalignedbb != Block.NULL_AABB && !this.checkNoEntityCollision(axisalignedbb.offset(pos), placer))
        if (axisalignedbb != Shapes.empty() && !world.getEntities(placer, axisalignedbb.bounds().move(pos)).isEmpty()) {
            return false;
        }
//        else if (iblockstate1.getMaterial() == Material.CIRCUITS && blockIn == Blocks.ANVIL)
        else if (iblockstate1.getMaterial() == Material.DECORATION && blockIn == Blocks.ANVIL) {
            return true;
        } else {
//            return iblockstate1.getBlock().isReplaceable(this, pos) && blockIn.canPlaceBlockOnSide(this, pos, sidePlacedOn);
            return iblockstate1.canBeReplaced(
                    new BlockPlaceContext(
                            world,
                            placer,
                            InteractionHand.MAIN_HAND,
                            StackUtil.EMPTY,
                            BlockHitResult.miss(Vec3.ZERO, Direction.NORTH, pos)
                    )
            )
                    && blockIn.canSurvive(world.getBlockState(pos), world, pos);
        }
    }
}
