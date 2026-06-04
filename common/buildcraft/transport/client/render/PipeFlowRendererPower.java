/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.transport.client.render;

import org.joml.Vector3f;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.pipe.IPipeFlowRenderer;

import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.ModelUtil.UvFaceData;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraft.lib.misc.MathUtil;
import buildcraft.lib.misc.VecUtil;

import buildcraft.transport.BCTransportSprites;
import buildcraft.transport.pipe.flow.PipeFlowPower;
import buildcraft.transport.pipe.flow.PipeFlowPower.Section;

@Environment(EnvType.CLIENT)
public enum PipeFlowRendererPower implements IPipeFlowRenderer<PipeFlowPower> {
    INSTANCE;

    @Override
    public void render(PipeFlowPower flow, MatrixStack matrices, VertexConsumer vc,
            int light, float partialTicks) {
        double centrePower = 0;
        double[] power = new double[6];
        for (Direction side : Direction.values()) {
            Section s = flow.getSection(side);
            int i = side.ordinal();
            power[i] = s.displayPower / (double) MjAPI.MJ;
            centrePower = Math.max(centrePower, power[i]);
        }
        if (centrePower <= 0) return;

        for (Direction side : Direction.values()) {
            if (!flow.pipe.isConnected(side)) continue;
            int i = side.ordinal();
            Section s = flow.getSection(side);
            double offset = computeOffset(s.clientDisplayFlowLast, s.clientDisplayFlow, partialTicks);
            renderSidePower(side, power[i], centrePower, offset, matrices, vc, light);
        }

        Vec3d offsetLast = flow.clientDisplayFlowCentreLast;
        Vec3d offsetThis = flow.clientDisplayFlowCentre;
        double offsetX = computeOffset(offsetLast.x, offsetThis.x, partialTicks);
        double offsetY = computeOffset(offsetLast.y, offsetThis.y, partialTicks);
        double offsetZ = computeOffset(offsetLast.z, offsetThis.z, partialTicks);
        renderCentrePower(centrePower, offsetX, offsetY, offsetZ, matrices, vc, light);
    }

    private static double computeOffset(double tick0, double tick1, float partialTicks) {
        if (tick0 + 8 < tick1) tick0 += 16;
        else if (tick1 + 8 < tick0) tick1 += 16;
        double offset = MathUtil.interp(partialTicks, tick0, tick1);
        if (offset >= 16) offset -= 16;
        return offset;
    }

    private static void renderSidePower(Direction side, double power, double centrePower,
            double offset, MatrixStack matrices, VertexConsumer vc, int light) {
        if (power < 0) return;
        double radius = 0.248 * power;
        if (radius >= 0.248) radius = 0.248;
        boolean overload = false;

        SpriteHolder spriteHolder = overload ? BCTransportSprites.POWER_FLOW_OVERLOAD
                                              : BCTransportSprites.POWER_FLOW;
        if (spriteHolder == null) return;
        Sprite sprite = spriteHolder.getSprite();
        if (sprite == null) return;

        double centreRadius = 0.252 - (0.248 * centrePower);
        Vec3d centre = VecUtil.offset(VecUtil.VEC_HALF, side, 0.25 + 0.125 - centreRadius / 2);
        Vec3d radiusV = new Vec3d(radius, radius, radius);
        radiusV = VecUtil.replaceValue(radiusV, side.getAxis(), 0.125 + centreRadius / 2);

        Vector3f centreF = VecUtil.convertFloat(centre);
        Vector3f radiusF = VecUtil.convertFloat(radiusV);

        UvFaceData uvs = new UvFaceData();
        for (Direction face : Direction.values()) {
            if (face == side.getOpposite()) continue;
            Box box = new Box(
                centre.subtract(radiusV).multiply(0.5),
                centre.add(radiusV).multiply(0.5)
            ).offset(VecUtil.offset(Vec3d.ZERO, side,
                offset * side.getDirection().offset() / 32.0));
            ModelUtil.mapBoxToUvs(box, face, uvs);

            MutableQuad quad = ModelUtil.createFace(face, centreF, radiusF, uvs);
            quad.texFromSprite(sprite);
            quad.lighti(15, 15);
            quad.render(vc);
        }
    }

    private static void renderCentrePower(double power, double offsetX, double offsetY, double offsetZ,
            MatrixStack matrices, VertexConsumer vc, int light) {
        float radius = 0.248f * (float) power;
        if (radius > 0.248f) radius = 0.248f;

        SpriteHolder spriteHolder = BCTransportSprites.POWER_FLOW;
        if (spriteHolder == null) return;
        Sprite sprite = spriteHolder.getSprite();
        if (sprite == null) return;

        Vector3f centre = new Vector3f(0.5f, 0.5f, 0.5f);
        Vector3f radiusP = new Vector3f(radius, radius, radius);

        Vec3d centreVec = new Vec3d(0.5, 0.5, 0.5);
        Vec3d radiusVec = new Vec3d(radius, radius, radius);
        UvFaceData uvs = new UvFaceData();
        for (Direction face : Direction.values()) {
            Box box = new Box(
                centreVec.subtract(radiusVec).multiply(0.5),
                centreVec.add(radiusVec).multiply(0.5)
            ).offset(offsetX / 32.0, offsetY / 32.0, offsetZ / 32.0);
            ModelUtil.mapBoxToUvs(box, face, uvs);

            MutableQuad quad = ModelUtil.createFace(face, centre, radiusP, uvs);
            quad.texFromSprite(sprite);
            quad.lighti(15, 15);
            quad.render(vc);
        }
    }
}
