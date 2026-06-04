/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.transport.client.render;

import java.util.EnumMap;
import java.util.Map;

import org.joml.Vector3f;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.math.Vec3d;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.transport.EnumWirePart;

import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.ModelUtil.UvFaceData;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraft.lib.misc.VecUtil;

import buildcraft.transport.tile.TilePipeHolder;
import buildcraft.transport.wire.EnumWireBetween;

@Environment(EnvType.CLIENT)
public class PipeWireRenderer {

    private static final Map<EnumWirePart, MutableQuad[]> partQuads =
        new EnumMap<>(EnumWirePart.class);
    private static final Map<EnumWireBetween, MutableQuad[]> betweenQuads =
        new EnumMap<>(EnumWireBetween.class);
    private static final Map<DyeColor, SpriteHolder> wireSprites =
        new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor color : DyeColor.values()) {
            wireSprites.put(color,
                SpriteHolderRegistry.getHolder("buildcrafttransport:wires/" + color.getName()));
        }
        for (EnumWirePart part : EnumWirePart.VALUES) {
            partQuads.put(part, buildPartQuads(part));
        }
        for (EnumWireBetween between : EnumWireBetween.VALUES) {
            betweenQuads.put(between, buildBetweenQuads(between));
        }
    }

    public static void clearWireCache() {
        // No display-list cache in Fabric; quad arrays are static and always valid.
    }

    public static SpriteHolder getWireSprite(DyeColor colour) {
        return wireSprites.get(colour);
    }

    /** Render all wires on the given pipe into {@code vc}. */
    public static void renderWires(TilePipeHolder pipe, MatrixStack matrices,
            VertexConsumer vc, int light) {
        for (Map.Entry<EnumWirePart, DyeColor> e : pipe.wireManager.parts.entrySet()) {
            renderPart(e.getKey(), e.getValue(), pipe, matrices, vc, light);
        }
        for (Map.Entry<EnumWireBetween, DyeColor> e : pipe.wireManager.betweens.entrySet()) {
            renderBetween(e.getKey(), e.getValue(), pipe, matrices, vc, light);
        }
    }

    private static void renderPart(EnumWirePart part, DyeColor color, TilePipeHolder pipe,
            MatrixStack matrices, VertexConsumer vc, int light) {
        boolean isOn = pipe.wireManager.isPowered(part);
        ISprite sprite = wireSprites.get(color);
        MutableQuad[] quads = partQuads.get(part);
        int quadLight = isOn ? (15 << 20 | 15 << 4) : light;
        renderQuads(quads, sprite, isOn, matrices, vc, quadLight);
    }

    private static void renderBetween(EnumWireBetween between, DyeColor color, TilePipeHolder pipe,
            MatrixStack matrices, VertexConsumer vc, int light) {
        boolean isOn = pipe.wireManager.isPowered(between.parts[0]);
        ISprite sprite = wireSprites.get(color);
        MutableQuad[] quads = betweenQuads.get(between);
        int quadLight = isOn ? (15 << 20 | 15 << 4) : light;
        renderQuads(quads, sprite, isOn, matrices, vc, quadLight);
    }

    private static void renderQuads(MutableQuad[] quads, ISprite sprite, boolean isOn,
            MatrixStack matrices, VertexConsumer vc, int light) {
        float vOffset = isOn ? (15 / 16f) : 0f;
        for (MutableQuad q : quads) {
            MutableQuad copy = new MutableQuad(q);
            copy.lighti(light >> 4 & 0xFFFF, light >> 20 & 0xFFFF);
            // Apply sprite UVs with vertical offset for the on/off state strip
            copy.vertex_0.texf(
                (float) sprite.getInterpU(copy.vertex_0.tex_u),
                (float) sprite.getInterpV(copy.vertex_0.tex_v + vOffset));
            copy.vertex_1.texf(
                (float) sprite.getInterpU(copy.vertex_1.tex_u),
                (float) sprite.getInterpV(copy.vertex_1.tex_v + vOffset));
            copy.vertex_2.texf(
                (float) sprite.getInterpU(copy.vertex_2.tex_u),
                (float) sprite.getInterpV(copy.vertex_2.tex_v + vOffset));
            copy.vertex_3.texf(
                (float) sprite.getInterpU(copy.vertex_3.tex_u),
                (float) sprite.getInterpV(copy.vertex_3.tex_v + vOffset));
            copy.render(vc);
        }
    }

    // -------------------------------------------------------------------------
    // Quad geometry builders
    // -------------------------------------------------------------------------

    private static MutableQuad[] buildPartQuads(EnumWirePart part) {
        MutableQuad[] quads = new MutableQuad[6];
        Vector3f center = new Vector3f(
            0.5f + part.x.offset() * 4.51f / 16f,
            0.5f + part.y.offset() * 4.51f / 16f,
            0.5f + part.z.offset() * 4.51f / 16f
        );
        Vector3f radius = new Vector3f(1 / 32f, 1 / 32f, 1 / 32f);
        int off = axisDir(part.x) * 4 + axisDir(part.y) * 2 + axisDir(part.z);
        UvFaceData uvs = new UvFaceData(off / 16f, 0f, (off + 1) / 16f, 1 / 16f);
        for (Direction face : Direction.values()) {
            quads[face.ordinal()] = ModelUtil.createFace(face, center, radius, uvs);
        }
        return quads;
    }

    private static MutableQuad[] buildBetweenQuads(EnumWireBetween between) {
        MutableQuad[] quads = new MutableQuad[4];

        boolean ax = between.mainAxis == Axis.X;
        boolean ay = between.mainAxis == Axis.Y;
        boolean az = between.mainAxis == Axis.Z;

        Vec3d center;
        Vec3d radius;
        if (between.to == null) {
            double cL = 0.5 - 4.51 / 16.0;
            double cU = 0.5 + 4.51 / 16.0;
            center = new Vec3d(
                ax ? 0.5 : (between.xy ? cU : cL),
                ay ? 0.5 : ((ax ? between.xy : between.yz) ? cU : cL),
                az ? 0.5 : (between.yz ? cU : cL)
            );
            double rC = 4.01 / 16.0;
            double rN = 1.0 / 32.0;
            radius = new Vec3d(ax ? rC : rN, ay ? rC : rN, az ? rC : rN);
        } else {
            double cL = (8 - 4.51) / 16.0;
            double cU = (8 + 4.51) / 16.0;
            radius = new Vec3d(
                ax ? 2.99 / 32.0 : 1 / 32.0,
                ay ? 2.99 / 32.0 : 1 / 32.0,
                az ? 2.99 / 32.0 : 1 / 32.0
            );
            center = new Vec3d(
                ax ? (0.5 + 6.505 / 16.0 * between.to.getOffsetX()) : (between.xy ? cU : cL),
                ay ? (0.5 + 6.505 / 16.0 * between.to.getOffsetY()) : ((ax ? between.xy : between.yz) ? cU : cL),
                az ? (0.5 + 6.505 / 16.0 * between.to.getOffsetZ()) : (between.yz ? cU : cL)
            );
        }

        UvFaceData uvBase = new UvFaceData(
            (float) VecUtil.getValue(center.subtract(radius), between.mainAxis),
            0f,
            (float) VecUtil.getValue(center.add(radius), between.mainAxis),
            1 / 16f
        );
        Vector3f centerF = VecUtil.convertFloat(center);
        Vector3f radiusF = VecUtil.convertFloat(radius);

        int i = 0;
        for (Direction face : Direction.values()) {
            if (face.getAxis() == between.mainAxis) continue;
            UvFaceData uvs = new UvFaceData(uvBase);
            int rotations = computeBetweenUvMods(uvs, between, face);
            MutableQuad q = ModelUtil.createFace(face, centerF, radiusF, uvs);
            if (rotations > 0) q.rotateTextureUp(rotations);
            quads[i++] = q;
        }
        return quads;
    }

    /** Applies swap mods to {@code uvs} in-place; returns rotation count (for post-create rotateTextureUp). */
    private static int computeBetweenUvMods(UvFaceData uvs, EnumWireBetween between, Direction face) {
        Axis aAxis = between.mainAxis;
        Axis fAxis = face.getAxis();
        boolean fPositive = face.getDirection() == AxisDirection.POSITIVE;

        int rotations = 0;
        boolean swapU = false;
        boolean swapV = false;
        if (aAxis == Axis.X) {
            swapV = fPositive;
        } else if (aAxis == Axis.Y) {
            rotations = 1;
            swapU = (fAxis == Axis.X) != fPositive;
            swapV = fAxis == Axis.Z;
        } else {
            if (fAxis == Axis.Y) rotations = 1;
            swapU = face == Direction.DOWN;
            swapV = face != Direction.EAST;
        }
        if (swapU) {
            float t = uvs.minU; uvs.minU = uvs.maxU; uvs.maxU = t;
        }
        if (swapV) {
            float t = uvs.minV; uvs.minV = uvs.maxV; uvs.maxV = t;
        }
        return rotations;
    }

    private static int axisDir(AxisDirection dir) {
        return dir == AxisDirection.POSITIVE ? 1 : 0;
    }
}
