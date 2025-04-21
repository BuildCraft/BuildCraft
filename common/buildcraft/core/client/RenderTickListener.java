/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.client;

import buildcraft.api.core.IBox;
import buildcraft.api.items.IMapLocation.MapLocationType;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.core.BCCoreItems;
import buildcraft.core.item.ItemMapLocation;
import buildcraft.core.item.ItemMarkerConnector;
import buildcraft.lib.client.render.DetachedRenderer;
import buildcraft.lib.client.render.laser.LaserBoxRenderer;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.client.render.laser.LaserData_BC8.LaserType;
import buildcraft.lib.client.render.laser.LaserRenderer_BC8;
import buildcraft.lib.debug.ClientDebuggables;
import buildcraft.lib.marker.MarkerCache;
import buildcraft.lib.marker.MarkerSubCache;
import buildcraft.lib.misc.MatrixUtil;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.misc.data.Box;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RenderTickListener {
    private static final Vector3d[][][] MAP_LOCATION_POINT = new Vector3d[6][][];
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
            Vector3d[][] arr = new Vector3d[5][2];
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 2; j++) {
                    double[] from = upFace[i][j];
                    Point3f point = new Point3f(new Point3d(from));
                    matrix.transform(point);
                    Vector3d to = new Vector3d(point.x, point.y, point.z);
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
        Minecraft mc = Minecraft.getInstance();
        IDebuggable debuggable = ClientDebuggables.getDebuggableObject(mc.hitResult);
        if (debuggable != null) {
//            List<String> clientLeft = new ArrayList<>();
//            List<String> clientRight = new ArrayList<>();
            List<ITextComponent> clientLeft = new ArrayList<>();
            List<ITextComponent> clientRight = new ArrayList<>();
//            debuggable.getDebugInfo(clientLeft, clientRight, mc.hitResult.sideHit);
            if (mc.hitResult instanceof BlockRayTraceResult) {
                BlockRayTraceResult blockHitResult = (BlockRayTraceResult) mc.hitResult;
                debuggable.getDebugInfo(clientLeft, clientRight, blockHitResult.getDirection());
            } else {
                debuggable.getDebugInfo(clientLeft, clientRight, null);
            }
            String headerFirst = DIFF_HEADER_FORMATTING + "SERVER:";
            String headerSecond = DIFF_HEADER_FORMATTING + "CLIENT:";
            appendDiff(event.getLeft(), ClientDebuggables.SERVER_LEFT, clientLeft, headerFirst, headerSecond);
            appendDiff(event.getRight(), ClientDebuggables.SERVER_RIGHT, clientRight, headerFirst, headerSecond);
//            debuggable.getClientDebugInfo(event.getLeft(), event.getRight(), mc.objectMouseOver.sideHit);
            if (mc.hitResult instanceof BlockRayTraceResult) {
                BlockRayTraceResult blockHitResult = (BlockRayTraceResult) mc.hitResult;
                debuggable.getClientDebugInfo(event.getLeft(), event.getRight(), blockHitResult.getDirection());
            } else {
                debuggable.getClientDebugInfo(event.getLeft(), event.getRight(), null);
            }
        }
    }

    // private static void appendDiff(List<String> dest, List<String> first, List<String> second, String headerFirst, String headerSecond)
    private static void appendDiff(List<String> dest, List<ITextComponent> first, List<ITextComponent> second, String headerFirst, String headerSecond) {
        dest.add("");
        dest.add(headerFirst);
//        dest.addAll(first);
        dest.addAll(Lists.newArrayList(first.stream().map(ITextComponent::getString).toArray(String[]::new)));
        dest.add("");
        dest.add(headerSecond);
        if (first.size() != second.size()) {
            // no diffing
//            dest.addAll(second);
            dest.addAll(Lists.newArrayList(second.stream().map(ITextComponent::getString).toArray(String[]::new)));
        } else {
            for (int l = 0; l < first.size(); l++) {
//                String shownLine = first.get(l);
                String shownLine = first.get(l).getString();
//                String diffLine = second.get(l);
                String diffLine = second.get(l).getString();
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
        float partialTicks = Minecraft.getInstance().getFrameTime();
        ActiveRenderInfo camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        renderHeldItemInWorld(partialTicks, event.getMatrixStack(), camera);
    }

    private static void renderHeldItemInWorld(float partialTicks, MatrixStack poseStack, ActiveRenderInfo camera) {
        Minecraft mc = Minecraft.getInstance();
        PlayerEntity player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        ItemStack mainHand = StackUtil.asNonNull(player.getMainHandItem());
        ItemStack offHand = StackUtil.asNonNull(player.getOffhandItem());
        ClientWorld world = mc.level;

        mc.getProfiler().push("bc");
        mc.getProfiler().push("renderWorld");

        // Calen: push and translate by camera
        DetachedRenderer.fromWorldOriginPre(player, partialTicks, poseStack, camera);

        Item mainHandItem = mainHand.getItem();
        Item offHandItem = offHand.getItem();

        if (mainHandItem == BCCoreItems.mapLocation.get()) {
            renderMapLocation(mainHand, poseStack);
        } else if (mainHandItem == BCCoreItems.markerConnector.get() || offHandItem == BCCoreItems.markerConnector.get()) {
            renderMarkerConnector(world, player, poseStack);
        }

        // Calen: pop
        DetachedRenderer.fromWorldOriginPost(poseStack);

        Minecraft.getInstance().renderBuffers().bufferSource().endBatch();

        mc.getProfiler().pop();
        mc.getProfiler().pop();
    }

    private static void renderMapLocation(@Nonnull ItemStack stack, MatrixStack poseStack) {
        MapLocationType type = MapLocationType.getFromStack(stack);
        if (type == MapLocationType.SPOT) {
            Direction face = ItemMapLocation.getPointFace(stack);
            IBox box = ItemMapLocation.getPointBox(stack);
            if (box != null) {
                Vector3d[][] vectors = MAP_LOCATION_POINT[face.ordinal()];
//                GL11.glTranslated(box.min().getX(), box.min().getY(), box.min().getZ());
                poseStack.pushPose();
                poseStack.translate(box.min().getX(), box.min().getY(), box.min().getZ());
                for (Vector3d[] vec : vectors) {
                    LaserData_BC8 laser =
                            new LaserData_BC8(BuildCraftLaserManager.STRIPES_WRITE, vec[0], vec[1], 1 / 16.0);
//                    LaserRenderer_BC8.renderLaserStatic(laser);
                    LaserRenderer_BC8.renderLaserStatic(laser, poseStack.last());
                }
                poseStack.popPose();
            }

        } else if (type == MapLocationType.AREA) {

            IBox box = ItemMapLocation.getAreaBox(stack);
            LAST_RENDERED_MAP_LOC.reset();
            LAST_RENDERED_MAP_LOC.initialize(box);
//            LaserBoxRenderer.renderLaserBoxStatic(LAST_RENDERED_MAP_LOC, BuildCraftLaserManager.STRIPES_WRITE, true);
            LaserBoxRenderer.renderLaserBoxStatic(LAST_RENDERED_MAP_LOC, BuildCraftLaserManager.STRIPES_WRITE, poseStack.last(), true);

        } else if (type == MapLocationType.PATH) {
            List<BlockPos> path = BCCoreItems.mapLocation.get().getPath(stack);
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

    private static void renderMarkerConnector(ClientWorld world, PlayerEntity player, MatrixStack poseStack) {
        IProfiler profiler = Minecraft.getInstance().getProfiler();
        profiler.push("marker");
        for (MarkerCache<?> cache : MarkerCache.CACHES) {
            profiler.push(cache.name);
            renderMarkerCache(player, cache.getSubCache(world), poseStack);
            profiler.pop();
        }
        profiler.pop();
    }

    private static void renderMarkerCache(PlayerEntity player, MarkerSubCache<?> cache, MatrixStack poseStack) {
        IProfiler profiler = Minecraft.getInstance().getProfiler();
        profiler.push("compute");
        Set<LaserData_BC8> toRender = new HashSet<>();
        for (final BlockPos a : cache.getAllMarkers()) {
            for (final BlockPos b : cache.getValidConnections(a)) {
                if (a.asLong() > b.asLong()) {
                    // Only render each pair once
                    continue;
                }

                Vector3d start = VecUtil.convertCenter(a);
                Vector3d end = VecUtil.convertCenter(b);

                Vector3d startToEnd = end.subtract(start).normalize();
                Vector3d endToStart = start.subtract(end).normalize();
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
//            LaserRenderer_BC8.renderLaserStatic(laser);
            LaserRenderer_BC8.renderLaserStatic(laser, poseStack.last());
        }
        profiler.pop();
    }

    private static boolean isLookingAt(BlockPos from, BlockPos to, PlayerEntity player) {
        return ItemMarkerConnector.doesInteract(from, to, player);
    }
}
