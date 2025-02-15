/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.client.render;

import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.builders.BCBuildersBlocks;
import buildcraft.builders.tile.TileQuarry;
import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.lib.client.render.laser.LaserBoxRenderer;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.client.render.laser.LaserRenderer_BC8;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.misc.VecUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderQuarry implements BlockEntityRenderer<TileQuarry> {
    public static final LaserData_BC8.LaserType FRAME;
    public static final LaserData_BC8.LaserType FRAME_BOTTOM;
    public static final LaserData_BC8.LaserType DRILL;
    public static final LaserData_BC8.LaserType LASER;

    static {
        {
            SpriteHolderRegistry.SpriteHolder sprite = SpriteHolderRegistry.getHolder("buildcraftbuilders:block/frame/default");
            LaserData_BC8.LaserRow capStart = new LaserData_BC8.LaserRow(sprite, 0, 0, 0, 0);
            LaserData_BC8.LaserRow start = null;
            LaserData_BC8.LaserRow[] middle = { new LaserData_BC8.LaserRow(sprite, 0, 4, 16, 12) };
            LaserData_BC8.LaserRow end = new LaserData_BC8.LaserRow(sprite, 0, 4, 16, 12);
            LaserData_BC8.LaserRow capEnd = new LaserData_BC8.LaserRow(sprite, 0, 0, 0, 0);
            FRAME = new LaserData_BC8.LaserType(capStart, start, middle, end, capEnd);
        }
        {
            SpriteHolderRegistry.SpriteHolder sprite = SpriteHolderRegistry.getHolder("buildcraftbuilders:block/frame/default");
            LaserData_BC8.LaserRow capStart = new LaserData_BC8.LaserRow(sprite, 0, 0, 0, 0);
            LaserData_BC8.LaserRow start = null;
            LaserData_BC8.LaserRow[] middle = { new LaserData_BC8.LaserRow(sprite, 0, 4, 16, 12) };
            LaserData_BC8.LaserRow end = new LaserData_BC8.LaserRow(sprite, 0, 4, 16, 12);
            LaserData_BC8.LaserRow capEnd = new LaserData_BC8.LaserRow(sprite, 4, 4, 12, 12);
            FRAME_BOTTOM = new LaserData_BC8.LaserType(capStart, start, middle, end, capEnd);
        }
        {
            SpriteHolderRegistry.SpriteHolder sprite = SpriteHolderRegistry.getHolder("buildcraftbuilders:block/quarry/drill");
            LaserData_BC8.LaserRow capStart = new LaserData_BC8.LaserRow(sprite, 6, 0, 10, 4);
            LaserData_BC8.LaserRow start = null;
            LaserData_BC8.LaserRow[] middle = { new LaserData_BC8.LaserRow(sprite, 0, 0, 16, 4) };
            LaserData_BC8.LaserRow end = null;
            LaserData_BC8.LaserRow capEnd = new LaserData_BC8.LaserRow(sprite, 6, 0, 10, 4);
            DRILL = new LaserData_BC8.LaserType(capStart, start, middle, end, capEnd);
        }
        {
            LASER = BuildCraftLaserManager.POWER_LOW;
        }
    }

    public RenderQuarry(BlockEntityRendererProvider.Context context) {
    }

    @Override
//    public void render(TileQuarry tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
    public void render(TileQuarry tile, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        ProfilerFiller profiler = Minecraft.getInstance().getProfiler();
        profiler.push("bc");
        profiler.push("quarry");
        profiler.push("setup");

//        SpriteUtil.bindBlockTextureMap();
////        RenderHelper.disableStandardItemLighting();
////        GlStateManager.enableBlend();
//        RenderUtil.enableBlend();
////        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

////        if (Minecraft.isAmbientOcclusionEnabled())
//        if (Minecraft.useAmbientOcclusion()) {
////            GlStateManager.shadeModel(GL11.GL_SMOOTH);
//            RenderSystem.setShader(GameRenderer::getRendertypeEntitySmoothCutoutShader);
//        } else {
////            GlStateManager.shadeModel(GL11.GL_FLAT);
//            RenderSystem.setShader(GameRenderer::getBlockShader);
//        }

//        GlStateManager.pushMatrix();
        poseStack.pushPose();
//        GlStateManager.translate(x - tile.getPos().getX(), y - tile.getPos().getY(), z - tile.getPos().getZ());
        poseStack.translate(-tile.getBlockPos().getX(), -tile.getBlockPos().getY(), -tile.getBlockPos().getZ());

        final BlockPos min = tile.frameBox.min();
        final BlockPos max = tile.frameBox.max();


        profiler.pop();
        if (tile.frameBox.isInitialized()) {
            double yOffset = 1 + 4 / 16D;

            profiler.push("laser");
            if (tile.currentTask != null && tile.currentTask instanceof TileQuarry.TaskBreakBlock taskBreakBlock) {
                BlockPos pos = taskBreakBlock.breakPos;

                if (tile.drillPos == null) {
                    if (taskBreakBlock.clientPower != 0) {
                        // Don't render a laser before we have any power
                        Vec3 from = VecUtil.convertCenter(tile.getBlockPos());
                        Vec3 to = VecUtil.convertCenter(pos);
                        LaserData_BC8 laser = new LaserData_BC8(LASER, from, to, 1 / 16.0);
//                        LaserRenderer_BC8.renderLaserStatic(laser);
                        LaserRenderer_BC8.renderLaserStatic(laser, poseStack.last());
                    }
                } else {
                    long power = (long) (
                            taskBreakBlock.prevClientPower +
                                    (taskBreakBlock.clientPower - taskBreakBlock.prevClientPower) * (double) partialTicks
                    );
//                    AxisAlignedBB aabb = tile.getLevel().getBlockState(pos).getBoundingBox(tile.getLevel(), pos);
                    VoxelShape shape = tile.getLevel().getBlockState(pos).getCollisionShape(tile.getLevel(), pos); // Calen: if shape is empty, .bounds() will cause UnsupportedOperationException (ArrayVoxelShape:28)
                    AABB aabb = shape.isEmpty() ? new AABB(0, 0, 0, 0, 0, 0) : shape.bounds();
                    double value = (double) power / taskBreakBlock.getTarget();
                    if (value < 0.9) {
                        value = 1 - value / 0.9;
                    } else {
                        value = (value - 0.9) / 0.1;
                    }
                    double scaleMin = 1 - (1 - aabb.maxY) - (aabb.maxY - aabb.minY) / 2;
                    double scaleMax = 1 + 4 / 16D;
                    yOffset = scaleMin + value * (scaleMax - scaleMin);
                }
            }

            profiler.popPush("frame");
            if (tile.clientDrillPos != null && tile.prevClientDrillPos != null) {
                Vec3 interpolatedPos = tile.prevClientDrillPos.add(tile.clientDrillPos.subtract(tile.prevClientDrillPos).scale(partialTicks));

                LaserRenderer_BC8.renderLaserStatic(new LaserData_BC8(FRAME,//
                                new Vec3(interpolatedPos.x + 0.5, max.getY() + 0.5, interpolatedPos.z),//
                                new Vec3(interpolatedPos.x + 0.5, max.getY() + 0.5, max.getZ() + 12 / 16D),//
//                                1 / 16D, true, true, 0),
                                1 / 16D, true, 0),
                        poseStack.last()
                );
                LaserRenderer_BC8.renderLaserStatic(new LaserData_BC8(FRAME,//
                                new Vec3(interpolatedPos.x + 0.5, max.getY() + 0.5, interpolatedPos.z),//
                                new Vec3(interpolatedPos.x + 0.5, max.getY() + 0.5, min.getZ() + 4 / 16D),//
//                                1 / 16D, true, true, 0),
                                1 / 16D, true, 0),
                        poseStack.last()
                );
                LaserRenderer_BC8.renderLaserStatic(new LaserData_BC8(FRAME,//
                                new Vec3(interpolatedPos.x, max.getY() + 0.5, interpolatedPos.z + 0.5),//
                                new Vec3(max.getX() + 12 / 16D, max.getY() + 0.5, interpolatedPos.z + 0.5),//
//                                1 / 16D, true, true, 0),
                                1 / 16D, true, 0),
                        poseStack.last()
                );
                LaserRenderer_BC8.renderLaserStatic(new LaserData_BC8(FRAME,//
                                new Vec3(interpolatedPos.x, max.getY() + 0.5, interpolatedPos.z + 0.5),//
                                new Vec3(min.getX() + 4 / 16D, max.getY() + 0.5, interpolatedPos.z + 0.5),//
//                                1 / 16D, true, true, 0),
                                1 / 16D, true, 0),
                        poseStack.last()
                );
                LaserRenderer_BC8.renderLaserStatic(new LaserData_BC8(FRAME_BOTTOM,//
                                new Vec3(interpolatedPos.x + 0.5, interpolatedPos.y + 1 + 4 / 16D, interpolatedPos.z + 0.5),//
                                new Vec3(interpolatedPos.x + 0.5, max.getY() + 0.5, interpolatedPos.z + 0.5),//
//                                1 / 16D, true, true, 0),
                                1 / 16D, true, 0),
                        poseStack.last()
                );
                LaserRenderer_BC8.renderLaserStatic(new LaserData_BC8(DRILL,//
                                new Vec3(interpolatedPos.x + 0.5, interpolatedPos.y + 1 + yOffset, interpolatedPos.z + 0.5),//
                                new Vec3(interpolatedPos.x + 0.5, interpolatedPos.y + yOffset, interpolatedPos.z + 0.5),//
//                                1 / 16D, true, true, 0),
                                1 / 16D, true, 0),
                        poseStack.last()
                );
            } else {
                LaserBoxRenderer.renderLaserBoxStatic(tile.frameBox, BuildCraftLaserManager.STRIPES_WRITE, poseStack.last(), true);
            }
            profiler.pop();
        }

//        GlStateManager.popMatrix();
        poseStack.popPose();
        profiler.push("items");

        if (tile.frameBox.isInitialized() && false) {
            TileQuarry.TaskAddFrame currentTask = (TileQuarry.TaskAddFrame) tile.currentTask;
            int index = tile.framePoses.indexOf(currentTask.framePos);
            if (index > 1) {
                double progress = (double) currentTask.power / currentTask.getTarget() * (index - 1) / tile.framePoses.size();
                double progress1 = (progress >= 0 && progress <= 0.25) ? progress * 4 ://
                        (progress >= 0.25 && progress <= 0.5) ? 1 ://
                                (progress >= 0.5 && progress <= 0.75) ? 1 - (progress - 0.5) * 4 ://
                                        (progress >= 0.75 && progress <= 1) ? 0 : -1 /* not possible */;
                double progress2 = (progress >= 0 && progress <= 0.25) ? 1 : (progress >= 0.25 && progress <= 0.5) ? 1 - (progress - 0.25) * 4 : (progress >= 0.5 && progress <= 0.75) ? 0 : (progress >= 0.75 && progress <= 1) ? (progress - 0.75) * 4
                        : -1 /* not possible */;
                double xProgress = -1;
                double zProgress = -1;
                Direction side = tile.getLevel().getBlockState(tile.getBlockPos()).getValue(BuildCraftProperties.BLOCK_FACING).getOpposite();
                BlockPos firstPos = tile.getBlockPos().relative(side);
                switch (side) {
                    case SOUTH:
                        if (firstPos.getX() == min.getX()) {
                            xProgress = 1 - progress2;
                            zProgress = progress1;
                        } else {
                            xProgress = progress2;
                            zProgress = progress1;
                        }
                        break;
                    case WEST:
                        if (firstPos.getZ() == min.getZ()) {
                            xProgress = 1 - progress1;
                            zProgress = 1 - progress2;
                        } else {
                            xProgress = 1 - progress1;
                            zProgress = progress2;
                        }
                        break;
                    case NORTH:
                        if (firstPos.getX() == min.getX()) {
                            xProgress = 1 - progress2;
                            zProgress = 1 - progress1;
                        } else {
                            xProgress = progress2;
                            zProgress = 1 - progress1;
                        }
                        break;
                    case EAST:
                        if (firstPos.getZ() == min.getZ()) {
                            xProgress = progress1;
                            zProgress = 1 - progress2;
                        } else {
                            xProgress = progress1;
                            zProgress = progress2;
                        }
                        break;
                }
                double xResult = min.getX() + (max.getX() - min.getX()) * xProgress;
                double zResult = min.getZ() + (max.getZ() - min.getZ()) * zProgress;
                ItemStack stack = new ItemStack(BCBuildersBlocks.frame.get());

//                RenderHelper.disableStandardItemLighting();
//                GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
//                RenderSystem.activeTexture(33985);
//                GlStateManager.disableTexture2D();
//                GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
//                RenderSystem.activeTexture(OpenGlHelper.defaultTexUnit);
//                RenderSystem.activeTexture(33984);
//                GlStateManager.pushMatrix();
                poseStack.pushPose();
//                GlStateManager.translate(x - tile.getBlockPos().getX(), y - tile.getBlockPos().getY(), z - tile.getBlockPos().getZ());
                poseStack.translate(-tile.getBlockPos().getX(), -tile.getBlockPos().getY(), -tile.getBlockPos().getZ());
//                GlStateManager.pushMatrix();
                poseStack.pushPose();
//                GlStateManager.translate(xResult + 0.5, tile.getBlockPos().getY(), zResult + 0.5);
                poseStack.translate(xResult + 0.5, tile.getBlockPos().getY(), zResult + 0.5);
//                GlStateManager.scale(3, 3, 3);
                poseStack.scale(3, 3, 3);
//                Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.GROUND);
                Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.GROUND, combinedLight, combinedOverlay, poseStack, bufferSource, tile.getLevel(), (int) tile.getBlockPos().asLong());

//                GlStateManager.popMatrix();
                poseStack.popPose();
//                GlStateManager.popMatrix();
                poseStack.popPose();
            }
        }
//        RenderHelper.enableStandardItemLighting();

        profiler.pop();
        profiler.pop();
        profiler.pop();
    }

    @Override
//    public boolean isGlobalRenderer(TileQuarry te)
    public boolean shouldRenderOffScreen(TileQuarry tile) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 512;
    }

    public static void init() {

    }
}
