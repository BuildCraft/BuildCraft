package buildcraft.lib.client.model;

import javax.vecmath.*;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.renderer.vertex.VertexFormatElement.EnumUsage;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class MutableVertex {
    private float position_x, position_y, position_z;
    private float normal_x, normal_y, normal_z;
    private short colour_r, colour_g, colour_b, colour_a;
    private float tex_u, tex_v;
    private byte light_block, light_sky;

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
    }

    public void toBakedItem(int[] data, int offset) {
        // POSITION_3F
        data[offset + 0] = Float.floatToRawIntBits(position_x);
        data[offset + 1] = Float.floatToRawIntBits(position_y);
        data[offset + 2] = Float.floatToRawIntBits(position_z);
        // COLOR_4UB
        data[offset + 3] = colourRGBA();
        // TEX_2F
        data[offset + 4] = Float.floatToRawIntBits(tex_u);
        data[offset + 5] = Float.floatToRawIntBits(tex_v);
        // NROMAL_3B
        data[offset + 6] = normalToPackedInt();
    }

    // Rendering

    public void render(VertexBuffer vb) {
        VertexFormat vf = vb.getVertexFormat();
        for (VertexFormatElement vfe : vf.getElements()) {
            if (vfe.getUsage() == EnumUsage.POSITION) renderPosition(vb);
            else if (vfe.getUsage() == EnumUsage.NORMAL) renderNormal(vb);
            else if (vfe.getUsage() == EnumUsage.COLOR) renderColour(vb);
            else if (vfe.getUsage() == EnumUsage.UV) {
                if (vfe.getIndex() == 0) renderTex(vb);
                else if (vfe.getIndex() == 1) renderLightMap(vb);
            }
        }
        vb.endVertex();
    }

    public void renderPosition(VertexBuffer vb) {
        vb.pos(position_x, position_y, position_z);
    }

    public void renderNormal(VertexBuffer vb) {
        vb.normal(normal_x, normal_y, normal_z);
    }

    public void renderColour(VertexBuffer vb) {
        vb.color(colour_r, colour_g, colour_b, colour_a);
    }

    public void renderTex(VertexBuffer vb) {
        vb.tex(tex_u, tex_v);
    }

    public void renderLightMap(VertexBuffer vb) {
        vb.lightmap(light_sky << 4, light_block << 4);
    }

    // Mutating

    public MutableVertex positionv(Tuple3f vec) {
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

    public Point3f positionvf() {
        return new Point3f(position_x, position_y, position_z);
    }

    /** Sets the current normal for this vertex based off the given vector.
     * 
     * @see #normalf(float, float, float)
     * @implNote This calls {@link #normalf(float, float, float)} internally, so refer to that for more warnings. */
    public MutableVertex normalv(Tuple3f vec) {
        return normalf(vec.x, vec.y, vec.z);
    }

    /** Sets the current normal given the x, y, and z coordinates. These are NOT normalised or checked. */
    public MutableVertex normalf(float x, float y, float z) {
        normal_x = x;
        normal_y = y;
        normal_z = z;
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

    public MutableVertex colourv(Tuple4f vec) {
        return colourf(vec.x, vec.y, vec.z, vec.w);
    };

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

    public Point4f colourv() {
        return new Point4f(colour_r / 255f, colour_g / 255f, colour_b / 255f, colour_a / 255f);
    }

    public int colourRGBA() {
        return (colour_r << 0)//
            | (colour_g << 8)//
            | (colour_b << 16)//
            | (colour_a << 24);
    }

    public int colourABGR() {
        return (colour_r << 24)//
            | (colour_g << 16)//
            | (colour_b << 8)//
            | (colour_a << 0);
    }

    public MutableVertex multColourd(double d) {
        return multColourd(d, d, d, 1);
    }

    public MutableVertex multColourd(double r, double g, double b, double a) {
        return colouri(//
                (int) (colour_r * r),//
                (int) (colour_g * g),//
                (int) (colour_b * b),//
                (int) (colour_a * a)//
        );
    }

    public MutableVertex texFromSprite(TextureAtlasSprite sprite) {
        tex_u = sprite.getInterpolatedU(tex_u);
        tex_v = sprite.getInterpolatedV(tex_v);
        return this;
    }

    public MutableVertex texv(Tuple2f vec) {
        return texf(vec.x, vec.y);
    }

    public MutableVertex texf(float u, float v) {
        tex_u = u;
        tex_v = v;
        return this;
    }

    public Point2f tex() {
        return new Point2f(tex_u, tex_v);
    }

    public MutableVertex lightv(Tuple2f vec) {
        return lightf(vec.x, vec.y);
    }

    public MutableVertex lightf(float block, float sky) {
        return lighti((int) (block * 0xF), (int) (sky * 0xF));
    }

    public MutableVertex lighti(int combined) {
        return lighti(combined >> 4, combined >> 20);
    }

    public MutableVertex lighti(int block, int sky) {
        light_block = (byte) block;
        light_sky = (byte) sky;
        return this;
    }

    public Point2f lightvf() {
        return new Point2f(light_block * 15f, light_sky * 15f);
    }

    public int lightc() {
        return light_block << 4 + light_sky << 20;
    }

    public int[] lighti() {
        return new int[] { light_block, light_sky };
    };

    public MutableVertex transform(Matrix4f matrix) {
        Point3f point = positionvf();
        matrix.transform(point);
        return positionv(point);
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

    public MutableVertex translatevd(Vec3d vec) {
        return translated(vec.xCoord, vec.yCoord, vec.zCoord);
    }
}
