/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.item;

import buildcraft.api.blocks.BlockConstants;
import buildcraft.factory.BCFactoryBlocks;
import buildcraft.factory.block.BlockWaterGel;
import buildcraft.lib.item.ItemBC_Neptune;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.Random;

public class ItemWaterGel extends ItemBC_Neptune {
    // Calen: from 1.12.2 not present in 1.18.2
    protected static Random itemRand = new Random();

    public ItemWaterGel(String idBC, Item.Properties properties) {
        super(idBC, properties);
//        this.maxStackSize = 16;
    }

    @Override
//    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand)
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
//        Vector3d start = player.getPositionVector().addVector(0, player.getEyeHeight(), 0);
        Vector3d start = player.position().add(0, player.getEyeHeight(), 0);
//        Vector3d look = player.getLookVec();
        Vector3d look = player.getLookAngle();
        Vector3d end = start.add(look.scale(7));
//        RayTraceResult ray = world.rayTraceBlocks(start, end, /*liquid*/true, /*ignoreBlockWithoutBoundingBox*/false, /*returnLastUncollidableBlock*/true);
        BlockRayTraceResult ray = world.clip(new RayTraceContext(start, end, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.ANY, player));

//        if (ray == null || ray.getLocation() == null)
        if (ray.getType() == RayTraceResult.Type.MISS) {
            return new ActionResult<>(ActionResultType.FAIL, stack);
        }

        Block b = world.getBlockState(ray.getBlockPos()).getBlock();
        if (b != Blocks.WATER) {
            return new ActionResult<>(ActionResultType.FAIL, stack);
        }

//        if (!player.capabilities.isCreativeMode)
        if (!player.isCreative()) {
//            stack.setCount(stack.getCount() - 1);
            stack.shrink(1);
        }

        // Same as ItemSnowball
        world.playSound(null, player.getX(), player.getY(), player.getZ(),//
                SoundEvents.SNOWBALL_THROW, SoundCategory.NEUTRAL,//
//                0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
                0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

        if (!world.isClientSide) {
            world.setBlock(ray.getBlockPos(), BCFactoryBlocks.waterGel.get().defaultBlockState().setValue(BlockWaterGel.PROP_STAGE, BlockWaterGel.GelStage.SPREAD_0), BlockConstants.UPDATE_ALL);
            world.getBlockTicks().scheduleTick(ray.getBlockPos(), BCFactoryBlocks.waterGel.get(), 200);

            // TODO: Snowball stuff

            // EntitySnowball entitysnowball = new EntitySnowball(world, player);
            // entitysnowball.setHeadingFromThrower(player, player.rotationPitch, player.rotationYaw, 0.0F, 1.5F, 1.0F);
            // world.spawnEntityInWorld(entitysnowball);
        }

        // player.addStat(StatList.getObjectUseStats(this));
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }

}
