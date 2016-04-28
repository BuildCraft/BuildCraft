package buildcraft.core.client;

import java.util.HashSet;
import java.util.Set;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import buildcraft.api.core.IBox;
import buildcraft.api.items.IMapLocation.MapLocationType;
import buildcraft.core.BCCoreItems;
import buildcraft.core.EntityLaser;
import buildcraft.core.LaserData;
import buildcraft.core.item.ItemMapLocation;
import buildcraft.core.lib.utils.MatrixUtils;
import buildcraft.core.lib.utils.Utils;
import buildcraft.core.render.RenderLaser;
import buildcraft.lib.client.render.LaserData_BC8;
import buildcraft.lib.client.render.LaserData_BC8.LaserType;
import buildcraft.lib.client.render.LaserRenderer_BC8;
import buildcraft.lib.misc.PositionUtil;
import buildcraft.lib.tile.MarkerCache;
import buildcraft.lib.tile.TileMarkerBase;

public enum RenderTickListener {
    INSTANCE;

    private static final Vec3d[][][] MAP_LOCATION_POINT = new Vec3d[6][][];

    static {
        double[][][] upFace = {// Comments for formatting :)
            { { 0.5, 0.9, 0.5 }, { 0.5, 1.6, 0.5 } },// Main line
            { { 0.5, 0.9, 0.5 }, { 0.8, 1.2, 0.5 } }, // First arrow part (+X)
            { { 0.5, 0.9, 0.5 }, { 0.2, 1.2, 0.5 } }, // Second arrow part (-X)
            { { 0.5, 0.9, 0.5 }, { 0.5, 1.2, 0.8 } }, // Third arrow part (+Z)
            { { 0.5, 0.9, 0.5 }, { 0.5, 1.2, 0.2 } }, // Forth arrow part (-Z)
        };

        for (EnumFacing face : EnumFacing.values()) {
            Matrix4f matrix = MatrixUtils.rotateTowardsFace(EnumFacing.UP, face);
            Vec3d[][] arr = new Vec3d[5][2];
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 2; j++) {
                    double[] from = upFace[i][j];
                    Point3f point = new Point3f(new Point3d(from));
                    matrix.transform(point);
                    Vec3d to = new Vec3d(point.x, point.y, point.z);
                    arr[i][j] = to;
                }
            }

            MAP_LOCATION_POINT[face.ordinal()] = arr;
        }
    }

    @SubscribeEvent
    public void tick(RenderWorldLastEvent event) {
        float partialTicks = event.getPartialTicks();
        renderHeldItemInWorld(partialTicks);
    }

    private static void renderHeldItemInWorld(float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player == null) return;
        ItemStack mainHand = player.getHeldItemMainhand();
        ItemStack offHand = player.getHeldItemOffhand();
        WorldClient world = mc.theWorld;

        mc.mcProfiler.startSection("bc");
        mc.mcProfiler.startSection("renderWorld");

        fromPlayerPreGl(player, partialTicks);

        Item mainHandItem = mainHand == null ? null : mainHand.getItem();
        Item offHandItem = offHand == null ? null : offHand.getItem();

        if (mainHandItem == BCCoreItems.mapLocation) {
            renderMapLocation(world, mainHand);
        } else if (mainHandItem == BCCoreItems.markerConnector || offHandItem == BCCoreItems.markerConnector) {
            renderMarkerConnector(world, player, partialTicks);
        }

        fromPlayerPostGl();

        mc.mcProfiler.endSection();
        mc.mcProfiler.endSection();
    }

    private static void fromPlayerPreGl(EntityPlayer player, float partialTicks) {
        GL11.glPushMatrix();

        Vec3d diff = new Vec3d(0, 0, 0);
        diff = diff.subtract(player.getPositionEyes(partialTicks));
        diff = diff.addVector(0, player.getEyeHeight(), 0);
        GL11.glTranslated(diff.xCoord, diff.yCoord, diff.zCoord);

    }

    private static void fromPlayerPostGl() {
        GL11.glPopMatrix();
    }

    private static void renderMapLocation(WorldClient world, ItemStack stack) {
        MapLocationType type = MapLocationType.getFromStack(stack);
        if (type == MapLocationType.SPOT) {
            EnumFacing face = ItemMapLocation.getPointFace(stack);
            IBox box = ItemMapLocation.getPointBox(stack);
            Vec3d[][] vectors = MAP_LOCATION_POINT[face.ordinal()];
            GL11.glTranslated(box.min().getX(), box.min().getY(), box.min().getZ());
            for (Vec3d[] vec : vectors) {
                LaserData laser = new LaserData(vec[0], vec[1]);
                RenderLaser.doRenderLaser(world, Minecraft.getMinecraft().getTextureManager(), laser, EntityLaser.LASER_STRIPES_YELLOW);
            }
        } else if (type == MapLocationType.AREA) {
            IBox box = ItemMapLocation.getAreaBox(stack);
            LaserData[] laserBox = Utils.createLaserDataBox(new Vec3d(box.min()), new Vec3d(box.max().add(1, 1, 1)));

            for (LaserData laser : laserBox) {
                RenderLaser.doRenderLaser(world, Minecraft.getMinecraft().getTextureManager(), laser, EntityLaser.LASER_STRIPES_YELLOW);
            }
        } else if (type == MapLocationType.PATH) {
            // TODO!
        } else if (type == MapLocationType.ZONE) {
            // TODO!
        }
    }

    private static void renderMarkerConnector(WorldClient world, EntityPlayer player, float partialTicks) {
        Profiler profiler = Minecraft.getMinecraft().mcProfiler;
        profiler.startSection("marker");
        for (MarkerCache<?> cache : TileMarkerBase.CACHES) {
            profiler.startSection(cache.name);
            renderMarkerCache(world, player, cache);
            profiler.endSection();
        }
        profiler.endSection();
    }

    private static <T extends TileMarkerBase<T>> void renderMarkerCache(WorldClient world, EntityPlayer player, MarkerCache<T> cache) {
        Profiler profiler = Minecraft.getMinecraft().mcProfiler;
        profiler.startSection("compute");
        Set<LaserData_BC8> toRender = new HashSet<>();
        for (T tile : cache.getCache(world).values()) {
            for (T to : tile.getValidConnections()) {
                BlockPos a = tile.getPos();
                BlockPos b = to.getPos();
                if (a.toLong() > b.toLong()) {
                    BlockPos hold = b;
                    b = a;
                    a = hold;
                }

                Vec3d start = new Vec3d(a).add(Utils.VEC_HALF);
                Vec3d end = new Vec3d(b).add(Utils.VEC_HALF);

                // Add a little offset in the direction of the
                Vec3d startToEnd = end.subtract(start).normalize();
                Vec3d endToStart = start.subtract(end).normalize();
                start = start.add(PositionUtil.scale(startToEnd, 0.125));
                end = end.add(PositionUtil.scale(endToStart, 0.125));

                LaserType laserType = tile.getPossibleLaserType();
                if (laserType == null) laserType = BuildCraftLaserManager.MARKER_DEFAULT_POSSIBLE;
                LaserData_BC8 data = new LaserData_BC8(laserType, start, end, 1 / 16.0);
                toRender.add(data);
            }
        }
        profiler.endStartSection("render");
        for (LaserData_BC8 laser : toRender) {
            LaserRenderer_BC8.renderLaser(laser);
        }
        profiler.endSection();
    }
}
