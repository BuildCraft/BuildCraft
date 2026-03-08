/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class WorldUtil {
//    public static boolean isWorldCreative(World world) {
//        return world.getWorldInfo().getGameType().isCreative();
//    }

    public static boolean mayPlace(World world, Block blockIn, BlockPos pos, boolean skipCollisionCheck, Direction sidePlacedOn, @Nullable PlayerEntity placer) {
        BlockState iblockstate1 = world.getBlockState(pos);
        VoxelShape axisalignedbb = skipCollisionCheck ? null : blockIn.defaultBlockState().getCollisionShape(world, pos);

//        if (axisalignedbb != Block.NULL_AABB && !this.checkNoEntityCollision(axisalignedbb.offset(pos), placer))
        if (axisalignedbb != VoxelShapes.empty() && !world.getEntities(placer, axisalignedbb.bounds().move(pos)).isEmpty()) {
            return false;
        }
//        else if (iblockstate1.getMaterial() == Material.CIRCUITS && blockIn == Blocks.ANVIL)
        else if (iblockstate1.getMaterial() == Material.DECORATION && blockIn == Blocks.ANVIL) {
            return true;
        } else {
//            return iblockstate1.getBlock().isReplaceable(this, pos) && blockIn.canPlaceBlockOnSide(this, pos, sidePlacedOn);
            return iblockstate1.canBeReplaced(
                    new BlockItemUseContext(
                            world,
                            placer,
                            Hand.MAIN_HAND,
                            StackUtil.EMPTY,
                            BlockRayTraceResult.miss(Vector3d.ZERO, Direction.NORTH, pos)
                    )
            )
                    && blockIn.canSurvive(world.getBlockState(pos), world, pos);
        }
    }
}
