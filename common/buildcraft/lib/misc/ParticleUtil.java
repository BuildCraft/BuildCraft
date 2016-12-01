package buildcraft.lib.misc;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import buildcraft.lib.particle.ParticlePipes;
import buildcraft.lib.particle.ParticlePosition;

public class ParticleUtil {
    public static void showChangeColour(World world, Vec3d pos, @Nullable EnumDyeColor colour) {
        if (colour == null) {
            showWaterParticles(world, pos);
        } else {

        }
    }

    private static void showWaterParticles(World world, Vec3d pos) {

    }

    public static void showTempPower(World world, BlockPos pos, EnumFacing face, long microJoules) {
        world = Minecraft.getMinecraft().world;

        double x = pos.getX() + 0.5 + face.getFrontOffsetX() * 0.5;
        double y = pos.getY() + 0.5 + face.getFrontOffsetY() * 0.5;
        double z = pos.getZ() + 0.5 + face.getFrontOffsetZ() * 0.5;

        Vec3d startingMotion = new Vec3d(face.getDirectionVec());
        startingMotion = VecUtil.scale(startingMotion, 0.05);

        ParticlePosition nPos = new ParticlePosition(new Vec3d(x, y, z), startingMotion);

        for (ParticlePosition pp : ParticlePipes.DUPLICATE_SPREAD.pipe(nPos)) {
            world.spawnParticle(EnumParticleTypes.FLAME, x, y, z, pp.motion.xCoord, pp.motion.yCoord, pp.motion.zCoord);
        }
    }
}
