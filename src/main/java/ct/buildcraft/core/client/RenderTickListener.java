/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package ct.buildcraft.core.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector4f;

import ct.buildcraft.api.core.IBox;
import ct.buildcraft.api.items.IMapLocation.MapLocationType;
import ct.buildcraft.api.tiles.IDebuggable;
import ct.buildcraft.core.BCCoreItems;
import ct.buildcraft.core.item.ItemMapLocation;
import ct.buildcraft.core.item.ItemMarkerConnector;
import ct.buildcraft.lib.client.render.DetachedRenderer;
import ct.buildcraft.lib.client.render.laser.LaserBoxRenderer;
import ct.buildcraft.lib.client.render.laser.LaserData_BC8;
import ct.buildcraft.lib.client.render.laser.LaserData_BC8.LaserType;
import ct.buildcraft.lib.client.render.laser.LaserRenderer_BC8;
import ct.buildcraft.lib.debug.ClientDebuggables;
import ct.buildcraft.lib.marker.MarkerCache;
import ct.buildcraft.lib.marker.MarkerSubCache;
import ct.buildcraft.lib.misc.MatrixUtil;
import ct.buildcraft.lib.misc.VecUtil;
import ct.buildcraft.lib.misc.data.Box;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;

public class RenderTickListener {
    private static final Vec3[][][] MAP_LOCATION_POINT = new Vec3[6][][];
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

        for (Direction face : Direction.values()) {
            Matrix4f matrix = MatrixUtil.rotateTowardsFace(Direction.UP, face);
            Vec3[][] arr = new Vec3[5][2];
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 2; j++) {
                    double[] from = upFace[i][j];
                    Vector4f point = new Vector4f((float)from[0], (float)from[1], (float)from[2], 1);
                    point.transform(matrix);
                    Vec3 to = new Vec3(point.x(), point.y(), point.z());
                    arr[i][j] = to;
                }
            }

            MAP_LOCATION_POINT[face.ordinal()] = arr;
        }
        DIFF_START = ChatFormatting.RED + "" + ChatFormatting.BOLD + "!" + ChatFormatting.RESET;
        DIFF_HEADER_FORMATTING = ChatFormatting.AQUA + "" + ChatFormatting.BOLD;
    }

    public static void renderOverlay(CustomizeGuiOverlayEvent.DebugText event) {
        Minecraft mc = Minecraft.getInstance();
        IDebuggable debuggable = ClientDebuggables.getDebuggableObject(mc.hitResult);
        if (debuggable != null) {
            List<String> clientLeft = new ArrayList<>();
            List<String> clientRight = new ArrayList<>();
            Direction face = mc.cameraEntity.getDirection().getOpposite();//TODO CHECK
            debuggable.getDebugInfo(clientLeft, clientRight, face);
            String headerFirst = DIFF_HEADER_FORMATTING + "SERVER:";
            String headerSecond = DIFF_HEADER_FORMATTING + "CLIENT:";
            appendDiff(event.getLeft(), ClientDebuggables.SERVER_LEFT, clientLeft, headerFirst, headerSecond);
            appendDiff(event.getRight(), ClientDebuggables.SERVER_RIGHT, clientRight, headerFirst, headerSecond);
            debuggable.getClientDebugInfo(event.getLeft(), event.getRight(), face);
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

    public static void renderLast(RenderLevelStageEvent event) {
        float partialTicks = event.getPartialTick();
        PoseStack poseStack = event.getPoseStack();
        Matrix4f matrix = event.getProjectionMatrix();
        renderHeldItemInWorld(poseStack, matrix, partialTicks);
    }

    private static void renderHeldItemInWorld(PoseStack poseStack, Matrix4f matrix, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            return;
        }
        ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND);
        ClientLevel world = mc.level;

        mc.getProfiler().push("bc");
        mc.getProfiler().push("renderWorld");

        DetachedRenderer.fromWorldOriginPre(poseStack, matrix, partialTicks);

        Item mainHandItem = mainHand.getItem();
        Item offHandItem = offHand.getItem();

        if (mainHandItem == BCCoreItems.MAP_LOCATION.get()) {
            renderMapLocation(poseStack, matrix, mainHand);
        } else if (mainHandItem == BCCoreItems.MARKER_CONNECTOR.get() || offHandItem == BCCoreItems.MARKER_CONNECTOR.get()) {
            renderMarkerConnector(poseStack, matrix, world, player);
        }

        DetachedRenderer.fromWorldOriginPost(poseStack, matrix);

        mc.getProfiler().pop();
        mc.getProfiler().pop();
    }

    private static void renderMapLocation(PoseStack poseStack, Matrix4f matrix, @Nonnull ItemStack stack) {
        MapLocationType type = MapLocationType.getFromStack(stack);
        if (type == MapLocationType.SPOT) {
            Direction face = ItemMapLocation.getPointFace(stack);
            IBox box = ItemMapLocation.getPointBox(stack);
            if (box != null) {
                Vec3[][] vectors = MAP_LOCATION_POINT[face.ordinal()];
                GL11.glTranslated(box.min().getX(), box.min().getY(), box.min().getZ());
                for (Vec3[] vec : vectors) {
                    LaserData_BC8 laser =
                        new LaserData_BC8(BuildCraftLaserManager.STRIPES_WRITE, vec[0], vec[1], 1 / 16.0);
                    LaserRenderer_BC8.renderLaserStatic(poseStack, matrix, laser);
                }
            }

        } else if (type == MapLocationType.AREA) {

            IBox box = ItemMapLocation.getAreaBox(stack);
            LAST_RENDERED_MAP_LOC.reset();
            LAST_RENDERED_MAP_LOC.initialize(box);
            LaserBoxRenderer.renderLaserBoxStatic(poseStack, matrix, LAST_RENDERED_MAP_LOC, BuildCraftLaserManager.STRIPES_WRITE, true);

        } else if (type == MapLocationType.PATH) {
            List<BlockPos> path = BCCoreItems.MAP_LOCATION.get().getPath(stack);
            if (path != null && path.size() > 1) {
                BlockPos last = null;
                for (BlockPos p : path) {
                    if (last == null) {
                        last = p;
                    }
                }
            }

            // TODO!
        } else if (type == MapLocationType.ZONE) {
            // TODO!
        }
    }

    private static void renderMarkerConnector(PoseStack poseStack, Matrix4f matrix, ClientLevel world, Player player) {
        ProfilerFiller profiler = Minecraft.getInstance().getProfiler();
        profiler.push("marker");
        for (MarkerCache<?> cache : MarkerCache.CACHES) {
            profiler.push(cache.name);
            renderMarkerCache(poseStack, matrix, player, cache.getSubCache(world));
            profiler.pop();
        }
        profiler.pop();
    }

    private static void renderMarkerCache(PoseStack poseStack, Matrix4f matrix, Player player, MarkerSubCache<?> cache) {
        ProfilerFiller profiler = Minecraft.getInstance().getProfiler();
        profiler.push("compute");
        Set<LaserData_BC8> toRender = new HashSet<>();
        for (final BlockPos a : cache.getAllMarkers()) {
            for (final BlockPos b : cache.getValidConnections(a)) {
                if (a.asLong() > b.asLong()) {
                    // Only render each pair once
                    continue;
                }

                Vec3 start = VecUtil.convertCenter(a);
                Vec3 end = VecUtil.convertCenter(b);

                Vec3 startToEnd = end.subtract(start).normalize();
                Vec3 endToStart = start.subtract(end).normalize();
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
        profiler.popPush("render");
        for (LaserData_BC8 laser : toRender) {
            LaserRenderer_BC8.renderLaserStatic(poseStack, matrix, laser);
        }
        profiler.pop();
    }

    private static boolean isLookingAt(BlockPos from, BlockPos to, Player player) {
        return ItemMarkerConnector.doesInteract(from, to, player);
    }
}
