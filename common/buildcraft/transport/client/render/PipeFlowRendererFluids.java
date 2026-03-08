/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.render;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.transport.pipe.IPipeFlowRenderer;
import buildcraft.lib.client.render.fluid.FluidRenderer;
import buildcraft.lib.client.render.fluid.FluidSpriteType;
import buildcraft.lib.misc.RenderUtil;
import buildcraft.lib.misc.VecUtil;
import buildcraft.transport.pipe.Pipe;
import buildcraft.transport.pipe.flow.PipeFlowFluids;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.matrix.MatrixStack.Entry;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

import java.util.Arrays;

@OnlyIn(Dist.CLIENT)
public enum PipeFlowRendererFluids implements IPipeFlowRenderer<PipeFlowFluids> {
    INSTANCE;

    @Override
//    public void render(PipeFlowFluids flow, double x, double y, double z, float partialTicks, BufferBuilder vb)
    public void render(PipeFlowFluids flow, float partialTicks, MatrixStack poseStack, IVertexBuilder fluidBuffer, int combinedLight, int combinedOverlay) {
        FluidStack forRender = flow.getFluidStackForRender();
        if (forRender == null) {
            return;
        }

        IProfiler prof = Minecraft.getInstance().getProfiler();
        prof.push("calc");

        boolean[] sides = new boolean[6];
        Arrays.fill(sides, true);

        double[] amounts = flow.getAmountsForRender(partialTicks);
        Vector3d[] offsets = flow.getOffsetsForRender(partialTicks);

        int blocklight = forRender.getRawFluid().getAttributes().getLuminosity(forRender);
//        IPipeHolder holder = flow.pipe.getHolder();
//        combinedLight = holder.getPipeWorld().getCombinedLight(holder.getPipePos(), blocklight);
        combinedLight = RenderUtil.combineWithFluidLight(combinedLight, (byte) blocklight);

        FluidRenderer.vertex.lighti(combinedLight);
        FluidRenderer.vertex.overlay(combinedOverlay);

//        try (AutoTessellator tess = RenderUtil.getThreadLocalUnusedTessellator()) {
//            BufferBuilder fluidBuffer = tess.tessellator.getBuffer();
//            fluidBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
//            fluidBuffer.setTranslation(x, y, z);

        boolean gas = forRender.getRawFluid().getAttributes().isGaseous(forRender);
        boolean horizontal = false;
        boolean vertical = flow.pipe.isConnected(gas ? Direction.DOWN : Direction.UP);

        prof.popPush("build");
        for (Direction face : Direction.values()) {
            double size = ((Pipe) flow.pipe).getConnectedDist(face);
//            double amount = amounts[face.getIndex()];
            double amount = amounts[face.get3DDataValue()];
            if (face.getAxis() != Axis.Y) {
                horizontal |= flow.pipe.isConnected(face) && amount > 0;
            }

            Vector3d center = VecUtil.offset(new Vector3d(0.5, 0.5, 0.5), face, 0.245 + size / 2);
            Vector3d radius = new Vector3d(0.24, 0.24, 0.24);
            radius = VecUtil.replaceValue(radius, face.getAxis(), 0.005 + size / 2);

            if (face.getAxis() == Axis.Y) {
                double perc = amount / flow.capacity;
                perc = Math.sqrt(perc);
                radius = new Vector3d(perc * 0.24, radius.y, perc * 0.24);
            }

//            Vector3d offset = offsets[face.getIndex()];
            Vector3d offset = offsets[face.get3DDataValue()];
            // TODO Calen: when complete SpriteFluidFrozen.class, release the comments ##0-6/7
            // TODO Calen: if force use FROZEN, the texture will flash
            if (offset == null) offset = Vector3d.ZERO;
//            center = center.add(offset); // TODO Calen ##0/7
//            fluidBuffer.setTranslation(x - offset.x, y - offset.y, z - offset.z);
            poseStack.pushPose();

//            poseStack.translate(-offset.x, -offset.y, -offset.z); // TODO Calen ##1/7

            Vector3d min = center.subtract(radius);
            Vector3d max = center.add(radius);

            if (face.getAxis() == Axis.Y) {
                FluidRenderer.renderFluid(FluidSpriteType.FROZEN, forRender, 1, 1, min, max, poseStack.last(), fluidBuffer, sides);
            } else {
                FluidRenderer.renderFluid(FluidSpriteType.FROZEN, forRender, amount, flow.capacity, min, max, poseStack.last(), fluidBuffer, sides);
            }
            poseStack.popPose();
        }

        double amount = amounts[EnumPipePart.CENTER.getIndex()];

        double horizPos = 0.26;

        Vector3d offset = offsets[EnumPipePart.CENTER.getIndex()];
        if (offset == null) offset = Vector3d.ZERO;
//            fluidBuffer.setTranslation(x - offset.x, y - offset.y, z - offset.z);
        poseStack.pushPose();
//        poseStack.translate(-offset.x, -offset.y, -offset.z); // TODO Calen ##2/7
        Entry pose = poseStack.last();

        if (horizontal | !vertical) {
            Vector3d min = new Vector3d(0.26, 0.26, 0.26);
            Vector3d max = new Vector3d(0.74, 0.74, 0.74);

//            min = min.add(offset); // TODO Calen ##3/7
//            max = max.add(offset); // TODO Calen ##4/7

            FluidRenderer.renderFluid(FluidSpriteType.FROZEN, forRender, amount, flow.capacity, min, max, pose, fluidBuffer, sides);
            horizPos += (max.y - min.y) * amount / flow.capacity;
        }
        poseStack.popPose();

        if (vertical && horizPos < 0.74) {
            double perc = amount / flow.capacity;
            perc = Math.sqrt(perc);
            double minXZ = 0.5 - 0.24 * perc;
            double maxXZ = 0.5 + 0.24 * perc;

            double yMin = gas ? 0.26 : horizPos;
            double yMax = gas ? 1 - horizPos : 0.74;

            Vector3d min = new Vector3d(minXZ, yMin, minXZ);
            Vector3d max = new Vector3d(maxXZ, yMax, maxXZ);
//            min = min.add(offset); // TODO Calen ##5/7
//            max = max.add(offset); // TODO Calen ##6/7

            FluidRenderer.renderFluid(FluidSpriteType.FROZEN, forRender, 1, 1, min, max, pose, fluidBuffer, sides);
        }

//            // gl state setup
//            RenderHelper.disableStandardItemLighting();
//            Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
//            GlStateManager.enableBlend();
//            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
//            GlStateManager.enableCull();

        prof.popPush("draw");
//            fluidBuffer.setTranslation(0, 0, 0);
//            tess.tessellator.draw();
//        }

//        RenderHelper.enableStandardItemLighting();

        FluidRenderer.vertex.lighti((byte) 0xF, (byte) 0xF);
        prof.pop();
    }
}
