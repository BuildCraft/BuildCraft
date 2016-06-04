package buildcraft.lib.misc;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SoundUtil {
    public static void playBlockPlace(World world, BlockPos pos, IBlockState state) {
        SoundType soundType = state.getBlock().getSoundType();
        float volume = (soundType.getVolume() + 1.0F) / 2.0F;
        float pitch = soundType.getPitch() * 0.8F;
        world.playSound(null, pos, soundType.getPlaceSound(), SoundCategory.BLOCKS, volume, pitch);
    }
}
