/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.item;

import buildcraft.api.BCBlocks;
import buildcraft.factory.block.BlockWaterGel;
import buildcraft.factory.block.BlockWaterGel.GelStage;
import buildcraft.lib.item.ItemBC_Neptune;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ItemWaterGel extends ItemBC_Neptune {

    public ItemWaterGel(String id) {
        super(id);
        this.maxStackSize = 16;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemStack, World world, EntityPlayer player, EnumHand hand) {
        Vec3d start = player.getPositionVector().addVector(0, player.getEyeHeight(), 0);
        Vec3d look = player.getLookVec();
        Vec3d end = start.add(look.scale(7));
        RayTraceResult ray = world.rayTraceBlocks(start, end, true, false, true);

        if (ray == null || ray.getBlockPos() == null) {
            return new ActionResult<>(EnumActionResult.FAIL, itemStack);
        }

        Block b = world.getBlockState(ray.getBlockPos()).getBlock();
        if (b != Blocks.WATER) {
            return new ActionResult<>(EnumActionResult.FAIL, itemStack);
        }

        if (!player.capabilities.isCreativeMode) {
            itemStack.stackSize -= 1;
        }

        // Same as ItemSnowball
        world.playSound(null, player.posX, player.posY, player.posZ,//
                SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL,//
                0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

        if (!world.isRemote) {
            world.setBlockState(ray.getBlockPos(), BCBlocks.Factory.WATER_GEL.getDefaultState().withProperty(BlockWaterGel.PROP_STAGE, GelStage.SPREAD_0));
            world.scheduleUpdate(ray.getBlockPos(), BCBlocks.Factory.WATER_GEL, 200);

            // TODO: Snowball stuff

            // EntitySnowball entitysnowball = new EntitySnowball(world, player);
            // entitysnowball.setHeadingFromThrower(player, player.rotationPitch, player.rotationYaw, 0.0F, 1.5F, 1.0F);
            // world.spawnEntityInWorld(entitysnowball);
        }

        // player.addStat(StatList.getObjectUseStats(this));
        return new ActionResult<>(EnumActionResult.SUCCESS, itemStack);
    }

}
