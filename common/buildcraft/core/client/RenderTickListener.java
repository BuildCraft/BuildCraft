package buildcraft.core.client;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;

import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import buildcraft.BuildCraftCore;
import buildcraft.api.core.IBox;
import buildcraft.api.items.IMapLocation.MapLocationType;
import buildcraft.core.EntityLaser;
import buildcraft.core.ItemMapLocation;
import buildcraft.core.LaserData;
import buildcraft.core.lib.utils.MatrixUtils;
import buildcraft.core.lib.utils.Utils;
import buildcraft.core.render.RenderLaser;

public enum RenderTickListener {
    INSTANCE;

    private static final Vec3[][][] MAP_LOCATION_POINT = new Vec3[6][][];

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
            Vec3[][] arr = new Vec3[5][2];
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 2; j++) {
                    double[] from = upFace[i][j];
                    Point3f point = new Point3f(new Point3d(from));
                    matrix.transform(point);
                    Vec3 to = new Vec3(point.x, point.y, point.z);
                    arr[i][j] = to;
                }
            }

            MAP_LOCATION_POINT[face.ordinal()] = arr;
        }
    }

    @SubscribeEvent
    public void tick(RenderWorldLastEvent event) {
        float partialTicks = event.partialTicks;
        renderHeldItemInWorld(partialTicks);
    }

    private static void renderHeldItemInWorld(float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.getRenderManager().livingPlayer == null) return;

        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        ItemStack held = player.getHeldItem();
        WorldClient world = mc.theWorld;

        mc.mcProfiler.startSection("bc");
        mc.mcProfiler.startSection("renderWorld");

        fromPlayerPreGl(player, partialTicks);

        if (held != null && held.stackSize > 0) {
            Item item = player.getHeldItem().getItem();
            if (item == BuildCraftCore.mapLocationItem) {
                renderMapLocation(world, player.getHeldItem());
            }
        }

        fromPlayerPostGl();

        mc.mcProfiler.endSection();
        mc.mcProfiler.endSection();
    }

    private static void fromPlayerPreGl(EntityPlayer player, float partialTicks) {
        GL11.glPushMatrix();

        Vec3 diff = new Vec3(0, 0, 0);
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
            Vec3[][] vectors = MAP_LOCATION_POINT[face.ordinal()];
            GL11.glTranslated(box.min().getX(), box.min().getY(), box.min().getZ());
            for (Vec3[] vec : vectors) {
                LaserData laser = new LaserData(vec[0], vec[1]);
                RenderLaser.doRenderLaser(world, Minecraft.getMinecraft().getTextureManager(), laser, EntityLaser.LASER_STRIPES_YELLOW);
            }
        } else if (type == MapLocationType.AREA) {
            IBox box = ItemMapLocation.getAreaBox(stack);
            LaserData[] laserBox = Utils.createLaserDataBox(new Vec3(box.min()), new Vec3(box.max().add(1, 1, 1)));

            for (LaserData laser : laserBox) {
                RenderLaser.doRenderLaser(world, Minecraft.getMinecraft().getTextureManager(), laser, EntityLaser.LASER_STRIPES_YELLOW);
            }
        } else if (type == MapLocationType.PATH) {
            // TODO!
        } else if (type == MapLocationType.ZONE) {
            // TODO!
        }
    }
}
