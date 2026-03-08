/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;

public class SoundUtil {
    public static void playBlockPlace(World world, BlockPos pos) {
        playBlockPlace(world, pos, world.getBlockState(pos));
    }

    public static void playBlockPlace(World world, BlockPos pos, BlockState state) {
        SoundType soundType = state.getBlock().getSoundType(state, world, pos, null);
        float volume = (soundType.getVolume() + 1.0F) / 2.0F;
        float pitch = soundType.getPitch() * 0.8F;
        world.playSound(null, pos, soundType.getPlaceSound(), SoundCategory.BLOCKS, volume, pitch);
    }

    public static void playBlockBreak(World world, BlockPos pos) {
        playBlockBreak(world, pos, world.getBlockState(pos));
    }

    public static void playBlockBreak(World world, BlockPos pos, BlockState state) {
        SoundType soundType = state.getBlock().getSoundType(state, world, pos, null);
        float volume = (soundType.getVolume() + 1.0F) / 2.0F;
        float pitch = soundType.getPitch() * 0.8F;
        world.playSound(null, pos, soundType.getBreakSound(), SoundCategory.BLOCKS, volume, pitch);
    }

    public static void playLeverSwitch(World world, BlockPos pos, boolean isNowOn) {
        float pitch = isNowOn ? 0.6f : 0.5f;
        SoundEvent soundEvent = SoundEvents.LEVER_CLICK;
        world.playSound(null, pos, soundEvent, SoundCategory.BLOCKS, 0.2f, pitch);
    }

    public static void playChangeColour(World world, BlockPos pos, @Nullable DyeColor colour) {
        SoundType soundType = SoundType.SLIME_BLOCK;
        final SoundEvent soundEvent;
        if (colour == null) {
            soundEvent = SoundEvents.BUCKET_EMPTY;
        } else {
            // FIXME: is this a good sound? Idk tbh.
            // TODO: Look into configuring this kind of stuff.
            soundEvent = SoundEvents.SLIME_SQUISH;
        }
        float volume = (soundType.getVolume() + 1.0F) / 2.0F;
        float pitch = soundType.getPitch() * 0.8F;
        world.playSound(null, pos, soundEvent, SoundCategory.BLOCKS, volume, pitch);
    }

    public static void playSlideSound(World world, BlockPos pos) {
        playSlideSound(world, pos, world.getBlockState(pos));
    }

    public static void playSlideSound(World world, BlockPos pos, ActionResultType result) {
        playSlideSound(world, pos, world.getBlockState(pos), result);
    }

    public static void playSlideSound(World world, BlockPos pos, BlockState state) {
        playSlideSound(world, pos, state, ActionResultType.SUCCESS);
    }

    public static void playSlideSound(World world, BlockPos pos, BlockState state, ActionResultType result) {
        if (result == ActionResultType.PASS) return;
        SoundType soundType = state.getBlock().getSoundType(state, world, pos, null);
        SoundEvent event;
        if (result == ActionResultType.SUCCESS) {
            event = SoundEvents.PISTON_CONTRACT;
        } else {
            event = SoundEvents.PISTON_EXTEND;
        }
        float volume = (soundType.getVolume() + 1.0F) / 2.0F;
        float pitch = soundType.getPitch() * 0.8F;
        world.playSound(null, pos, event, SoundCategory.BLOCKS, volume, pitch);
    }

    public static void playBucketEmpty(World world, BlockPos pos, FluidStack moved) {
        SoundEvent sound = moved.getRawFluid().getAttributes().getEmptySound(moved);
        world.playSound(null, pos, sound, SoundCategory.PLAYERS, 1, 1);
    }

    public static void playBucketFill(World world, BlockPos pos, FluidStack moved) {
        SoundEvent sound = moved.getRawFluid().getAttributes().getFillSound(moved);
        world.playSound(null, pos, sound, SoundCategory.PLAYERS, 1, 1);
    }
}
