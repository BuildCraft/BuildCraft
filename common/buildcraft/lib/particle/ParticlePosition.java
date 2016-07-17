package buildcraft.lib.particle;

import net.minecraft.util.math.Vec3d;

public class ParticlePosition {
    public final Vec3d position, motion;

    public ParticlePosition(Vec3d position, Vec3d motion) {
        this.position = position;
        this.motion = motion;
    }
}
