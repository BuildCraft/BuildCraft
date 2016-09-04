package buildcraft.lib.client.render.laser;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import buildcraft.lib.client.render.laser.LaserData_BC8.LaserType;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.misc.data.Box;

public class LaserBoxRenderer {
    private static final double RENDER_SCALE = 1 / 16.05;
    private static final Vec3d VEC_HALF = new Vec3d(0.5, 0.5, 0.5);

    public static void renderLaserBoxGl(Box box, LaserType type) {
        if (box == null || box.min() == null || box.max() == null) return;

        makeLaserBox(box, type);

        for (LaserData_BC8 data : box.laserData) {
            LaserRenderer_BC8.renderLaserGlList(data);
        }
    }

    public static void renderLaserBoxVb(Box box, LaserType type, VertexBuffer vb) {
        if (box == null || box.min() == null || box.max() == null) return;

        makeLaserBox(box, type);

        for (LaserData_BC8 data : box.laserData) {
            LaserRenderer_BC8.renderLaserBuffer(data, vb);
        }
    }

    private static void makeLaserBox(Box box, LaserType type) {
        int sizeX = box.size().getX();
        int sizeY = box.size().getY();
        int sizeZ = box.size().getZ();

        BlockPos min = box.min();
        BlockPos max = box.max();

        if (min.equals(box.lastMin) && max.equals(box.lastMax) && box.laserData != null) {
            return;
        }

        List<LaserData_BC8> datas = new ArrayList<>();

        Vec3d[][][] vecs = new Vec3d[2][2][2];
        vecs[0][0][0] = new Vec3d(min).add(VEC_HALF);
        vecs[1][0][0] = new Vec3d(new BlockPos(max.getX(), min.getY(), min.getZ())).add(VEC_HALF);
        vecs[0][1][0] = new Vec3d(new BlockPos(min.getX(), max.getY(), min.getZ())).add(VEC_HALF);
        vecs[1][1][0] = new Vec3d(new BlockPos(max.getX(), max.getY(), min.getZ())).add(VEC_HALF);
        vecs[0][0][1] = new Vec3d(new BlockPos(min.getX(), min.getY(), max.getZ())).add(VEC_HALF);
        vecs[1][0][1] = new Vec3d(new BlockPos(max.getX(), min.getY(), max.getZ())).add(VEC_HALF);
        vecs[0][1][1] = new Vec3d(new BlockPos(min.getX(), max.getY(), max.getZ())).add(VEC_HALF);
        vecs[1][1][1] = new Vec3d(max).add(VEC_HALF);

        if (sizeX > 1) {
            datas.add(makeLaser(type, vecs[0][0][0], vecs[1][0][0], Axis.X));
            if (sizeY > 1) {
                datas.add(makeLaser(type, vecs[0][1][0], vecs[1][1][0], Axis.X));
                if (sizeZ > 1) {
                    datas.add(makeLaser(type, vecs[0][1][1], vecs[1][1][1], Axis.X));
                }
            }
            if (sizeZ > 1) {
                datas.add(makeLaser(type, vecs[0][0][1], vecs[1][0][1], Axis.X));
            }
        }

        if (sizeY > 1) {
            datas.add(makeLaser(type, vecs[0][0][0], vecs[0][1][0], Axis.Y));
            if (sizeX > 1) {
                datas.add(makeLaser(type, vecs[1][0][0], vecs[1][1][0], Axis.Y));
                if (sizeZ > 1) {
                    datas.add(makeLaser(type, vecs[1][0][1], vecs[1][1][1], Axis.Y));
                }
            }
            if (sizeZ > 1) {
                datas.add(makeLaser(type, vecs[0][0][1], vecs[0][1][1], Axis.Y));
            }
        }

        if (box.size().getZ() > 1) {
            datas.add(makeLaser(type, vecs[0][0][0], vecs[0][0][1], Axis.Z));
            if (sizeX > 1) {
                datas.add(makeLaser(type, vecs[1][0][0], vecs[1][0][1], Axis.Z));
                if (sizeY > 0) {
                    datas.add(makeLaser(type, vecs[1][1][0], vecs[1][1][1], Axis.Z));
                }
            }
            if (sizeY > 0) {
                datas.add(makeLaser(type, vecs[0][1][0], vecs[0][1][1], Axis.Z));
            }
        }

        box.laserData = datas.toArray(new LaserData_BC8[datas.size()]);
        box.lastMin = min;
        box.lastMax = max;
    }

    private static LaserData_BC8 makeLaser(LaserType type, Vec3d min, Vec3d max, Axis axis) {
        EnumFacing faceForMin = VecUtil.getFacing(axis, true);
        EnumFacing faceForMax = VecUtil.getFacing(axis, false);
        Vec3d one = offset(min, faceForMin);
        Vec3d two = offset(max, faceForMax);
        return new LaserData_BC8(type, one, two, RENDER_SCALE);
    }

    private static Vec3d offset(Vec3d vec, EnumFacing face) {
        double by = 1 / 16.0;
        if (face == EnumFacing.DOWN) {
            return vec.addVector(0, -by, 0);
        } else if (face == EnumFacing.UP) {
            return vec.addVector(0, by, 0);
        } else if (face == EnumFacing.EAST) {
            return vec.addVector(by, 0, 0);
        } else if (face == EnumFacing.WEST) {
            return vec.addVector(-by, 0, 0);
        } else if (face == EnumFacing.SOUTH) {
            return vec.addVector(0, 0, by);
        } else {// North
            return vec.addVector(0, 0, -by);
        }
    }
}
