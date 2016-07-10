package buildcraft.lib.particle;

import javax.annotation.Nullable;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;

public class ParticlePosition {
    public final Vec3d position, motion;

    @Nullable
    public final EnumFacing idealMotion;

    public ParticlePosition(Vec3d position, Vec3d motion, @Nullable EnumFacing idealMotion) {
        this.position = position;
        this.motion = motion;
        this.idealMotion = idealMotion;
    }
}
