/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.render;

import java.util.Arrays;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.transport.pipe.IPipeFlowRenderer;
import buildcraft.api.transport.pipe.IPipeHolder;

import buildcraft.lib.client.render.fluid.FluidRenderer;
import buildcraft.lib.client.render.fluid.FluidSpriteType;
import buildcraft.lib.misc.VecUtil;

import buildcraft.transport.pipe.Pipe;
import buildcraft.transport.pipe.flow.PipeFlowFluids;

public enum PipeFlowRendererFluids implements IPipeFlowRenderer<PipeFlowFluids> {
    INSTANCE;

    @Override
    public void render(PipeFlowFluids flow, double x, double y, double z, float partialTicks, VertexBuffer vb) {
        FluidStack forRender = flow.getFluidStackForRender();
        if (forRender == null) {
            return;
        }

        Profiler prof = Minecraft.getMinecraft().mcProfiler;
        prof.startSection("calc");

        boolean[] sides = new boolean[6];
        Arrays.fill(sides, true);

        double[] amounts = flow.getAmountsForRender(partialTicks);
        Vec3d[] offsets = flow.getOffsetsForRender(partialTicks);

        int blocklight = forRender.getFluid().getLuminosity(forRender);
        IPipeHolder holder = flow.pipe.getHolder();
        int combinedLight = holder.getPipeWorld().getCombinedLight(holder.getPipePos(), blocklight);

        FluidRenderer.vertex.lighti(combinedLight);

        VertexBuffer fluidBuffer = Tessellator.getInstance().getBuffer();
        fluidBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        fluidBuffer.setTranslation(x, y, z);

        boolean gas = forRender.getFluid().isGaseous(forRender);
        boolean horizontal = false;
        boolean vertical = flow.pipe.isConnected(gas ? EnumFacing.DOWN : EnumFacing.UP);

        prof.endStartSection("build");
        for (EnumFacing face : EnumFacing.VALUES) {
            double size = ((Pipe) flow.pipe).getConnectedDist(face);
            double amount = amounts[face.getIndex()];
            if (face.getAxis() != Axis.Y) {
                horizontal |= flow.pipe.isConnected(face) && amount > 0;
            }

            Vec3d center = VecUtil.offset(new Vec3d(0.5, 0.5, 0.5), face, 0.245 + size / 2);
            Vec3d radius = new Vec3d(0.24, 0.24, 0.24);
            radius = VecUtil.replaceValue(radius, face.getAxis(), 0.005 + size / 2);

            if (face.getAxis() == Axis.Y) {
                double perc = amount / flow.capacity;
                perc = Math.sqrt(perc);
                radius = new Vec3d(perc * 0.24, radius.yCoord, perc * 0.24);
            }

            Vec3d offset = offsets[face.getIndex()];
            if (offset == null) offset = Vec3d.ZERO;
            center = center.add(offset);
            fluidBuffer.setTranslation(x - offset.xCoord, y - offset.yCoord, z - offset.zCoord);

            Vec3d min = center.subtract(radius);
            Vec3d max = center.add(radius);

            if (face.getAxis() == Axis.Y) {
                FluidRenderer.renderFluid(FluidSpriteType.FROZEN, forRender, 1, 1, min, max, fluidBuffer, sides);
            } else {
                FluidRenderer.renderFluid(FluidSpriteType.FROZEN, forRender, amount, flow.capacity, min, max, fluidBuffer, sides);
            }
        }

        double amount = amounts[EnumPipePart.CENTER.getIndex()];

        double horizPos = 0.26;

        Vec3d offset = offsets[EnumPipePart.CENTER.getIndex()];
        if (offset == null) offset = Vec3d.ZERO;
        fluidBuffer.setTranslation(x - offset.xCoord, y - offset.yCoord, z - offset.zCoord);

        if (horizontal | !vertical) {
            Vec3d min = new Vec3d(0.26, 0.26, 0.26);
            Vec3d max = new Vec3d(0.74, 0.74, 0.74);

            min = min.add(offset);
            max = max.add(offset);

            FluidRenderer.renderFluid(FluidSpriteType.FROZEN, forRender, amount, flow.capacity, min, max, fluidBuffer, sides);
            horizPos += (max.yCoord - min.yCoord) * amount / flow.capacity;
        }

        if (vertical && horizPos < 0.74) {
            double perc = amount / flow.capacity;
            perc = Math.sqrt(perc);
            double minXZ = 0.5 - 0.24 * perc;
            double maxXZ = 0.5 + 0.24 * perc;
            
            double yMin = gas ? 0.26 : horizPos;
            double yMax = gas ? 1 - horizPos : 0.74;

            Vec3d min = new Vec3d(minXZ, yMin, minXZ);
            Vec3d max = new Vec3d(maxXZ, yMax, maxXZ);
            min = min.add(offset);
            max = max.add(offset);

            FluidRenderer.renderFluid(FluidSpriteType.FROZEN, forRender, 1, 1, min, max, fluidBuffer, sides);
        }

        // gl state setup
        RenderHelper.disableStandardItemLighting();
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableCull();

        prof.endStartSection("draw");
        fluidBuffer.setTranslation(0, 0, 0);
        Tessellator.getInstance().draw();

        RenderHelper.enableStandardItemLighting();

        FluidRenderer.vertex.lighti(0xF, 0xF);
        prof.endSection();

    }

    private static void drawFluidCenter(FluidStack fluid, double percentage, boolean horizontal, boolean above, VertexBuffer vb) {
        boolean[] sides = new boolean[6];
        Arrays.fill(sides, true);
        Vec3d min = new Vec3d(0.26, 0.26, 0.26);
        Vec3d max = new Vec3d(0.74, 0.74, 0.74);
        FluidRenderer.renderFluid(FluidSpriteType.STILL, fluid, percentage, 1, min, max, vb, sides);
    }
}
