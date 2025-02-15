/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.model;

import buildcraft.api.core.render.ISprite;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * Holds all of the information necessary to make one of the verticies in a {@link BakedQuad}. This provides a variety
 * of methods to quickly set or get different elements. This should be used with {@link MutableQuad} to make a face, or
 * by itself if you only need to define a single vertex. <br>
 * This currently holds the 3D position, normal, colour, 2D texture, skylight and blocklight. Note that you don't have
 * to use all of the elements for this to work - the extra elements come with sensible defaults. <br>
 * All of the mutating methods are in the form {@literal <element><type>}, where {@literal <element>} is the element to
 * set/get, and {@literal <type>} is the type that they should be set as. So {@link #positiond(double, double, double)}
 * will take in 3 doubles and set them to the position element, and {@link #colouri(int, int, int, int)} will take in 4
 * int's and set them to the colour elements.
 */
@OnlyIn(Dist.CLIENT)
public class MutableVertex {
    /** The position of this vertex. */
    public float position_x, position_y, position_z;
    /** The normal of this vertex. Might not be normalised. Default value is [0, 1, 0]. */
    public float normal_x, normal_y, normal_z;
    /** The colour of this vertex, where each one is a number in the range 0-255. Default value is 255. */
    public short colour_r, colour_g, colour_b, colour_a;
    /** The texture co-ord of this vertex. Should usually be between 0-1 */
    public float tex_u, tex_v;
    /** The light of this vertex. Should be in the range 0-15. */
    public byte light_block, light_sky;
    // Calen
    public int overlay;

    public MutableVertex() {
        normal_x = 0;
        normal_y = 1;
        normal_z = 0;

        colour_r = 0xFF;
        colour_g = 0xFF;
        colour_b = 0xFF;
        colour_a = 0xFF;
    }

    public MutableVertex(MutableVertex from) {
        copyFrom(from);
    }

    @Override
    public String toString() {
        return "{ pos = [ " + position_x + ", " + position_y + ", " + position_z //
                + " ], norm = [ " + normal_x + ", " + normal_y + ", " + normal_z//
                + " ], colour = [ " + colour_r + ", " + colour_g + ", " + colour_b + ", " + colour_a//
                + " ], tex = [ " + tex_u + ", " + tex_v //
                + " ], light_block = " + light_block + ", light_sky = " + light_sky + " }";
    }

    public MutableVertex copyFrom(MutableVertex from) {
        position_x = from.position_x;
        position_y = from.position_y;
        position_z = from.position_z;

        normal_x = from.normal_x;
        normal_y = from.normal_y;
        normal_z = from.normal_z;

        colour_r = from.colour_r;
        colour_g = from.colour_g;
        colour_b = from.colour_b;
        colour_a = from.colour_a;

        tex_u = from.tex_u;
        tex_v = from.tex_v;

        light_block = from.light_block;
        light_sky = from.light_sky;
        return this;
    }

    public void toBakedBlock(int[] data, int offset) {
        // POSITION_3F
        data[offset + 0] = Float.floatToRawIntBits(position_x);
        data[offset + 1] = Float.floatToRawIntBits(position_y);
        data[offset + 2] = Float.floatToRawIntBits(position_z);
        // COLOR_4UB
        data[offset + 3] = colourRGBA();
        // TEX_2F
        data[offset + 4] = Float.floatToRawIntBits(tex_u);
        data[offset + 5] = Float.floatToRawIntBits(tex_v);
        // TEX_2S
        data[offset + 6] = lightc();
        // Calen: NORMAL_3B + ELEMENT_PADDING 1B -> 1 Int
        data[offset + 7] = normalToPackedInt();
    }

    public void toBakedItem(int[] data, int offset) {
        // POSITION_3F
        data[offset + 0] = Float.floatToRawIntBits(position_x);
        data[offset + 1] = Float.floatToRawIntBits(position_y);
        data[offset + 2] = Float.floatToRawIntBits(position_z);
        // COLOR_4UB -> 1 int
        data[offset + 3] = colourRGBA();
        // TEX_2F
        data[offset + 4] = Float.floatToRawIntBits(tex_u);
        data[offset + 5] = Float.floatToRawIntBits(tex_v);
        // ELEMENT_UV2 Light 2 Short -> 1 int
        data[offset + 6] = lightc();
        // Calen: NORMAL_3B + ELEMENT_PADDING 1B -> 1 Int
        data[offset + 7] = normalToPackedInt();
    }

    public void fromBakedBlock(int[] data, int offset) {
        // POSITION_3F
        position_x = Float.intBitsToFloat(data[offset + 0]);
        position_y = Float.intBitsToFloat(data[offset + 1]);
        position_z = Float.intBitsToFloat(data[offset + 2]);
        // COLOR_4UB
        colouri(data[offset + 3]);
        // TEX_2F
        tex_u = Float.intBitsToFloat(data[offset + 4]);
        tex_v = Float.intBitsToFloat(data[offset + 5]);
        // TEX_2S
        lighti(data[offset + 6]);
//        normalf(0, 1, 0);
        // NORMAL_3B
        normali(data[offset + 7]);
    }

    public void fromBakedItem(int[] data, int offset) {
        // POSITION_3F
        position_x = Float.intBitsToFloat(data[offset + 0]);
        position_y = Float.intBitsToFloat(data[offset + 1]);
        position_z = Float.intBitsToFloat(data[offset + 2]);
        // COLOR_4UB
        colouri(data[offset + 3]);
        // TEX_2F
        tex_u = Float.intBitsToFloat(data[offset + 4]);
        tex_v = Float.intBitsToFloat(data[offset + 5]);
        // TEX_2S
//        lightf(1, 1);
        lighti(data[offset + 6]);
        // NORMAL_3B
        normali(data[offset + 7]);
    }

    // Rendering

    // public void render(BufferBuilder bb)
    public void render(PoseStack.Pose pose, VertexConsumer vertexConsumer) {
//        VertexFormat vf = bb.getVertexFormat();
//        if (vf == DefaultVertexFormats.BLOCK) {
//            renderAsBlock(bb);
//        } else {
//            for (VertexFormatElement vfe : vf.getElements()) {
//                if (vfe.getUsage() == EnumUsage.POSITION) renderPosition(bb);
//                else if (vfe.getUsage() == EnumUsage.NORMAL) renderNormal(bb);
//                else if (vfe.getUsage() == EnumUsage.COLOR) renderColour(bb);
//                else if (vfe.getUsage() == EnumUsage.UV) {
//                    if (vfe.getIndex() == 0) renderTex(bb);
//                    else if (vfe.getIndex() == 1) renderLightMap(bb);
//                }
//            }
//            bb.endVertex();
//        }
        renderAsBlock(pose, vertexConsumer);
    }

    // Calen
    public void renderPositionColour(VertexConsumer vertexConsumer) {
//        renderPosition(vertexConsumer, pose.pose());
        renderPositionWithoutPose(vertexConsumer);
        renderColour(vertexConsumer);
//        renderNormal(pose.normal(), vertexConsumer);
        renderNormalWithoutPose(vertexConsumer);
        vertexConsumer.endVertex();
    }

    /** Renders this vertex into the given {@link BufferBuilder}, assuming that the {@link VertexFormat} is
     * {@link DefaultVertexFormat#BLOCK}.
     * <p>
     * Slight performance increase over {@link #render(PoseStack.Pose, VertexConsumer)}. */
//    public void renderAsBlock(BufferBuilder bb)
    public void renderAsBlock(PoseStack.Pose lastMatrix, VertexConsumer buffer) {
//        buffer.vertex(poseStack.last().pose(), position_x, position_y, position_z); // Calen test

        renderPosition(buffer, lastMatrix.pose());
        renderColour(buffer);
        renderTex(buffer);
        renderOverlay(buffer);
        renderLightMap(buffer);
        renderNormal(lastMatrix.normal(), buffer);
        buffer.endVertex();
    }

    // public void renderPosition(BufferBuilder bb)
    public void renderPosition(VertexConsumer bb, org.joml.Matrix4f matrix4f) {
//        bb.pos(position_x, position_y, position_z);
        bb.vertex(matrix4f, position_x, position_y, position_z);
    }

    // Calen 1.20.1
    public void renderPositionWithoutPose(VertexConsumer bb) {
//        bb.pos(position_x, position_y, position_z);
        bb.vertex(position_x, position_y, position_z);
    }

    public void renderNormal(org.joml.Matrix3f normal, VertexConsumer bb) {
        bb.normal(normal, normal_x, normal_y, normal_z);
    }

    // Calen 1.20.1
    public void renderNormalWithoutPose(VertexConsumer bb) {
        bb.normal(normal_x, normal_y, normal_z);
    }

    // public void renderColour(BufferBuilder bb)
    public void renderColour(VertexConsumer bb) {
        bb.color(colour_r, colour_g, colour_b, colour_a);
    }

    public void renderTex(VertexConsumer bb) {
        bb.uv(tex_u, tex_v);
    }

    public void renderTex(BufferBuilder bb, ISprite sprite) {
        bb.uv((float) sprite.getInterpU(tex_u), (float) sprite.getInterpV(tex_v));
    }

    public void renderLightMap(VertexConsumer bb) {
//        bb.lightmap(light_sky << 4, light_block << 4);
        bb.uv2(lightc());
    }

    // Calen add
    public void renderOverlay(VertexConsumer bb) {
        bb.overlayCoords(overlay);
    }

    // Mutating

    public MutableVertex positionv(Vector3f vec) {
        return positionf(vec.x, vec.y, vec.z);
    }

    public MutableVertex positiond(double x, double y, double z) {
        return positionf((float) x, (float) y, (float) z);
    }

    public MutableVertex positionf(float x, float y, float z) {
        position_x = x;
        position_y = y;
        position_z = z;
        return this;
    }

    public Vector3f positionvf() {
        return new Vector3f(position_x, position_y, position_z);
    }

    /** Sets the current normal for this vertex based off the given vector.<br>
     * Note: This calls {@link #normalf(float, float, float)} internally, so refer to that for more warnings.
     *
     * @see #normalf(float, float, float) */
    public MutableVertex normalv(Vector3f vec) {
        return normalf(vec.x, vec.y, vec.z);
    }

    /** Sets the current normal given the x, y, and z coordinates. These are NOT normalised or checked. */
    public MutableVertex normalf(float x, float y, float z) {
        normal_x = x;
        normal_y = y;
        normal_z = z;
        return this;
    }

    public MutableVertex normali(int combined) {
        normal_x = ((combined >> 0) & 0xFF) / 0x7f;
        normal_y = ((combined >> 8) & 0xFF) / 0x7f;
        normal_z = ((combined >> 16) & 0xFF) / 0x7f;
        return this;
    }

    public MutableVertex invertNormal() {
        return normalf(-normal_x, -normal_y, -normal_z);
    }

    /** @return The current normal vector of this vertex. This might be normalised. */
    public Vector3f normal() {
        return new Vector3f(normal_x, normal_y, normal_z);
    }

    public int normalToPackedInt() {
        return normalAsByte(normal_x, 0) //
                | normalAsByte(normal_y, 8) //
                | normalAsByte(normal_z, 16);
    }

    private static int normalAsByte(float norm, int offset) {
        int as = (int) (norm * 0x7f);
        return as << offset;
    }

    public MutableVertex colourv(Vector4f vec) {
        return colourf(vec.x, vec.y, vec.z, vec.w);
    }

    public MutableVertex colourf(float r, float g, float b, float a) {
        return colouri((int) (r * 0xFF), (int) (g * 0xFF), (int) (b * 0xFF), (int) (a * 0xFF));
    }

    public MutableVertex colouri(int rgba) {
        return colouri(rgba, rgba >> 8, rgba >> 16, rgba >>> 24);
    }

    public MutableVertex colouri(int r, int g, int b, int a) {
        colour_r = (short) (r & 0xFF);
        colour_g = (short) (g & 0xFF);
        colour_b = (short) (b & 0xFF);
        colour_a = (short) (a & 0xFF);
        return this;
    }

    public Vector4f colourv() {
        return new Vector4f(colour_r / 255f, colour_g / 255f, colour_b / 255f, colour_a / 255f);
    }

    public int colourRGBA() {
        int rgba = 0;
        rgba |= (colour_r & 0xFF) << 0;
        rgba |= (colour_g & 0xFF) << 8;
        rgba |= (colour_b & 0xFF) << 16;
        rgba |= (colour_a & 0xFF) << 24;
        return rgba;
    }

    public int colourABGR() {
        int rgba = 0;
        rgba |= (colour_r & 0xFF) << 24;
        rgba |= (colour_g & 0xFF) << 16;
        rgba |= (colour_b & 0xFF) << 8;
        rgba |= (colour_a & 0xFF) << 0;
        return rgba;
    }

    public MutableVertex multColourd(double d) {
        int m = (int) (d * 255);
        return multColouri(m);
    }

    public MutableVertex multColourd(double r, double g, double b, double a) {
        return multColouri((int) (r * 255), (int) (g * 255), (int) (b * 255), (int) (a * 255));
    }

    public MutableVertex multColouri(int by) {
        return multColouri(by, by, by, 255);
    }

    public MutableVertex multColouri(int r, int g, int b, int a) {
        colour_r = (short) (colour_r * r / 255);
        colour_g = (short) (colour_g * g / 255);
        colour_b = (short) (colour_b * b / 255);
        colour_a = (short) (colour_a * a / 255);
        return this;
    }

    /** Multiplies the colour by {@link MutableQuad#diffuseLight(float, float, float)} for the normal. */
    public MutableVertex multShade() {
        return multColourd(MutableQuad.diffuseLight(normal_x, normal_y, normal_z));
    }

    public MutableVertex texFromSprite(TextureAtlasSprite sprite) {
        tex_u = sprite.getU(tex_u * 16);
        tex_v = sprite.getV(tex_v * 16);
        return this;
    }

    public MutableVertex texv(Vector2f vec) {
        return texf(vec.x, vec.y);
    }

    public MutableVertex texf(float u, float v) {
        tex_u = u;
        tex_v = v;
        return this;
    }

    public Vector2f tex() {
        return new Vector2f(tex_u, tex_v);
    }

    public MutableVertex lightv(Vector2f vec) {
        return lightf(vec.x, vec.y);
    }

    public MutableVertex lightf(float block, float sky) {
        return lighti((byte) (block * 0xF), (byte) (sky * 0xF));
//        return lighti((short) (((int) (block * 0xF)) << 4), (short) (((int) (sky * 0xF)) << 4));
    }

    public MutableVertex lighti(int combined) {
        return lighti((byte) (combined >> 4), (byte) (combined >> 20));
    }

    // Calen add
    public MutableVertex overlay(int overlayIn) {
        overlay = overlayIn;
        return this;
    }

    // public MutableVertex maxLighti(int block, int sky)
    public MutableVertex lighti(byte block, byte sky) {
        light_block = (byte) block;
        light_sky = (byte) sky;
        return this;
    }

    public MutableVertex maxLighti(byte block, byte sky) {
//        return lighti(Math.max(block, light_block), Math.max(sky, light_sky));
        return lighti((byte) Math.max(block, light_block), (byte) Math.max(sky, light_sky));
    }

    public Vector2f lightvf() {
        return new Vector2f(light_block * 15f, light_sky * 15f);
    }

    public int lightc() {
        // Calen FIX: BC 1.12.2 made a mistake!
        // without (), + will be calculated before <<
        return (light_block << 4) + (light_sky << 20);
    }

    public int[] lighti() {
        return new int[] { light_block, light_sky };
    }

    public MutableVertex transform(Matrix4f matrix) {
        Vector3f point3f = positionvf();
        Vector4f point4f = new Vector4f(point3f.x, point3f.y, point3f.z, 1);
        matrix.transform(point4f);
        point3f.x = point4f.x;
        point3f.y = point4f.y;
        point3f.z = point4f.z;
        positionv(point3f);

        Vector3f normal3f = normal();
        Vector4f normal4f = new Vector4f(normal3f.x, normal3f.y, normal3f.z, 1);
        matrix.transform(normal4f);
        normal3f.x = normal4f.x;
        normal3f.y = normal4f.y;
        normal3f.z = normal4f.z;
        normalv(normal3f);
        return this;
    }

    public MutableVertex translatei(int x, int y, int z) {
        position_x += x;
        position_y += y;
        position_z += z;
        return this;
    }

    public MutableVertex translatef(float x, float y, float z) {
        position_x += x;
        position_y += y;
        position_z += z;
        return this;
    }

    public MutableVertex translated(double x, double y, double z) {
        position_x += x;
        position_y += y;
        position_z += z;
        return this;
    }

    public MutableVertex translatevi(Vec3i vec) {
        return translatei(vec.getX(), vec.getY(), vec.getZ());
    }

    public MutableVertex translatevd(Vec3 vec) {
        return translated(vec.x, vec.y, vec.z);
    }

    public MutableVertex scalef(float scale) {
        position_x *= scale;
        position_y *= scale;
        position_z *= scale;
        return this;
    }

    public MutableVertex scaled(double scale) {
        return scalef((float) scale);
    }

    public MutableVertex scalef(float x, float y, float z) {
        position_x *= x;
        position_y *= y;
        position_z *= z;
        // TODO: scale normals?
        return this;
    }

    public MutableVertex scaled(double x, double y, double z) {
        return scalef((float) x, (float) y, (float) z);
    }

    /** Rotates around the X axis by angle. */
    public void rotateX(float angle) {
//        float cos = MathHelper.cos(angle);
        float cos = Mth.cos(angle);
//        float sin = MathHelper.sin(angle);
        float sin = Mth.sin(angle);
        rotateDirectlyX(cos, sin);
    }

    /** Rotates around the Y axis by angle. */
    public void rotateY(float angle) {
//        float cos = MathHelper.cos(angle);
        float cos = Mth.cos(angle);
//        float sin = MathHelper.sin(angle);
        float sin = Mth.sin(angle);
        rotateDirectlyY(cos, sin);
    }

    /** Rotates around the Z axis by angle. */
    public void rotateZ(float angle) {
//        float cos = MathHelper.cos(angle);
        float cos = Mth.cos(angle);
//        float sin = MathHelper.sin(angle);
        float sin = Mth.sin(angle);
        rotateDirectlyZ(cos, sin);
    }

    public void rotateDirectlyX(float cos, float sin) {
        float y = position_y;
        float z = position_z;
        position_y = y * cos - z * sin;
        position_z = y * sin + z * cos;
    }

    public void rotateDirectlyY(float cos, float sin) {
        float x = position_x;
        float z = position_z;
        position_x = x * cos - z * sin;
        position_z = x * sin + z * cos;
    }

    public void rotateDirectlyZ(float cos, float sin) {
        float x = position_x;
        float y = position_y;
        position_x = x * cos + y * sin;
        position_y = x * -sin + y * cos;
    }

    /** Rotates this vertex around the X axis 90 degrees.
     *
     * @param scale The multiplier for scaling. Positive values will rotate clockwise, negative values rotate
     *            anti-clockwise. */
    public MutableVertex rotateX_90(float scale) {
        float ym = scale;
        float zm = -ym;

        float t = position_y * ym;
        position_y = position_z * zm;
        position_z = t;

        t = normal_y * ym;
        normal_y = normal_z * zm;
        normal_z = t;
        return this;
    }

    /** Rotates this vertex around the Y axis 90 degrees.
     *
     * @param scale The multiplier for scaling. Positive values will rotate clockwise, negative values rotate
     *            anti-clockwise. */
    public MutableVertex rotateY_90(float scale) {
        float xm = scale;
        float zm = -xm;

        float t = position_x * xm;
        position_x = position_z * zm;
        position_z = t;

        t = normal_x * xm;
        normal_x = normal_z * zm;
        normal_z = t;
        return this;
    }

    /** Rotates this vertex around the Z axis 90 degrees.
     *
     * @param scale The multiplier for scaling. Positive values will rotate clockwise, negative values rotate
     *            anti-clockwise. */
    public MutableVertex rotateZ_90(float scale) {
        float xm = scale;
        float ym = -xm;

        float t = position_x * xm;
        position_x = position_y * ym;
        position_y = t;

        t = normal_x * xm;
        normal_x = normal_y * ym;
        normal_y = t;
        return this;
    }

    /** Rotates this vertex around the X axis by 180 degrees. */
    public MutableVertex rotateX_180() {
        position_y = -position_y;
        position_z = -position_z;
        normal_y = -normal_y;
        normal_z = -normal_z;
        return this;
    }

    /** Rotates this vertex around the Y axis by 180 degrees. */
    public MutableVertex rotateY_180() {
        position_x = -position_x;
        position_z = -position_z;
        normal_x = -normal_x;
        normal_z = -normal_z;
        return this;
    }

    /** Rotates this vertex around the Z axis by 180 degrees. */
    public MutableVertex rotateZ_180() {
        position_x = -position_x;
        position_y = -position_y;
        normal_x = -normal_x;
        normal_y = -normal_y;
        return this;
    }
}
