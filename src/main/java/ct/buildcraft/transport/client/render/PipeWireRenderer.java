/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport.client.render;

import java.util.EnumMap;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

import ct.buildcraft.api.core.render.ISprite;
import ct.buildcraft.api.transport.EnumWirePart;
import ct.buildcraft.lib.client.model.ModelUtil;
import ct.buildcraft.lib.client.model.ModelUtil.UvFaceData;
import ct.buildcraft.lib.client.model.MutableQuad;
import ct.buildcraft.lib.client.model.MutableVertex;
import ct.buildcraft.lib.client.sprite.SpriteHolderRegistry;
import ct.buildcraft.lib.misc.ColourUtil;
import ct.buildcraft.lib.misc.VecUtil;
import ct.buildcraft.transport.tile.TilePipeHolder;
import ct.buildcraft.transport.wire.EnumWireBetween;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PipeWireRenderer {

    private static final Map<EnumWirePart, MutableQuad[]> partQuads = new EnumMap<>(EnumWirePart.class);
    private static final Map<EnumWireBetween, MutableQuad[]> betweenQuads = new EnumMap<>(EnumWireBetween.class);

    private static final Map<DyeColor, SpriteHolderRegistry.SpriteHolder> wireSprites =
        new EnumMap<>(DyeColor.class);
/*    private static final int[] wireRenderingCache =
        new int[(EnumWireBetween.VALUES.length + EnumWirePart.VALUES.length) * ColourUtil.COLOURS.length * 2];*/

    final static int WIRE_COUNT = EnumWirePart.VALUES.length * ColourUtil.COLOURS.length * 2;

    static {
//        Arrays.fill(wireRenderingCache, -1);

        for (DyeColor color : DyeColor.values()) {
            wireSprites.put(color, SpriteHolderRegistry.getHolder("buildcrafttransport:wires/" + color.getName()));
        }

        for (EnumWirePart part : EnumWirePart.VALUES) {
            partQuads.put(part, getQuads(part));
        }
        for (EnumWireBetween part : EnumWireBetween.VALUES) {
            betweenQuads.put(part, getQuads(part));
        }
    }

/*    public static void clearWireCache() {
        Arrays.fill(wireRenderingCache, -1);
    }*/
    
    public static void onTextureStitchPre() {
	}

    public static SpriteHolderRegistry.SpriteHolder getWireSprite(DyeColor colour) {
        return wireSprites.get(colour);
    }

    private static MutableQuad[] getQuads(EnumWirePart part) {
        MutableQuad[] quads = new MutableQuad[6];

        Vector3f center = new Vector3f(//
            0.5f + (part.x.getStep() * 4.51f / 16f), //
            0.5f + (part.y.getStep() * 4.51f / 16f), //
            0.5f + (part.z.getStep() * 4.51f / 16f) //
        );
        Vector3f radius = new Vector3f(1 / 32f, 1 / 32f, 1 / 32f);
        UvFaceData uvs = new UvFaceData();
        int off = func(part.x) * 4 + func(part.y) * 2 + func(part.z);
        uvs.minU = off / 16f;
        uvs.maxU = (off + 1) / 16f;
        uvs.minV = 0;
        uvs.maxV = 1 / 16f;
        for (Direction face : Direction.values()) {
            quads[face.ordinal()] = ModelUtil.createFace(face, center, radius, uvs);
        }
        return quads;
    }

    private static int func(Direction.AxisDirection dir) {
        return dir == AxisDirection.POSITIVE ? 1 : 0;
    }

    private static MutableQuad[] getQuads(EnumWireBetween between) {
        // 4 rather than 6 -- don't render the end caps
        MutableQuad[] quads = new MutableQuad[4];

        int i = 0;

        Vec3 center;
        Vec3 radius;

        boolean ax = between.mainAxis == Axis.X;
        boolean ay = between.mainAxis == Axis.Y;
        boolean az = between.mainAxis == Axis.Z;

        if (between.to == null) {
            double cL = 0.5f - 4.51f / 16f;
            double cU = 0.5f + 4.51f / 16f;
            center = new Vec3(//
                ax ? 0.5f : (between.xy ? cU : cL), //
                ay ? 0.5f : ((ax ? between.xy : between.yz) ? cU : cL), //
                az ? 0.5f : (between.yz ? cU : cL) //
            );
            double rC = 4.01f / 16f;
            double rN = 1f / 16f / 2;
            radius = new Vec3(//
                ax ? rC : rN, //
                ay ? rC : rN, //
                az ? rC : rN //
            );
        } else {// we are a connection
            double cL = (8 - 4.51) / 16;
            double cU = (8 + 4.51) / 16;
            radius = new Vec3(//
                ax ? 2.99 / 32 : 1 / 32.0, //
                ay ? 2.99 / 32 : 1 / 32.0, //
                az ? 2.99 / 32 : 1 / 32.0 //
            );
            center = new Vec3(//
                ax ? (0.5 + 6.505 / 16 * between.to.getStepX()) : (between.xy ? cU : cL), //
                ay ? (0.5 + 6.505 / 16 * between.to.getStepY()) : ((ax ? between.xy : between.yz) ? cU : cL), //
                az ? (0.5 + 6.505 / 16 * between.to.getStepZ()) : (between.yz ? cU : cL) //
            );
        }

        UvFaceData uvBase = new UvFaceData();
        uvBase.minU = (float) VecUtil.getValue(center.subtract(radius), between.mainAxis);
        uvBase.maxU = (float) VecUtil.getValue(center.add(radius), between.mainAxis);
        uvBase.minV = 0;
        uvBase.maxV = 1 / 16f;

        Vector3f centerFloat = new Vector3f(center);
        Vector3f radiusFloat = new Vector3f(radius);

        for (Direction face : Direction.values()) {
            if (face.getAxis() == between.mainAxis) {
                continue;
            }
            UvFaceData uvs = new UvFaceData(uvBase);

            Axis aAxis = between.mainAxis;
            Axis fAxis = face.getAxis();
            boolean fPositive = face.getAxisDirection() == AxisDirection.POSITIVE;

            int rotations = 0;
            boolean swapU = false;
            boolean swapV = false;

            if (aAxis == Axis.X) {
                swapV = fPositive;
            } else if (aAxis == Axis.Y) {
                rotations = 1;
                swapU = (fAxis == Axis.X) != fPositive;
                swapV = fAxis == Axis.Z;
            } else {// aAxis == Axis.Z
                if (fAxis == Axis.Y) {
                    rotations = 1;
                }
                swapU = face == Direction.DOWN;
                swapV = face != Direction.EAST;
            }

            if (swapU) {
                float t = uvs.minU;
                uvs.minU = uvs.maxU;
                uvs.maxU = t;
            }
            if (swapV) {
                float t = uvs.minV;
                uvs.minV = uvs.maxV;
                uvs.maxV = t;
            }

            MutableQuad quad = ModelUtil.createFace(face, centerFloat, radiusFloat, uvs);
            if (rotations > 0) quad.rotateTextureUp(rotations);
            quads[i++] = quad;
        }
        return quads;
    }

    private static void renderQuads(Matrix4f pose, Matrix3f normal, VertexConsumer bb, MutableQuad[] quads,
    		ISprite sprite, int level, int blockLight, int skyLight, int combinedOverlay) {
/*        VertexFormat vf = DefaultVertexFormat.POSITION_TEX_COLOR;
        Tesselator tesselator = new Tesselator(quads.length * vf.getVertexSize());
        BufferBuilder bb = tesselator.getBuilder();
        bb.begin(VertexFormat.Mode.QUADS, vf);*/

        float vOffset = (level & 0xF) / 16f;
        MutableQuad newQuad = new MutableQuad();
        for (MutableQuad q : quads) {
            if (q.getFace() != Direction.UP && level != 15) {
                q = newQuad.copyFrom(q);
                float shade = 1 - q.getCalculatedDiffuse();
                shade = shade * (15 - level) / 15;
                shade = 1 - shade;
                q.multColourd(shade);
            }
            q.lighti(blockLight>>4, skyLight>>4);
            renderVertex(pose, normal, bb, q.vertex_0, sprite, vOffset, combinedOverlay);
            renderVertex(pose, normal, bb, q.vertex_1, sprite, vOffset, combinedOverlay);
            renderVertex(pose, normal, bb, q.vertex_2, sprite, vOffset, combinedOverlay);
            renderVertex(pose, normal, bb, q.vertex_3, sprite, vOffset, combinedOverlay);
        }
//        tesselator.end();
    }

    private static void renderVertex(Matrix4f pose, Matrix3f normal, VertexConsumer bb, MutableVertex vertex, ISprite sprite, float vOffset, int combinedOverlay) {
        vertex.renderPosition(pose, bb);
        vertex.renderColour(bb);
        float u = sprite.getInterpU(vertex.tex_u);
        float v = sprite.getInterpV(vertex.tex_v + vOffset);
        bb.uv(u, v)
        .overlayCoords(combinedOverlay);
        vertex.renderLightMap(bb);
 //       bb.uv2(15728640);
        vertex.renderNormal(normal, bb);
        bb.endVertex();
    }

/*    private static int compileQuads(MutableQuad[] quads, DyeColor colour, boolean isOn) {
        int index = RenderSystem.glGenBuffers(1);
        VertexBuffer vbo = new VertexBuffer();
        
//TODO
        ISprite sprite = wireSprites.get(colour);
        /*
         * Currently pipe wire only supports two states - on or off. However all the textures supply 16 different
         * states, which could (possibly) be used for making pipe wire use all 16 states that normal redstone does. This
         * just opens up the possibility in the future.
         */
/*        renderQuads(quads, sprite, isOn ? 15 : 0);

        GL11.glEndList();
        return index;
    }*/

    private static int getIndex(EnumWirePart part, DyeColor colour, boolean isOn) {
        return part.ordinal() * 32 + colour.ordinal() * 2 + (isOn ? 1 : 0);
    }

    private static int getIndex(EnumWireBetween bet, DyeColor colour, boolean isOn) {
        return WIRE_COUNT + bet.ordinal() * 32 + colour.ordinal() * 2 + (isOn ? 1 : 0);
    }

/*    private static int compileWire(EnumWirePart part, DyeColor colour, boolean isOn) {
        return compileQuads(getQuads(part), colour, isOn);
    }

    private static int compileWire(EnumWireBetween between, DyeColor colour, boolean isOn) {
        return compileQuads(getQuads(between), colour, isOn);
    }*/

    public static void renderWires(TilePipeHolder pipe, float conSize, PoseStack matrix, MultiBufferSource buffer,
			int combinedLight, int combinedOverlay) {
        int skyLight = combinedLight >> 16 & 0xFFFF;
        int blockLight = combinedLight & 0xFFFF;
//        RenderSystem.disa();
//        matrix.pushPose();
        VertexConsumer bb = buffer.getBuffer(RenderType.cutout());
        Matrix4f pose = matrix.last().pose();
        Matrix3f normal = matrix.last().normal();
        for (Map.Entry<EnumWirePart, DyeColor> partColor : pipe.getWireManager().parts.entrySet()) {
            EnumWirePart part = partColor.getKey();
            DyeColor color = partColor.getValue();
            boolean isOn = pipe.wireManager.isPowered(part);
            //int idx = getIndex(part, color, isOn);
            renderQuads(pose, normal, bb, partQuads.get(part), wireSprites.get(color), isOn ? 15 : 0, isOn ? 240 : blockLight, skyLight, combinedOverlay);
/*            if (wireRenderingCache[idx] == -1) {
                wireRenderingCache[idx] = compileWire(part, color, isOn);
            }
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, isOn ? 240 : blockLight, skyLight);
            Render.callList(wireRenderingCache[idx]);*/
        }
        for (Map.Entry<EnumWireBetween, DyeColor> betweenColor : pipe.getWireManager().betweens.entrySet()) {
            EnumWireBetween between = betweenColor.getKey();
            DyeColor color = betweenColor.getValue();
            boolean isOn = pipe.wireManager.isPowered(between.parts[0]);
            //int idx = getIndex(between, color, isOn);
            renderQuads(pose, normal, bb, betweenQuads.get(between), wireSprites.get(color), isOn ? 15 : 0, isOn ? 240 : blockLight, skyLight, combinedOverlay);
/*            if (wireRenderingCache[idx] == -1) {
                wireRenderingCache[idx] = compileWire(between, color, isOn);
            }
            bb.uv2(isOn ? 240 : blockLight, skyLight);
            //OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, );
            GlStateManager.callList(wireRenderingCache[idx]);*/
        }
//        matrix.popPose();
//        GlStateManager.enableLighting();
        /*
         * Directly rendering (like with a gllist) changes the colour directly, so we need to change the opengl state
         * directly
         */
//        GL11.glColor3f(1, 1, 1);
//        GlStateManager.color(1, 1, 1, 1);
    }

    public static void init() {
        // make sure static runs
    }

}
