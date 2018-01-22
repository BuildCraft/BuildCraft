/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
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
import net.minecraft.util.text.TextFormatting;

import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import buildcraft.api.core.IBox;
import buildcraft.api.items.IMapLocation.MapLocationType;
import buildcraft.api.tiles.IDebuggable;

import buildcraft.lib.client.render.DetachedRenderer;
import buildcraft.lib.client.render.laser.LaserBoxRenderer;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.client.render.laser.LaserData_BC8.LaserType;
import buildcraft.lib.client.render.laser.LaserRenderer_BC8;
import buildcraft.lib.debug.ClientDebuggables;
import buildcraft.lib.marker.MarkerCache;
import buildcraft.lib.marker.MarkerSubCache;
import buildcraft.lib.misc.MatrixUtil;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.misc.data.Box;

import buildcraft.core.BCCoreItems;
import buildcraft.core.item.ItemMapLocation;
import buildcraft.core.item.ItemMarkerConnector;

public class RenderTickListener {
    private static final Vec3d[][][] MAP_LOCATION_POINT = new Vec3d[6][][];
    private static final String DIFF_START, DIFF_HEADER_FORMATTING;

    private static final Box LAST_RENDERED_MAP_LOC = new Box();

    static {
        double[][][] upFace = { // Comments for formatting
            { { 0.5, 0.9, 0.5 }, { 0.5, 1.6, 0.5 } }, // Main line
            { { 0.5, 0.9, 0.5 }, { 0.8, 1.2, 0.5 } }, // First arrow part (+X)
            { { 0.5, 0.9, 0.5 }, { 0.2, 1.2, 0.5 } }, // Second arrow part (-X)
            { { 0.5, 0.9, 0.5 }, { 0.5, 1.2, 0.8 } }, // Third arrow part (+Z)
            { { 0.5, 0.9, 0.5 }, { 0.5, 1.2, 0.2 } }, // Forth arrow part (-Z)
        };

        for (EnumFacing face : EnumFacing.VALUES) {
            Matrix4f matrix = MatrixUtil.rotateTowardsFace(EnumFacing.UP, face);
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
        DIFF_START = TextFormatting.RED + "" + TextFormatting.BOLD + "!" + TextFormatting.RESET;
        DIFF_HEADER_FORMATTING = TextFormatting.AQUA + "" + TextFormatting.BOLD;
    }

    @SubscribeEvent
    public static void renderOverlay(RenderGameOverlayEvent.Text event) {
        Minecraft mc = Minecraft.getMinecraft();
        IDebuggable debuggable = ClientDebuggables.getDebuggableObject(mc.objectMouseOver);
        if (debuggable != null) {
            List<String> clientLeft = new ArrayList<>();
            List<String> clientRight = new ArrayList<>();
            debuggable.getDebugInfo(clientLeft, clientRight, mc.objectMouseOver.sideHit);
            String headerFirst = DIFF_HEADER_FORMATTING + "SERVER:";
            String headerSecond = DIFF_HEADER_FORMATTING + "CLIENT:";
            appendDiff(event.getLeft(), ClientDebuggables.SERVER_LEFT, clientLeft, headerFirst, headerSecond);
            appendDiff(event.getRight(), ClientDebuggables.SERVER_RIGHT, clientRight, headerFirst, headerSecond);
            debuggable.getClientDebugInfo(event.getLeft(), event.getRight(), mc.objectMouseOver.sideHit);
        }
    }

    private static void appendDiff(List<String> dest, List<String> first, List<String> second, String headerFirst,
        String headerSecond) {
        dest.add("");
        dest.add(headerFirst);
        dest.addAll(first);
        dest.add("");
        dest.add(headerSecond);
        if (first.size() != second.size()) {
            // no diffing
            dest.addAll(second);
        } else {
            for (int l = 0; l < first.size(); l++) {
                String shownLine = first.get(l);
                String diffLine = second.get(l);
                if (shownLine.equals(diffLine)) {
                    dest.add(diffLine);
                } else {
                    if (diffLine.startsWith(" ")) {
                        dest.add(DIFF_START + diffLine.substring(1));
                    } else {
                        dest.add(DIFF_START + diffLine);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void renderLast(RenderWorldLastEvent event) {
        float partialTicks = event.getPartialTicks();
        renderHeldItemInWorld(partialTicks);
    }

    private static void renderHeldItemInWorld(float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player == null) {
            return;
        }
        ItemStack mainHand = player.getHeldItemMainhand();
        ItemStack offHand = player.getHeldItemOffhand();
        WorldClient world = mc.world;

        mc.mcProfiler.startSection("bc");
        mc.mcProfiler.startSection("renderWorld");

        DetachedRenderer.fromWorldOriginPre(player, partialTicks);

        Item mainHandItem = mainHand != null ? mainHand.getItem() : null;
        Item offHandItem = offHand != null ? offHand.getItem() : null;

        if (mainHandItem != null) {
            if (mainHandItem == BCCoreItems.mapLocation) {
                renderMapLocation(mainHand);
            } else if (mainHandItem == BCCoreItems.markerConnector || offHandItem != null && offHandItem == BCCoreItems.markerConnector) {
                renderMarkerConnector(world, player);
            }
        }

        DetachedRenderer.fromWorldOriginPost();

        mc.mcProfiler.endSection();
        mc.mcProfiler.endSection();
    }

    private static void renderMapLocation(@Nonnull ItemStack stack) {
        MapLocationType type = MapLocationType.getFromStack(stack);
        switch (type) {
            case SPOT: {
                EnumFacing face = ItemMapLocation.getPointFace(stack);
                IBox box = ItemMapLocation.getPointBox(stack);
                if (box != null) {
                    Vec3d[][] vectors = MAP_LOCATION_POINT[face.ordinal()];
                    GL11.glTranslated(box.min().getX(), box.min().getY(), box.min().getZ());
                    for (Vec3d[] vec : vectors) {
                        LaserData_BC8 laser =
                                new LaserData_BC8(BuildCraftLaserManager.STRIPES_WRITE, vec[0], vec[1], 1 / 16.0);
                        LaserRenderer_BC8.renderLaserStatic(laser);
                    }
                }

                break;
            }
            case AREA: {

                IBox box = ItemMapLocation.getAreaBox(stack);
                LAST_RENDERED_MAP_LOC.reset();
                LAST_RENDERED_MAP_LOC.initialize(box);
                LaserBoxRenderer.renderLaserBoxStatic(LAST_RENDERED_MAP_LOC, BuildCraftLaserManager.STRIPES_WRITE, true);

                break;
            }
            case PATH:
                List<BlockPos> path = BCCoreItems.mapLocation.getPath(stack);
                if (path != null && path.size() > 1) {
                    BlockPos last = null;
                    for (BlockPos p : path) {
                        if (last == null) {
                            last = p;
                        }
                    }
                }

                // TODO!
                break;
            case ZONE:
                // TODO!
                break;
        }
    }

    private static void renderMarkerConnector(WorldClient world, EntityPlayer player) {
        Profiler profiler = Minecraft.getMinecraft().mcProfiler;
        profiler.startSection("marker");
        for (MarkerCache<?> cache : MarkerCache.CACHES) {
            profiler.startSection(cache.name);
            renderMarkerCache(player, cache.getSubCache(world));
            profiler.endSection();
        }
        profiler.endSection();
    }

    private static void renderMarkerCache(EntityPlayer player, MarkerSubCache<?> cache) {
        Profiler profiler = Minecraft.getMinecraft().mcProfiler;
        profiler.startSection("compute");
        Set<LaserData_BC8> toRender = new HashSet<>();
        for (final BlockPos a : cache.getAllMarkers()) {
            for (final BlockPos b : cache.getValidConnections(a)) {
                if (a.toLong() > b.toLong()) {
                    // Only render each pair once
                    continue;
                }

                Vec3d start = VecUtil.convertCenter(a);
                Vec3d end = VecUtil.convertCenter(b);

                Vec3d startToEnd = end.subtract(start).normalize();
                Vec3d endToStart = start.subtract(end).normalize();
                start = start.add(VecUtil.scale(startToEnd, 0.125));
                end = end.add(VecUtil.scale(endToStart, 0.125));

                LaserType laserType = cache.getPossibleLaserType();
                if (laserType == null || isLookingAt(a, b, player)) {
                    laserType = BuildCraftLaserManager.MARKER_DEFAULT_POSSIBLE;
                }

                LaserData_BC8 data = new LaserData_BC8(laserType, start, end, 1 / 16.0);
                toRender.add(data);
            }
        }
        profiler.endStartSection("render");
        for (LaserData_BC8 laser : toRender) {
            LaserRenderer_BC8.renderLaserStatic(laser);
        }
        profiler.endSection();
    }

    private static boolean isLookingAt(BlockPos from, BlockPos to, EntityPlayer player) {
        return ItemMarkerConnector.doesInteract(from, to, player);
    }
}
