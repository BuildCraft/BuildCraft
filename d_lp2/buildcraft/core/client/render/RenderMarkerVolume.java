package buildcraft.core.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import buildcraft.core.Box;
import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.core.tile.TileMarkerVolume;
import buildcraft.lib.client.render.DetatchedRenderer;
import buildcraft.lib.client.render.LaserData_BC8;
import buildcraft.lib.client.render.LaserData_BC8.LaserType;
import buildcraft.lib.client.render.LaserRenderer_BC8;
import buildcraft.lib.misc.PositionUtil;

public class RenderMarkerVolume extends TileEntitySpecialRenderer<TileMarkerVolume> {
    private static final double SCALE = 1 / 16.05;

    public static final RenderMarkerVolume INSTANCE = new RenderMarkerVolume();

    private static final LaserType LASER_TYPE = BuildCraftLaserManager.MARKER_VOLUME_CONNECTED;
    private static final Vec3d VEC_HALF = new Vec3d(0.5, 0.5, 0.5);

    @Override
    public boolean isGlobalRenderer(TileMarkerVolume te) {
        return true;
    }

    @Override
    public void renderTileEntityAt(TileMarkerVolume marker, double tileX, double tileY, double tileZ, float partialTicks, int destroyStage) {
        if (marker == null) return;
        Box box = marker.box;
        if (box == null && marker.signals == null) return;

        Profiler profiler = Minecraft.getMinecraft().mcProfiler;
        profiler.startSection("bc");
        profiler.startSection("marker");
        profiler.startSection("volume");

        DetatchedRenderer.fromWorldOriginPre(Minecraft.getMinecraft().thePlayer, partialTicks);
        RenderHelper.disableStandardItemLighting();
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        if (box != null) {
            int sizeX = box.size().getX();
            int sizeY = box.size().getY();
            int sizeZ = box.size().getZ();

            BlockPos min = box.min();
            BlockPos max = box.max();

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
                renderLaser(vecs[0][0][0], vecs[1][0][0], Axis.X);
                if (sizeY > 1) {
                    renderLaser(vecs[0][1][0], vecs[1][1][0], Axis.X);
                    if (sizeZ > 1) {
                        renderLaser(vecs[0][1][1], vecs[1][1][1], Axis.X);
                    }
                }
                if (sizeZ > 1) {
                    renderLaser(vecs[0][0][1], vecs[1][0][1], Axis.X);
                }
            }

            if (sizeY > 1) {
                renderLaser(vecs[0][0][0], vecs[0][1][0], Axis.Y);
                if (sizeX > 1) {
                    renderLaser(vecs[1][0][0], vecs[1][1][0], Axis.Y);
                    if (sizeZ > 1) {
                        renderLaser(vecs[1][0][1], vecs[1][1][1], Axis.Y);
                    }
                }
                if (sizeZ > 1) {
                    renderLaser(vecs[0][0][1], vecs[0][1][1], Axis.Y);
                }
            }

            if (box.size().getZ() > 1) {
                renderLaser(vecs[0][0][0], vecs[0][0][1], Axis.Z);
                if (sizeX > 1) {
                    renderLaser(vecs[1][0][0], vecs[1][0][1], Axis.Z);
                    if (sizeY > 0) {
                        renderLaser(vecs[1][1][0], vecs[1][1][1], Axis.Z);
                    }
                }
                if (sizeY > 0) {
                    renderLaser(vecs[0][1][0], vecs[0][1][1], Axis.Z);
                }
            }
        }
        RenderHelper.enableStandardItemLighting();
        DetatchedRenderer.fromWorldOriginPost();

        profiler.endSection();
        profiler.endSection();
        profiler.endSection();
    }

    private static void renderLaser(Vec3d min, Vec3d max, Axis axis) {
        EnumFacing faceForMin = PositionUtil.getFacing(axis, true);
        EnumFacing faceForMax = PositionUtil.getFacing(axis, false);
        Vec3d one = offset(min, faceForMin);
        Vec3d two = offset(max, faceForMax);
        LaserData_BC8 data = new LaserData_BC8(LASER_TYPE, one, two, SCALE);
        LaserRenderer_BC8.renderLaser(data);
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
