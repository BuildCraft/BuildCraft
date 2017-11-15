/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.client.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import buildcraft.api.properties.BuildCraftProperties;

import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.client.render.laser.LaserRenderer_BC8;
import buildcraft.lib.client.render.laser.RenderLaserBox;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.misc.SpriteUtil;
import buildcraft.lib.misc.VecUtil;

import buildcraft.builders.BCBuildersBlocks;
import buildcraft.builders.tile.TileQuarry;
import buildcraft.core.client.BuildCraftLaserManager;

public class RenderQuarry extends TileEntitySpecialRenderer<TileQuarry> {
    public static final LaserData_BC8.LaserType FRAME;
    public static final LaserData_BC8.LaserType FRAME_BOTTOM;
    public static final LaserData_BC8.LaserType DRILL;
    public static final LaserData_BC8.LaserType LASER;

    static {
        {
            SpriteHolderRegistry.SpriteHolder sprite = SpriteHolderRegistry.getHolder("buildcraftbuilders:blocks/frame/default");
            LaserData_BC8.LaserRow capStart = new LaserData_BC8.LaserRow(sprite, 0, 0, 0, 0);
            LaserData_BC8.LaserRow start = null;
            LaserData_BC8.LaserRow[] middle = { new LaserData_BC8.LaserRow(sprite, 0, 4, 16, 12) };
            LaserData_BC8.LaserRow end = new LaserData_BC8.LaserRow(sprite, 0, 4, 16, 12);
            LaserData_BC8.LaserRow capEnd = new LaserData_BC8.LaserRow(sprite, 0, 0, 0, 0);
            FRAME = new LaserData_BC8.LaserType(capStart, start, middle, end, capEnd);
        }
        {
            SpriteHolderRegistry.SpriteHolder sprite = SpriteHolderRegistry.getHolder("buildcraftbuilders:blocks/frame/default");
            LaserData_BC8.LaserRow capStart = new LaserData_BC8.LaserRow(sprite, 0, 0, 0, 0);
            LaserData_BC8.LaserRow start = null;
            LaserData_BC8.LaserRow[] middle = { new LaserData_BC8.LaserRow(sprite, 0, 4, 16, 12) };
            LaserData_BC8.LaserRow end = new LaserData_BC8.LaserRow(sprite, 0, 4, 16, 12);
            LaserData_BC8.LaserRow capEnd = new LaserData_BC8.LaserRow(sprite, 4, 4, 12, 12);
            FRAME_BOTTOM = new LaserData_BC8.LaserType(capStart, start, middle, end, capEnd);
        }
        {
            SpriteHolderRegistry.SpriteHolder sprite = SpriteHolderRegistry.getHolder("buildcraftbuilders:blocks/quarry/drill");
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

    @Override
    public void renderTileEntityAt(TileQuarry tile, double x, double y, double z, float partialTicks, int destroyStage) {
        Profiler profiler = Minecraft.getMinecraft().mcProfiler;
        profiler.startSection("bc");
        profiler.startSection("quarry");
        profiler.startSection("setup");

        SpriteUtil.bindBlockTextureMap();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableBlend();
        GlStateManager.enableCull();

        if (Minecraft.isAmbientOcclusionEnabled()) {
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
        } else {
            GlStateManager.shadeModel(GL11.GL_FLAT);
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(x - tile.getPos().getX(), y - tile.getPos().getY(), z - tile.getPos().getZ());

        final BlockPos min = tile.frameBox.min();
        final BlockPos max = tile.frameBox.max();


        profiler.endSection();
        if (tile.frameBox.isInitialized()) {
            double yOffset = 1;

            profiler.startSection("laser");
            if (tile.currentTask != null && tile.currentTask instanceof TileQuarry.TaskBreakBlock) {
                TileQuarry.TaskBreakBlock taskBreakBlock = (TileQuarry.TaskBreakBlock) tile.currentTask;
                BlockPos pos = taskBreakBlock.breakPos;

                if (tile.drillPos == null) {
                    if (taskBreakBlock.clientPower != 0) {
                        // Don't render a laser before we have any power
                        Vec3d from = VecUtil.convertCenter(tile.getPos());
                        Vec3d to = VecUtil.convertCenter(pos);
                        LaserData_BC8 laser = new LaserData_BC8(LASER, from, to, 1 / 16.0);
                        LaserRenderer_BC8.renderLaserStatic(laser);
                    }
                } else {
                    long power = (long) (
                        taskBreakBlock.prevClientPower +
                            (taskBreakBlock.clientPower - taskBreakBlock.prevClientPower) * (double) partialTicks
                    );
                    yOffset = (double) power / taskBreakBlock.getTarget();
                    if (yOffset < 0.9) {
                        yOffset = 1 - yOffset / 0.9;
                    } else {
                        yOffset = (yOffset - 0.9) / 0.1;
                    }
                }
            }

            yOffset += 4 / 16D;

            profiler.endStartSection("frame");
            if (tile.clientDrillPos != null && tile.prevClientDrillPos != null) {
                Vec3d interpolatedPos = tile.prevClientDrillPos.add(tile.clientDrillPos.subtract(tile.prevClientDrillPos).scale(partialTicks));

                LaserRenderer_BC8.renderLaserStatic(new LaserData_BC8(FRAME,//
                        new Vec3d(interpolatedPos.xCoord + 0.5, max.getY() + 0.5, interpolatedPos.zCoord),//
                        new Vec3d(interpolatedPos.xCoord + 0.5, max.getY() + 0.5, max.getZ() + 12 / 16D),//
                        1 / 16D, true, true, 0));
                LaserRenderer_BC8.renderLaserStatic(new LaserData_BC8(FRAME,//
                        new Vec3d(interpolatedPos.xCoord + 0.5, max.getY() + 0.5, interpolatedPos.zCoord),//
                        new Vec3d(interpolatedPos.xCoord + 0.5, max.getY() + 0.5, min.getZ() + 4 / 16D),//
                        1 / 16D, true, true, 0));
                LaserRenderer_BC8.renderLaserStatic(new LaserData_BC8(FRAME,//
                        new Vec3d(interpolatedPos.xCoord, max.getY() + 0.5, interpolatedPos.zCoord + 0.5),//
                        new Vec3d(max.getX() + 12 / 16D, max.getY() + 0.5, interpolatedPos.zCoord + 0.5),//
                        1 / 16D, true, true, 0));
                LaserRenderer_BC8.renderLaserStatic(new LaserData_BC8(FRAME,//
                        new Vec3d(interpolatedPos.xCoord, max.getY() + 0.5, interpolatedPos.zCoord + 0.5),//
                        new Vec3d(min.getX() + 4 / 16D, max.getY() + 0.5, interpolatedPos.zCoord + 0.5),//
                        1 / 16D, true, true, 0));
                LaserRenderer_BC8.renderLaserStatic(new LaserData_BC8(FRAME_BOTTOM,//
                        new Vec3d(interpolatedPos.xCoord + 0.5, interpolatedPos.yCoord + 1 + 4 / 16D, interpolatedPos.zCoord + 0.5),//
                        new Vec3d(interpolatedPos.xCoord + 0.5, max.getY() + 0.5, interpolatedPos.zCoord + 0.5),//
                        1 / 16D, true, true, 0));
                LaserRenderer_BC8.renderLaserStatic(new LaserData_BC8(DRILL,//
                        new Vec3d(interpolatedPos.xCoord + 0.5, interpolatedPos.yCoord + 1 + yOffset, interpolatedPos.zCoord + 0.5),//
                        new Vec3d(interpolatedPos.xCoord + 0.5, interpolatedPos.yCoord + yOffset, interpolatedPos.zCoord + 0.5),//
                        1 / 16D, true, true, 0));
            } else {
                RenderLaserBox.renderStatic(tile.frameBox, BuildCraftLaserManager.STRIPES_WRITE, true);
            }
            profiler.endSection();
        }

        GlStateManager.popMatrix();
        profiler.startSection("items");

        if (tile.frameBox.isInitialized() && false) {
            TileQuarry.TaskAddFrame currentTask = (TileQuarry.TaskAddFrame) tile.currentTask;
            int index = tile.framePoses.indexOf(currentTask.framePos);
            if (index > 1) {
                double progress = (double) currentTask.getPower() / currentTask.getTarget() * (index - 1) / tile.framePoses.size();
                double progress1 = (progress >= 0 && progress <= 0.25) ? progress * 4 ://
                    (progress >= 0.25 && progress <= 0.5) ? 1 ://
                        (progress >= 0.5 && progress <= 0.75) ? 1 - (progress - 0.5) * 4 ://
                            (progress >= 0.75 && progress <= 1) ? 0 : -1 /* not possible */;
                double progress2 = (progress >= 0 && progress <= 0.25) ? 1 : (progress >= 0.25 && progress <= 0.5) ? 1 - (progress - 0.25) * 4 : (progress >= 0.5 && progress <= 0.75) ? 0 : (progress >= 0.75 && progress <= 1) ? (progress - 0.75) * 4
                    : -1 /* not possible */;
                double xProgress = -1;
                double zProgress = -1;
                EnumFacing side = tile.getWorld().getBlockState(tile.getPos()).getValue(BuildCraftProperties.BLOCK_FACING).getOpposite();
                BlockPos firstPos = tile.getPos().offset(side);
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
                ItemStack stack = new ItemStack(BCBuildersBlocks.frame);

                RenderHelper.disableStandardItemLighting();
                GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
                GlStateManager.disableTexture2D();
                GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
                GlStateManager.pushMatrix();
                GlStateManager.translate(x - tile.getPos().getX(), y - tile.getPos().getY(), z - tile.getPos().getZ());
                GlStateManager.pushMatrix();
                GlStateManager.translate(xResult + 0.5, tile.getPos().getY(), zResult + 0.5);
                GlStateManager.scale(3, 3, 3);
                Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.GROUND);
                GlStateManager.popMatrix();
                GlStateManager.popMatrix();
            }
        }
        RenderHelper.enableStandardItemLighting();

        profiler.endSection();
        profiler.endSection();
        profiler.endSection();
    }

    @Override
    public boolean isGlobalRenderer(TileQuarry tile) {
        return true;
    }
}
