package buildcraft.lib.client.render.laser;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class LaserContext {
    public final Matrix4f matrix = new Matrix4f();
    private final Point3f point = new Point3f();
    private final ILaserRenderer renderer;
    public final double length;

    public LaserContext(ILaserRenderer renderer, LaserData_BC8 data) {
        this.renderer = renderer;
        Vec3d delta = data.start.subtract(data.end);
        double dx = delta.xCoord;
        double dy = delta.yCoord;
        double dz = delta.zCoord;

        final double angleY, angleZ;

        double realLength = delta.lengthVector();
        length = realLength / data.scale;
        angleZ = Math.PI - Math.atan2(dz, dx);
        double rl_squared = realLength * realLength;
        double dy_dy = dy * dy;
        if (dx == 0 && dz == 0) {
            final double angle = Math.PI / 2;
            if (dy < 0) {
                angleY = angle;
            } else {
                angleY = -angle;
            }
        } else {
            dx = Math.sqrt(rl_squared - dy_dy);
            angleY = -Math.atan2(dy, dx);
        }

        // Matrix steps:
        // 1: rotate angles (Y) to make everything work
        // 2: rotate angles (Z) to make everything work
        // 3: scale it by the laser's scale
        // 4: translate forward by "start"

        matrix.setIdentity();
        Matrix4f holding = new Matrix4f();
        holding.setIdentity();

        // // Step 4
        Vector3f translation = new Vector3f();
        translation.x = (float) data.start.xCoord;
        translation.y = (float) data.start.yCoord;
        translation.z = (float) data.start.zCoord;
        holding.setTranslation(translation);
        matrix.mul(holding);
        holding.setIdentity();

        // Step 3
        holding.m00 = (float) data.scale;
        holding.m11 = (float) data.scale;
        holding.m22 = (float) data.scale;
        matrix.mul(holding);
        holding.setIdentity();

        // Step 2
        holding.rotY((float) angleZ);
        matrix.mul(holding);
        holding.setIdentity();

        // Step 1
        holding.rotZ((float) angleY);
        matrix.mul(holding);
        holding.setIdentity();
    }

    public void addPoint(double x, double y, double z, double u, double v) {
        point.x = (float) x;
        point.y = (float) y;
        point.z = (float) z;
        matrix.transform(point);
        BlockPos pos = new BlockPos(point.x, point.y, point.z);
        int lmap = LaserRenderer_BC8.CACHED_LIGHTMAP.getUnchecked(pos).intValue();
        renderer.vertex(point.x, point.y, point.z, u, v, lmap);
    }
}
