package buildcraft.core.client.render;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.core.marker.volume.VolumeMarkerCache;
import buildcraft.core.marker.volume.VolumeMarkerCache.VolumeBox;
import buildcraft.lib.client.render.DetatchedRenderer.IDetachedRenderer;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.client.render.laser.LaserData_BC8.LaserType;
import buildcraft.lib.client.render.laser.LaserRenderer_BC8;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.misc.data.Box;

public enum RenderVolumeInWorld implements IDetachedRenderer {
    INSTANCE;

    private static final double OFFSET_BY = 2 / 16.0;
    private static final double RENDER_SCALE = 1 / 16.05;

    @Override
    public void render(EntityPlayer player, float partialTicks) {
        VertexBuffer vb = Tessellator.getInstance().getBuffer();

        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        for (VolumeBox box : VolumeMarkerCache.SERVER_INSTANCE.boxes) {
            if (box == VolumeMarkerCache.SERVER_INSTANCE.currentlyEditing) {
                renderEditingBox(player, partialTicks, vb);
            } else {
                renderBox(box, vb);
            }
        }

        Tessellator.getInstance().draw();
    }

    private static void renderBox(VolumeBox box, VertexBuffer vb) {
        makeLaserBox(box.box, BuildCraftLaserManager.MARKER_VOLUME_CONNECTED);

        for (LaserData_BC8 data : box.box.laserData) {
            LaserRenderer_BC8.renderLaserBuffer(data, vb);
        }
    }

    private static void renderEditingBox(EntityPlayer player, float partialTicks, VertexBuffer vb) {
        VolumeMarkerCache mk = VolumeMarkerCache.SERVER_INSTANCE;

        BlockPos offset = new BlockPos(player.getPositionEyes(partialTicks).add(player.getLook(partialTicks).scale(mk.dist)));

        mk.renderCache.reset();
        mk.renderCache.extendToEncompass(mk.held);
        mk.renderCache.extendToEncompass(offset);

        makeLaserBox(mk.renderCache, BuildCraftLaserManager.MARKER_VOLUME_SIGNAL);

        for (LaserData_BC8 data : mk.renderCache.laserData) {
            LaserRenderer_BC8.renderLaserBuffer(data, vb);
        }
    }

    private static void makeLaserBox(Box box, LaserType type) {
        BlockPos min = box.min();
        BlockPos max = box.max();

        if (min.equals(box.lastMin) && max.equals(box.lastMax) && box.laserData != null) {
            return;
        }

        List<LaserData_BC8> datas = new ArrayList<>();

        Vec3d[][][] vecs = new Vec3d[2][2][2];
        vecs[0][0][0] = new Vec3d(min);
        vecs[1][0][0] = new Vec3d(new BlockPos(max.getX(), min.getY(), min.getZ()));
        vecs[0][1][0] = new Vec3d(new BlockPos(min.getX(), max.getY(), min.getZ()));
        vecs[1][1][0] = new Vec3d(new BlockPos(max.getX(), max.getY(), min.getZ()));
        vecs[0][0][1] = new Vec3d(new BlockPos(min.getX(), min.getY(), max.getZ()));
        vecs[1][0][1] = new Vec3d(new BlockPos(max.getX(), min.getY(), max.getZ()));
        vecs[0][1][1] = new Vec3d(new BlockPos(min.getX(), max.getY(), max.getZ()));
        vecs[1][1][1] = new Vec3d(max);

        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                for (int z = 0; z < 2; z++) {
                    Vec3d offset = new Vec3d((x * 2 - 1) / 4.0 + 0.5, (y * 2 - 1) / 4.0 + 0.5, (z * 2 - 1) / 4.0 + 0.5);
                    vecs[x][y][z] = vecs[x][y][z].add(offset);
                }
            }
        }

        datas.add(makeLaser(type, vecs[0][0][0], vecs[1][0][0], Axis.X));
        datas.add(makeLaser(type, vecs[0][1][0], vecs[1][1][0], Axis.X));
        datas.add(makeLaser(type, vecs[0][1][1], vecs[1][1][1], Axis.X));
        datas.add(makeLaser(type, vecs[0][0][1], vecs[1][0][1], Axis.X));

        datas.add(makeLaser(type, vecs[0][0][0], vecs[0][1][0], Axis.Y));
        datas.add(makeLaser(type, vecs[1][0][0], vecs[1][1][0], Axis.Y));
        datas.add(makeLaser(type, vecs[1][0][1], vecs[1][1][1], Axis.Y));
        datas.add(makeLaser(type, vecs[0][0][1], vecs[0][1][1], Axis.Y));

        datas.add(makeLaser(type, vecs[0][0][0], vecs[0][0][1], Axis.Z));
        datas.add(makeLaser(type, vecs[1][0][0], vecs[1][0][1], Axis.Z));
        datas.add(makeLaser(type, vecs[1][1][0], vecs[1][1][1], Axis.Z));
        datas.add(makeLaser(type, vecs[0][1][0], vecs[0][1][1], Axis.Z));

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
        double by = OFFSET_BY;
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
