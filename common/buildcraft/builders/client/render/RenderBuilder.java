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
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class RenderBuilder extends TileEntityRenderer<TileBuilder> {
    private static final double OFFSET = 0.1;

    public RenderBuilder(TileEntityRendererDispatcher context) {
        super(context);
    }

    @Override
//    public void renderTileEntityFast(@Nonnull TileBuilder tile, double x, double y, double z, float partialTicks, int destroyStage, float partial, @Nonnull BufferBuilder buffer)
    public void render(TileBuilder tile, float partialTicks, MatrixStack poseStack, IRenderTypeBuffer bufferSource, int combinedLight, int combinedOverlay) {
        Minecraft.getInstance().getProfiler().push("bc");
        Minecraft.getInstance().getProfiler().push("builder");

//        buffer.setTranslation(x - tile.getPos().getX(), y - tile.getPos().getY(), z - tile.getPos().getZ());
        poseStack.pushPose();
        poseStack.translate(-tile.getBlockPos().getX(), -tile.getBlockPos().getY(), -tile.getBlockPos().getZ());

        Minecraft.getInstance().getProfiler().push("box");
        Box box = tile.getBox();
//        IVertexBuilder buffer = bufferSource.getBuffer(Atlases.translucentCullBlockSheet());
        IVertexBuilder buffer = bufferSource.getBuffer(RenderType.solid());
        LaserBoxRenderer.renderLaserBoxDynamic(box, BuildCraftLaserManager.STRIPES_WRITE, poseStack.last(), buffer, true);

        Minecraft.getInstance().getProfiler().popPush("path");

//        buffer = bufferSource.getBuffer(Atlases.translucentCullBlockSheet());
        buffer = bufferSource.getBuffer(RenderType.solid());
        List<BlockPos> path = tile.path;
        if (path != null) {
            BlockPos last = null;
            for (BlockPos p : path) {
                if (last != null) {
//                    Vector3d from = new Vector3d(last).add(VecUtil.VEC_HALF);
                    Vector3d from = Vector3d.atLowerCornerOf(last).add(VecUtil.VEC_HALF);
//                    Vector3d to = new Vector3d(p).add(VecUtil.VEC_HALF);
                    Vector3d to = Vector3d.atLowerCornerOf(p).add(VecUtil.VEC_HALF);
                    Vector3d one = offset(from, to);
                    Vector3d two = offset(to, from);
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
            buffer = bufferSource.getBuffer(Atlases.translucentCullBlockSheet());
//            RenderSnapshotBuilder.render(tile.getBuilder(), tile.getWorld(), tile.getPos(), x, y, z, partialTicks, buffer);
            RenderSnapshotBuilder.render(tile.getBuilder(), tile.getLevel(), tile.getBlockPos(), partialTicks, poseStack, buffer);
        }

        Minecraft.getInstance().getProfiler().pop();
        Minecraft.getInstance().getProfiler().pop();
    }

    private static Vector3d offset(Vector3d from, Vector3d to) {
        Vector3d dir = to.subtract(from).normalize();
        return from.add(VecUtil.scale(dir, OFFSET));
    }

    @Override
//    public boolean isGlobalRenderer(TileBuilder te)
    public boolean shouldRenderOffScreen(TileBuilder tile) {
        return true;
    }
}
