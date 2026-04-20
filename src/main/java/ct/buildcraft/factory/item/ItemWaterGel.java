/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.factory.item;

import ct.buildcraft.core.BCCore;
import ct.buildcraft.factory.BCFactoryBlocks;
import ct.buildcraft.factory.block.BlockWaterGel;
import ct.buildcraft.factory.block.BlockWaterGel.GelStage;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;

public class ItemWaterGel extends Item {

    public ItemWaterGel() {
        super(new Item.Properties().stacksTo(16).tab(BCCore.BUILDCRAFT_TAB));
    }
    
    @Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockHitResult ray = getPlayerPOVHitResult(world, player, ClipContext.Fluid.SOURCE_ONLY);//start, end, true, false, true);

        if (ray == null || ray.getBlockPos() == null) {
            return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
        }

        Block b = world.getBlockState(ray.getBlockPos()).getBlock();

        if (b != Blocks.WATER) {
            return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
        }
        
        if (!player.isCreative()) {
            stack.setCount(stack.getCount() - 1);
        }

        // Same as ItemSnowball
        world.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));

        if (!world.isClientSide) {
            world.setBlockAndUpdate(ray.getBlockPos(), BCFactoryBlocks.WATER_GEL.get().defaultBlockState().setValue(BlockWaterGel.PROP_STAGE, GelStage.SPREAD_0));
            world.scheduleTick(ray.getBlockPos(), BCFactoryBlocks.WATER_GEL.get(), 200);

            // TODO: Snowball stuff

            // EntitySnowball entitysnowball = new EntitySnowball(world, player);
            // entitysnowball.setHeadingFromThrower(player, player.rotationPitch, player.rotationYaw, 0.0F, 1.5F, 1.0F);
            // world.spawnEntityInWorld(entitysnowball);
        }

        // player.addStat(StatList.getObjectUseStats(this));
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
	}


}
