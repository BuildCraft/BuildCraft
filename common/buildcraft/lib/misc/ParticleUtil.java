package buildcraft.lib.misc;

import javax.annotation.Nullable;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ParticleUtil {
    public static void showChangeColour(World world, Vec3d pos, @Nullable EnumDyeColor colour) {
        if (colour == null) {
            showWaterParticles(world, pos);
        } else {
            
        }
    }

    private static void showWaterParticles(World world, Vec3d pos) {

    }
}
