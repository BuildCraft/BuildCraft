package buildcraft.core.lib.client.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.renderer.vertex.VertexFormatElement.EnumUsage;

import buildcraft.api.core.BCLog;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point2f;
import javax.vecmath.Point3f;
import javax.vecmath.Tuple2f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

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
        if (!failedStrings.contains(s)) {
            failedStrings.add(s);
            BCLog.logger.info("Element " + s + " failed!");
        }
        return new float[element.getElementCount()];
    }

    private static Set<String> failedStrings = new HashSet<>();

    public void render(WorldRenderer wr) {
        VertexFormat vf = wr.getVertexFormat();
        for (VertexFormatElement vfe : vf.getElements()) {
            if (vfe.getUsage() == EnumUsage.POSITION) wr.pos(position[0], position[1], position[2]);
            else if (vfe.getUsage() == EnumUsage.NORMAL) wr.normal(normal[0], normal[1], normal[2]);
            else if (vfe.getUsage() == EnumUsage.COLOR) wr.color(colour[0], colour[1], colour[2], colour[3]);
            else if (vfe.getUsage() == EnumUsage.UV) {
                if (vfe.getIndex() == 0) wr.tex(uv[0], uv[1]);
                else if (vfe.getIndex() == 1) wr.lightmap(lighti()[0], lighti()[1]);
            }
        }
        wr.endVertex();
    }

    public MutableVertex positionv(Tuple3f vec) {
        return positionf(vec.x, vec.y, vec.z);
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
    public MutableVertex normalv(Vector3f vec) {
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

    public MutableVertex colourv(Vector4f vec) {
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

    public Vector4f colourv() {
        return new Vector4f(colour);
    }

    public int colourRGBA() {
        // @formatter:off
        return (int) (colour[0] * 0xFF)
            + ((int) (colour[1] * 0xFF)) <<  8
            + ((int) (colour[2] * 0xFF)) << 16
            + ((int) (colour[3] * 0xFF)) << 24;
        // @formatter:on
    }

    public MutableVertex texv(Vector2f vec) {
        return texf(vec.x, vec.y);
    }

    public MutableVertex texf(float u, float v) {
        uv[0] = u;
        uv[1] = v;
        return this;
    }

    public Vector2f tex() {
        return new Vector2f(uv);
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

    @Override
    public String toString() {
        return "\tVertex [\n\t\tposition=" + Arrays.toString(position) + ",\n\t\tnormal=" + Arrays.toString(normal) + ",\n\t\tcolour=" + Arrays
                .toString(colour) + ",\n\t\tuv=" + Arrays.toString(uv) + ",\n\t\tlight=" + Arrays.toString(light) + "\n\t]";
    }
}
