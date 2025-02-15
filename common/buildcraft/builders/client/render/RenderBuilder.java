/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.client.render;

import buildcraft.builders.tile.TileBuilder;
import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.lib.client.render.laser.LaserBoxRenderer;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.client.render.laser.LaserRenderer_BC8;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.misc.data.Box;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class RenderBuilder implements BlockEntityRenderer<TileBuilder> {
    private static final double OFFSET = 0.1;

    public RenderBuilder(BlockEntityRendererProvider.Context context) {
    }

    @Override
//    public void renderTileEntityFast(@Nonnull TileBuilder tile, double x, double y, double z, float partialTicks, int destroyStage, float partial, @Nonnull BufferBuilder buffer)
    public void render(TileBuilder tile, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        Minecraft.getInstance().getProfiler().push("bc");
        Minecraft.getInstance().getProfiler().push("builder");

//        buffer.setTranslation(x - tile.getPos().getX(), y - tile.getPos().getY(), z - tile.getPos().getZ());
        poseStack.pushPose();
        poseStack.translate(-tile.getBlockPos().getX(), -tile.getBlockPos().getY(), -tile.getBlockPos().getZ());

        Minecraft.getInstance().getProfiler().push("box");
        Box box = tile.getBox();
//        VertexConsumer buffer = bufferSource.getBuffer(Sheets.translucentCullBlockSheet());
        VertexConsumer buffer = bufferSource.getBuffer(Sheets.solidBlockSheet());
        LaserBoxRenderer.renderLaserBoxDynamic(box, BuildCraftLaserManager.STRIPES_WRITE, poseStack.last(), buffer, true);

        Minecraft.getInstance().getProfiler().popPush("path");

//        buffer = bufferSource.getBuffer(Sheets.translucentCullBlockSheet());
        buffer = bufferSource.getBuffer(Sheets.solidBlockSheet());
        List<BlockPos> path = tile.path;
        if (path != null) {
            BlockPos last = null;
            for (BlockPos p : path) {
                if (last != null) {
//                    Vec3 from = new Vec3(last).add(VecUtil.VEC_HALF);
                    Vec3 from = Vec3.atLowerCornerOf(last).add(VecUtil.VEC_HALF);
//                    Vec3 to = new Vec3(p).add(VecUtil.VEC_HALF);
                    Vec3 to = Vec3.atLowerCornerOf(p).add(VecUtil.VEC_HALF);
                    Vec3 one = offset(from, to);
                    Vec3 two = offset(to, from);
                    LaserData_BC8 data = new LaserData_BC8(BuildCraftLaserManager.STRIPES_WRITE_DIRECTION, one, two, 1 / 16.1);
                    LaserRenderer_BC8.renderLaserDynamic(data, poseStack.last(), buffer);
                }
                last = p;
            }
        }

        Minecraft.getInstance().getProfiler().pop();

//        buffer.setTranslation(0, 0, 0);
        poseStack.popPose();

        if (tile.getBuilder() != null) {
            buffer = bufferSource.getBuffer(Sheets.translucentCullBlockSheet());
//            RenderSnapshotBuilder.render(tile.getBuilder(), tile.getWorld(), tile.getPos(), x, y, z, partialTicks, buffer);
            RenderSnapshotBuilder.render(tile.getBuilder(), tile.getLevel(), tile.getBlockPos(), partialTicks, poseStack, buffer);
        }

        Minecraft.getInstance().getProfiler().pop();
        Minecraft.getInstance().getProfiler().pop();
    }

    private static Vec3 offset(Vec3 from, Vec3 to) {
        Vec3 dir = to.subtract(from).normalize();
        return from.add(VecUtil.scale(dir, OFFSET));
    }

    @Override
//    public boolean isGlobalRenderer(TileBuilder te)
    public boolean shouldRenderOffScreen(TileBuilder tile) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 512;
    }
}
