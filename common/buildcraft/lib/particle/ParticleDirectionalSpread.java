package buildcraft.lib.particle;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;

public enum ParticleDirectionalSpread implements IParticlePositionPipe {
    MINIMAL(2),
    DECREASED(7),
    ALL(13);

    private static final double MOTION_MAX_DIFF = 0.02;

    public static ParticleDirectionalSpread getForOption() {
        GameSettings gs = Minecraft.getMinecraft().gameSettings;
        int count = gs.particleSetting % 3;
        if (count == 0) {
            return ALL;
        } else if (count == 1) {
            return DECREASED;
        }
        return MINIMAL;
    }

    private final int numExpanses;

    private ParticleDirectionalSpread(int numExpanses) {
        this.numExpanses = numExpanses;
    }

    @Override
    public List<ParticlePosition> pipe(ParticlePosition pos) {
        List<ParticlePosition> list = new ArrayList<>();

        for (int i = 0; i < numExpanses; i++) {
            Vec3d nMotion = modifyMotion(pos.motion, pos.idealMotion);
            list.add(new ParticlePosition(pos.position, nMotion, pos.idealMotion));
        }

        return list;
    }

    private Vec3d modifyMotion(Vec3d motion, EnumFacing idealMotion) {
        double dx = getDelta(idealMotion == null ? 0 : idealMotion.getFrontOffsetX());
        double dy = getDelta(idealMotion == null ? 0 : idealMotion.getFrontOffsetY());
        double dz = getDelta(idealMotion == null ? 0 : idealMotion.getFrontOffsetZ());
        return motion.addVector(dx, dy, dz);
    }

    private double getDelta(int o) {
        double subtract = -o / 2.0 + 0.5;
        return (Math.random() - subtract) * MOTION_MAX_DIFF * 2;
    }
}
