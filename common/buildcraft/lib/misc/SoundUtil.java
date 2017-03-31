package buildcraft.lib.misc;

import javax.annotation.Nullable;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SoundUtil {
    public static void playBlockPlace(World world, BlockPos pos) {
        playBlockPlace(world, pos, world.getBlockState(pos));
    }

    public static void playBlockPlace(World world, BlockPos pos, IBlockState state) {
        SoundType soundType = state.getBlock().getSoundType(state, world, pos, null);
        float volume = (soundType.getVolume() + 1.0F) / 2.0F;
        float pitch = soundType.getPitch() * 0.8F;
        world.playSound(null, pos, soundType.getPlaceSound(), SoundCategory.BLOCKS, volume, pitch);
    }

    public static void playBlockBreak(World world, BlockPos pos) {
        playBlockBreak(world, pos, world.getBlockState(pos));
    }

    public static void playBlockBreak(World world, BlockPos pos, IBlockState state) {
        SoundType soundType = state.getBlock().getSoundType(state, world, pos, null);
        float volume = (soundType.getVolume() + 1.0F) / 2.0F;
        float pitch = soundType.getPitch() * 0.8F;
        world.playSound(null, pos, soundType.getBreakSound(), SoundCategory.BLOCKS, volume, pitch);
    }

    public static void playChangeColour(World world, BlockPos pos, @Nullable EnumDyeColor colour) {
        SoundType soundType = SoundType.SLIME;
        final SoundEvent soundEvent;
        if (colour == null) {
            soundEvent = SoundEvents.ITEM_BUCKET_EMPTY;
        } else {
            // FIXME: is this a good sound? Idk tbh.
            // TODO: Look into configuring this kind of stuff.
            soundEvent = SoundEvents.ENTITY_SLIME_SQUISH;
        }
        float volume = (soundType.getVolume() + 1.0F) / 2.0F;
        float pitch = soundType.getPitch() * 0.8F;
        world.playSound(null, pos, soundEvent, SoundCategory.BLOCKS, volume, pitch);
    }

    public static void playSlideSound(World world, BlockPos pos) {
        playSlideSound(world, pos, world.getBlockState(pos));
    }

    public static void playSlideSound(World world, BlockPos pos, EnumActionResult result) {
        playSlideSound(world, pos, world.getBlockState(pos), result);
    }

    public static void playSlideSound(World world, BlockPos pos, IBlockState state) {
        playSlideSound(world, pos, state, EnumActionResult.SUCCESS);
    }

    public static void playSlideSound(World world, BlockPos pos, IBlockState state, EnumActionResult result) {
        if (result == EnumActionResult.PASS) return;
        SoundType soundType = state.getBlock().getSoundType(state, world, pos, null);
        SoundEvent soundEvent = result == EnumActionResult.SUCCESS ? SoundEvents.BLOCK_PISTON_CONTRACT : SoundEvents.BLOCK_PISTON_EXTEND;
        float volume = (soundType.getVolume() + 1.0F) / 2.0F;
        float pitch = soundType.getPitch() * 0.8F;
        world.playSound(null, pos, soundEvent, SoundCategory.BLOCKS, volume, pitch);
    }
}
