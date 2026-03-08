/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.render;

import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.pipe.IPipeFlowRenderer;
import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.ModelUtil.UvFaceData;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.misc.MathUtil;
import buildcraft.lib.misc.VecUtil;
import buildcraft.transport.BCTransportSprites;
import buildcraft.transport.pipe.flow.PipeFlowRedstoneFlux;
import buildcraft.transport.pipe.flow.PipeFlowRedstoneFlux.Section;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum PipeFlowRendererRf implements IPipeFlowRenderer<PipeFlowRedstoneFlux> {
    INSTANCE;

    @Override
    // public void render(PipeFlowRedstoneFlux flow, double x, double y, double z, float partialTicks, BufferBuilder bb)
    public void render(PipeFlowRedstoneFlux flow, float partialTicks, MatrixStack poseStack, IVertexBuilder bb, int combinedLight, int combinedOverlay) {
        double centrePower = 0;
        double[] power = new double[6];
        for (Direction side : Direction.values()) {
            Section s = flow.getSection(side);
            int i = side.ordinal();
            power[i] = s.displayPower / (double) MjAPI.MJ;
            centrePower = Math.max(centrePower, power[i]);
        }

        // bb.setTranslation(x, y, z);

        if (centrePower > 0) {
            for (Direction side : Direction.values()) {
                if (!flow.pipe.isConnected(side)) {
                    continue;
                }
                int i = side.ordinal();
                Section s = flow.getSection(side);
                double offset = computeOffset(s.clientDisplayFlowLast, s.clientDisplayFlow, partialTicks);
                renderSidePower(side, power[i], centrePower, offset, poseStack.last(), bb);
            }

            Vector3d offsetLast = flow.clientDisplayFlowCentreLast;
            Vector3d offsetThis = flow.clientDisplayFlowCentre;
            double offsetX = computeOffset(offsetLast.x, offsetThis.x, partialTicks);
            double offsetY = computeOffset(offsetLast.y, offsetThis.y, partialTicks);
            double offsetZ = computeOffset(offsetLast.z, offsetThis.z, partialTicks);

            renderCentrePower(centrePower, offsetX, offsetY, offsetZ, poseStack.last(), bb);
        }

        // bb.setTranslation(0, 0, 0);
    }

    private static double computeOffset(double tick0, double tick1, float partialTicks) {
        if (tick0 + 8 < tick1) {
            // overflow upwards
            tick0 += 16;
        } else if (tick1 + 8 < tick0) {
            // overflow downwards
            tick1 += 16;
        }
        double offset = MathUtil.interp(partialTicks, tick0, tick1);
        if (offset >= 16) {
            offset -= 16;
        }
        return offset;
    }

    private static void renderSidePower(Direction side, double power, double centrePower, double offset, MatrixStack.Entry pose, IVertexBuilder bb) {
        if (power < 0) {
            return;
        }
        double radius = 0.248 * power;
        if (radius >= 0.248) {
            // overload = true;
            radius = 0.248;
        }

        TextureAtlasSprite sprite = BCTransportSprites.POWER_FLOW_OVERLOAD.getSprite();

        double centreRadius = 0.252 - (0.248 * centrePower);

        Vector3d centre = VecUtil.offset(VecUtil.VEC_HALF, side, 0.25 + 0.125 - centreRadius / 2);
        Vector3d radiusV = new Vector3d(radius, radius, radius);
        radiusV = VecUtil.replaceValue(radiusV, side.getAxis(), 0.125 + centreRadius / 2);

        Vector3f centreF = new Vector3f((float) centre.x, (float) centre.y, (float) centre.z);
        Vector3f radiusF = new Vector3f((float) radiusV.x, (float) radiusV.y, (float) radiusV.z);

        UvFaceData uvs = new UvFaceData();
        for (Direction face : Direction.values()) {
            if (face == side.getOpposite()) {
                continue;
            }

            AxisAlignedBB box = new AxisAlignedBB(centre.subtract(radiusV).scale(0.5), centre.add(radiusV).scale(0.5));
            box = box.move(VecUtil.offset(Vector3d.ZERO, side, offset * side.getAxisDirection().getStep() / 32));
            ModelUtil.mapBoxToUvs(box, face, uvs);

            MutableQuad quad = ModelUtil.createFace(face, centreF, radiusF, uvs);
            quad.texFromSprite(sprite);
            quad.lighti((byte) 15, (byte) 15);
            quad.render(pose, bb);
        }
    }

    private static void renderCentrePower(double power, double offsetX, double offsetY, double offsetZ, MatrixStack.Entry pose, IVertexBuilder bb) {
        boolean overload = false;
        float radius = 0.248f * (float) power;
        if (radius > 0.248f) {
            // overload = true;
            radius = 0.248f;
        }
        TextureAtlasSprite sprite = BCTransportSprites.POWER_FLOW_OVERLOAD.getSprite();

        Vector3f centre = new Vector3f(0.5f, 0.5f, 0.5f);
        Vector3f radiusP = new Vector3f(radius, radius, radius);

        UvFaceData uvs = new UvFaceData();

        for (Direction face : Direction.values()) {

            AxisAlignedBB box = new AxisAlignedBB(
                    new Vector3d(0.5 - radius, 0.5 - radius, 0.5 - radius).scale(0.5), //
                    new Vector3d(0.5 + radius, 0.5 + radius, 0.5 + radius).scale(0.5)//
            );
            box = box.move(offsetX / 32.0, offsetY / 32.0, offsetZ / 32.0);
            ModelUtil.mapBoxToUvs(box, face, uvs);

            MutableQuad quad = ModelUtil.createFace(face, centre, radiusP, uvs);
            quad.texFromSprite(sprite);
            quad.lighti((byte) 15, (byte) 15);
            quad.render(pose, bb);
        }
    }
}
