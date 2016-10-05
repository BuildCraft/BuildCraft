package buildcraft.lib.client.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.vecmath.*;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.renderer.vertex.VertexFormatElement.EnumUsage;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import buildcraft.api.core.BCLog;

public class MutableVertex {
    private final float[] position = new float[3];
    private final float[] normal = new float[] { -1, -1, -1 };
    private final float[] colour = new float[] { 1, 1, 1, 1 };
    private final float[] uv = new float[2];
    private final float[] light = new float[2];

    public MutableVertex() {}

    public MutableVertex(MutableVertex from) {
        System.arraycopy(from.position, 0, position, 0, 3);
        System.arraycopy(from.normal, 0, normal, 0, 3);
        System.arraycopy(from.colour, 0, colour, 0, 4);
        System.arraycopy(from.uv, 0, uv, 0, 2);
        System.arraycopy(from.light, 0, light, 0, 2);
    }

    public void toBakedBlock(int[] data, int offset) {
        // POSITION_3F
        data[offset + 0] = Float.floatToRawIntBits(position[0]);
        data[offset + 1] = Float.floatToRawIntBits(position[1]);
        data[offset + 2] = Float.floatToRawIntBits(position[2]);
        // COLOR_4UB
        data[offset + 3] = colourRGBA();
        // TEX_2F
        data[offset + 4] = Float.floatToRawIntBits(uv[0]);
        data[offset + 5] = Float.floatToRawIntBits(uv[1]);
        // TEX_2S
        data[offset + 6] = 0;
    }

    public void toBakedItem(int[] data, int offset) {
        // POSITION_3F
        data[offset + 0] = Float.floatToRawIntBits(position[0]);
        data[offset + 1] = Float.floatToRawIntBits(position[1]);
        data[offset + 2] = Float.floatToRawIntBits(position[2]);
        // COLOR_4UB
        data[offset + 3] = colourRGBA();
        // TEX_2F
        data[offset + 4] = Float.floatToRawIntBits(uv[0]);
        data[offset + 5] = Float.floatToRawIntBits(uv[1]);
        // NROMAL_3B
        data[offset + 6] = normalToPackedInt();
    }

    public void setData(float[][] from, VertexFormat vfFrom) {
        int index = 0;
        for (VertexFormatElement elem : vfFrom.getElements()) {
            System.arraycopy(from[index], 0, getFor(elem), 0, from[index].length);
            index++;
        }
    }

    public float[][] getData(VertexFormat as) {
        float[][] data = new float[as.getElementCount()][];
        int index = 0;
        for (VertexFormatElement elem : as.getElements()) {
            float[] f = getFor(elem);
            data[index] = Arrays.copyOf(f, f.length);
            index++;
        }
        return data;
    }

    private float[] getFor(VertexFormatElement element) {
        EnumUsage usage = element.getUsage();
        if (usage == EnumUsage.POSITION) {
            return position;
        } else if (usage == EnumUsage.NORMAL) {
            return normal;
        } else if (usage == EnumUsage.COLOR) {
            return colour;
        } else if (usage == EnumUsage.UV) {
            if (element.getIndex() == 0) {
                return uv;
            } else if (element.getIndex() == 1) {
                return light;
            }
        }
        // Otherwise... thats not good.
        String s = element.toString();
        if (!failedStrings.contains(s) && usage != EnumUsage.PADDING) {
            failedStrings.add(s);
            BCLog.logger.info("Element " + s + " failed!");
        }
        return new float[element.getElementCount()];
    }

    private static Set<String> failedStrings = new HashSet<>();

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
        vb.pos(position[0], position[1], position[2]);
    }

    public void renderNormal(VertexBuffer vb) {
        vb.normal(normal[0], normal[1], normal[2]);
    }

    public void renderColour(VertexBuffer vb) {
        vb.color(colour[0], colour[1], colour[2], colour[3]);
    }

    public void renderTex(VertexBuffer vb) {
        vb.tex(uv[0], uv[1]);
    }

    public void renderLightMap(VertexBuffer vb) {
        int[] lighti = lighti();
        vb.lightmap(lighti[0] << 4, lighti[1] << 4);
    }

    // Mutating

    public MutableVertex positionv(Tuple3f vec) {
        return positionf(vec.x, vec.y, vec.z);
    }

    public MutableVertex positiond(double x, double y, double z) {
        return positionf((float) x, (float) y, (float) z);
    }

    public MutableVertex positionf(float x, float y, float z) {
        position[0] = x;
        position[1] = y;
        position[2] = z;
        return this;
    }

    public Point3f position() {
        return new Point3f(position);
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
        normal[0] = x;
        normal[1] = y;
        normal[2] = z;
        return this;
    }

    public MutableVertex invertNormal() {
        return normalf(-normal[0], -normal[1], -normal[2]);
    }

    /** @return The current normal vector of this vertex. This might be normalised. */
    public Vector3f normal() {
        return new Vector3f(normal);
    }

    public int normalToPackedInt() {
        return normalAsByte(normal[0], 0) //
            | normalAsByte(normal[1], 8) //
            | normalAsByte(normal[2], 16);
    }

    private static int normalAsByte(float norm, int offset) {
        int as = (int) (norm * 0x7f);
        return as << offset;
    }

    public MutableVertex colourv(Tuple4f vec) {
        return colourf(vec.x, vec.y, vec.z, vec.w);
    };

    public MutableVertex colourf(float r, float g, float b, float a) {
        colour[0] = r;
        colour[1] = g;
        colour[2] = b;
        colour[3] = a;
        return this;
    }

    public MutableVertex colouri(int rgba) {
        return colouri(rgba, rgba >> 8, rgba >> 16, rgba >>> 24);
    }

    public MutableVertex colouri(int r, int g, int b, int a) {
        return colourf((r & 0xFF) / 255f, (g & 0xFF) / 255f, (b & 0xFF) / 255f, (a & 0xFF) / 255f);
    }

    public Point4f colourv() {
        return new Point4f(colour);
    }

    public int colourRGBA() {
        return (rc(0) << 0)//
            + (rc(1) << 8)//
            + (rc(2) << 16)//
            + (rc(3) << 24);//
    }

    public int colourABGR() {
        return (rc(0) << 24)//
            + (rc(1) << 16)//
            + (rc(2) << 8)//
            + (rc(3) << 0);//
    }

    private int rc(int idx) {
        return (int) (colour[idx] * 0xFF);
    }

    public MutableVertex multColourd(double d) {
        return multColourd(d, d, d, 1);
    }

    public MutableVertex multColourd(double r, double g, double b, double a) {
        colour[0] *= r;
        colour[1] *= g;
        colour[2] *= b;
        colour[3] *= a;
        return this;
    }

    public MutableVertex texFromSprite(TextureAtlasSprite sprite) {
        uv[0] = sprite.getInterpolatedU(uv[0]);
        uv[1] = sprite.getInterpolatedV(uv[1]);
        return this;
    }

    public MutableVertex texv(Tuple2f vec) {
        return texf(vec.x, vec.y);
    }

    public MutableVertex texf(float u, float v) {
        uv[0] = u;
        uv[1] = v;
        return this;
    }

    public Point2f tex() {
        return new Point2f(uv);
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
        light[0] = light(block);
        light[1] = light(sky);
        return this;
    }

    public Point2f light() {
        return new Point2f(light);
    }

    public int lightc() {
        return light(light[0]) << 4 + light(light[1]) << 20;
    }

    public int[] lighti() {
        return new int[] { light(light[0]), light(light[1]) };
    };

    private static float light(int val) {
        val &= 0xF;
        return (float) val * 0x20 / 0xFFFF;
    }

    private static int light(float val) {
        return (int) (val * 0xFFFF / 0x20);
    }

    public MutableVertex transform(Matrix4f matrix) {
        Point3f point = position();
        matrix.transform(point);
        positionv(point);
        return this;
    }

    public MutableVertex translatei(int x, int y, int z) {
        position[0] += x;
        position[1] += y;
        position[2] += z;
        return this;
    }

    public MutableVertex translatef(float x, float y, float z) {
        position[0] += x;
        position[1] += y;
        position[2] += z;
        return this;
    }

    public MutableVertex translated(double x, double y, double z) {
        position[0] += x;
        position[1] += y;
        position[2] += z;
        return this;
    }

    public MutableVertex translatevi(Vec3i vec) {
        return translatei(vec.getX(), vec.getY(), vec.getZ());
    }

    public MutableVertex translatevd(Vec3d vec) {
        return translated(vec.xCoord, vec.yCoord, vec.zCoord);
    }

    @Override
    public String toString() {
        return "\tVertex [\n\t\tposition=" + Arrays.toString(position) + ",\n\t\tnormal=" + Arrays.toString(normal) + ",\n\t\tcolour=" + Arrays.toString(colour) + ",\n\t\tuv=" + Arrays.toString(uv) + ",\n\t\tlight=" + Arrays.toString(light)
            + "\n\t]";
    }
}
