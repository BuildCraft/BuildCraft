package buildcraft.lib.particle;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.math.Vec3d;

public enum ParticleDirectionalSpread implements IParticlePositionPipe {
    SMALL(0.01),
    MEDIUM(0.02),
    LARGE(0.04),
    MASSIVE(0.08);

    private final double motionDiff;

    ParticleDirectionalSpread(double motionDiff) {
        this.motionDiff = motionDiff;
    }

    @Override
    public List<ParticlePosition> pipe(ParticlePosition pos) {
        List<ParticlePosition> list = new ArrayList<>();

        Vec3d nMotion = modifyMotion(pos.motion);
        list.add(new ParticlePosition(pos.position, nMotion));

        return list;
    }

    private Vec3d modifyMotion(Vec3d motion) {
        double dx = getRandom();
        double dy = getRandom();
        double dz = getRandom();
        return motion.addVector(dx, dy, dz);
    }

    private double getRandom() {
        return (Math.random() - 0.5) * motionDiff;
    }
}
