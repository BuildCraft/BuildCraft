package buildcraft.core.lib.render;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.renderer.vertex.VertexFormatElement.EnumType;
import net.minecraft.client.renderer.vertex.VertexFormatElement.EnumUsage;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;

import buildcraft.api.core.BCLog;

public class MutableQuad {
    public static final VertexFormat ITEM_LMAP = new VertexFormat(DefaultVertexFormats.ITEM);
    public static final VertexFormat ITEM_BLOCK_PADDING = new VertexFormat();

    // Baked Quad array indices
    public static final int X = 0;
    public static final int Y = 1;
    public static final int Z = 2;
    public static final int SHADE = 3;
    public static final int U = 4;
    public static final int V = 5;
    /** Represents either the normal (for items) or lightmap (for blocks) */
    public static final int UNUSED = 6;

    static {
        ITEM_LMAP.addElement(DefaultVertexFormats.TEX_2S);

        ITEM_BLOCK_PADDING.addElement(DefaultVertexFormats.POSITION_3F);
        ITEM_BLOCK_PADDING.addElement(DefaultVertexFormats.COLOR_4UB);
        ITEM_BLOCK_PADDING.addElement(DefaultVertexFormats.TEX_2F);
        ITEM_BLOCK_PADDING.addElement(new VertexFormatElement(0, EnumType.INT, EnumUsage.PADDING, 1));
    }

    public static MutableQuad create(BakedQuad quad, VertexFormat format) {
        int[] data = quad.getVertexData();
        int stride = data.length / 4;
        MutableQuad mutable = new MutableQuad(quad.getTintIndex(), quad.getFace());
        for (int v = 0; v < 4; v++) {
            Vertex vertex = mutable.getVertex(v);
            float x = fromBits(data[stride * v + X]);
            float y = fromBits(data[stride * v + Y]);
            float z = fromBits(data[stride * v + Z]);
            vertex.positionf(x, y, z);
            vertex.colouri(data[stride * v + SHADE]);
            float texU = fromBits(data[stride * v + U]);
            float texV = fromBits(data[stride * v + V]);
            vertex.texf(texU, texV);
        }
        return mutable;
    }

    /** Creates a mutable quad as a copy of the given {@link BakedQuad}. This assumes the baked quad uses the format
     * {@link DefaultVertexFormats#BLOCK} or {@link DefaultVertexFormats#ITEM}, but ignores the lightmap value. (This
     * uses Mutable
     * 
     * @param quad
     * @return */
    public static MutableQuad create(BakedQuad quad) {
        return create(quad, ITEM_BLOCK_PADDING);
    }

    public static float fromBits(int bits) {
        return Float.intBitsToFloat(bits);
    }

    private final Vertex[] verticies = new Vertex[4];
    private int tintIndex = -1;
    private EnumFacing face = null;

    public MutableQuad(int tintIndex, EnumFacing face) {
        this.tintIndex = tintIndex;
        this.face = face;
        for (int v = 0; v < 4; v++) {
            verticies[v] = new Vertex();
        }
    }

    public MutableQuad(VertexFormat format, float[][][] data, int tintIndex, EnumFacing face) {
        this(tintIndex, face);
        for (int v = 0; v < 4; v++) {
            verticies[v].setData(data[v], format);
        }
    }

    public UnpackedBakedQuad toUnpacked(VertexFormat format) {
        float[][][] data = new float[4][][];
        for (int vertex = 0; vertex < 4; vertex++) {
            float[][] fromData = verticies[vertex].getData(format);
            data[vertex] = new float[fromData.length][];
            for (int element = 0; element < fromData.length; element++) {
                data[vertex][element] = new float[fromData[element].length];
                for (int d = 0; d < fromData[element].length; d++) {
                    data[vertex][element][d] = fromData[element][d];
                }
            }
        }
        return new UnpackedBakedQuad(data, tintIndex, face, format);
    }

    public Vertex getVertex(int v) {
        return verticies[v & 0b11];
    }

    /* A lot of delegate functions here. The actual documentation should be per-vertex. */

    // @formatter:off
    /** @see Vertex#normalv(Vector3f) */ public void normalv(Vector3f vec) {Arrays.stream(verticies).forEach(v -> v.normalv(vec));}
    public void normalf(float x, float y, float z) {Arrays.stream(verticies).forEach(v -> v.normalf(x, y, z));}

    public void colourv(Vector4f vec) {Arrays.stream(verticies).forEach(v -> v.colourv(vec));};
    public void colourf(float r, float g, float b, float a) {Arrays.stream(verticies).forEach(v -> v.colourf(r,g,b,a));}
    public void colouri(int rgba) {Arrays.stream(verticies).forEach(v -> v.colouri(rgba));}
    public void colouri(int r, int g, int b, int a) {Arrays.stream(verticies).forEach(v -> v.colouri(r, g, b, a));}

    public void lightv(Vector2f vec) {for (Vertex v : verticies) v.lightv(vec);}
    public void lightf(float block, float sky) {for (Vertex v : verticies) v.lightf(block, sky);}
    public void lighti(int combined) {for (Vertex v : verticies) v.lighti(combined);}
    public void lighti(int block, int sky) {for (Vertex v : verticies) v.lighti(block, sky);}
     // @formatter:on

    @Override
    public String toString() {
        return "MutableQuad [verticies=" + vToS() + ", tintIndex=" + tintIndex + ", face=" + face + "]";
    }

    private String vToS() {
        StringBuilder builder = new StringBuilder();
        for (Vertex v : verticies) {
            builder.append(v.toString() + "\n");
        }
        return builder.toString();
    }

    public static class Vertex {
        private final float[] position = new float[3];
        private final float[] normal = new float[] { -1, -1, -1 };
        private final float[] colour = new float[4];
        private final float[] uv = new float[2];
        private final float[] light = new float[2];

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

        public void positionv(Vector3f vec) {
            positionf(vec.x, vec.y, vec.z);
        }

        public void positionf(float x, float y, float z) {
            position[0] = x;
            position[1] = y;
            position[2] = z;
        }

        public Vector3f position() {
            return new Vector3f(position);
        }

        /** Sets the current normal for this vertex based off the given vector.
         * 
         * @see #normalf(float, float, float)
         * @implNote This calls {@link #normalf(float, float, float)} internally, so refer to that for more warnings. */
        public void normalv(Vector3f vec) {
            normalf(vec.x, vec.y, vec.z);
        }

        /** Sets the current normal given the x, y, and z coordinates. These are NOT normalised or checked. */
        public void normalf(float x, float y, float z) {
            normal[0] = x;
            normal[1] = y;
            normal[2] = z;
        }

        /** @return The current normal vector of this vertex. This might be normalised. */
        public Vector3f normal() {
            return new Vector3f(normal);
        }

        public void colourv(Vector4f vec) {
            colourf(vec.x, vec.y, vec.z, vec.w);
        };

        public void colourf(float r, float g, float b, float a) {
            colour[0] = r;
            colour[1] = g;
            colour[2] = b;
            colour[3] = a;
        }

        public void colouri(int rgba) {
            colouri(rgba, rgba >> 8, rgba >> 16, rgba >>> 24);
        }

        public void colouri(int r, int g, int b, int a) {
            colourf((r & 0xFF) / 255f, (g & 0xFF) / 255f, (b & 0xFF) / 255f, (a & 0xFF) / 255f);
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

        public void texv(Vector2f vec) {
            texf(vec.x, vec.y);
        }

        public void texf(float u, float v) {
            uv[0] = u;
            uv[1] = v;
        }

        public Vector2f tex() {
            return new Vector2f(uv);
        }

        public void lightv(Vector2f vec) {
            lightf(vec.x, vec.y);
        }

        public void lightf(float block, float sky) {
            lighti((int) (block * 0xF), (int) (sky * 0xF));
        }

        public void lighti(int combined) {
            lighti(combined >> 4, combined >> 20);
        }

        public void lighti(int block, int sky) {
            light[0] = light(block);
            light[1] = light(sky);
        }

        public Vector2f light() {
            return new Vector2f(light);
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

        @Override
        public String toString() {
            return "\tVertex [\n\t\tposition=" + Arrays.toString(position) + ",\n\t\tnormal=" + Arrays.toString(normal) + ",\n\t\tcolour=" + Arrays
                    .toString(colour) + ",\n\t\tuv=" + Arrays.toString(uv) + ",\n\t\tlight=" + Arrays.toString(light) + "\n\t]";
        }
    }
}
