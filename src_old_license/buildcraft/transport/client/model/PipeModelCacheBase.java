package buildcraft.transport.client.model;

import java.util.*;

import javax.vecmath.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.Vec3d;

import buildcraft.core.lib.client.model.BCModelHelper;
import buildcraft.lib.client.model.IModelCache;
import buildcraft.lib.client.model.ModelCache;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.model.MutableVertex;
import buildcraft.lib.config.DetailedConfigOption;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.transport.BCTransportSprites;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeRenderState;
import buildcraft.transport.client.model.key.PipeModelKey;

public class PipeModelCacheBase {
    private static final DetailedConfigOption OPTION_INSIDE_COLOUR_MULT = new DetailedConfigOption("render.pipe.misc.inside.shade", "0.725");

    static final IModelCache<PipeBaseCutoutKey> cacheCutout;
    static final IModelCache<PipeBaseTransclucentKey> cacheTranslucent;

    private static final MutableQuad[][][] QUADS;
    private static final MutableQuad[][][] QUADS_COLOURED;

    static {
        cacheCutout = new ModelCache<>("pipe.base.cutout", PipeModelCacheBase::generateCutout);
        cacheTranslucent = new ModelCache<>("pipe.base.transclucent", PipeModelCacheBase::generateTranslucent);

        QUADS = new MutableQuad[2][][];
        QUADS_COLOURED = new MutableQuad[2][][];
        final double colourOffset = 0.01;
        Vec3d[] faceOffset = new Vec3d[6];
        for (EnumFacing face : EnumFacing.VALUES) {
            faceOffset[face.ordinal()] = new Vec3d(face.getOpposite().getDirectionVec()).scale(colourOffset);
        }

        // not connected
        QUADS[0] = new MutableQuad[6][2];
        QUADS_COLOURED[0] = new MutableQuad[6][2];
        Tuple3f center = new Point3f(0.5f, 0.5f, 0.5f);
        Tuple3f radius = new Vector3f(0.25f, 0.25f, 0.25f);
        float[] uvs = { 4 / 16f, 12 / 16f, 4 / 16f, 12 / 16f };
        for (EnumFacing face : EnumFacing.VALUES) {
            MutableQuad quad = BCModelHelper.createFace(face, center, radius, uvs);
            quad.setDiffuse(quad.getVertex(0).normal());
            QUADS[0][face.ordinal()][0] = quad;
            dupDarker(QUADS[0][face.ordinal()]);

            MutableQuad[] colQuads = BCModelHelper.createDoubleFace(face, center, radius, uvs);
            for (MutableQuad q : colQuads) {
                q.translatevd(faceOffset[face.ordinal()]);
            }
            QUADS_COLOURED[0][face.ordinal()] = colQuads;
        }

        int[][] uvsRot = {//
            { 2, 0, 3, 3 },//
            { 0, 2, 1, 1 },//
            { 2, 0, 0, 2 },//
            { 0, 2, 2, 0 },//
            { 3, 3, 0, 2 },//
            { 1, 1, 2, 0 } //
        };

        float[][] types = {//
            { 4, 12, 0, 4 },//
            { 4, 12, 12, 16 },//
            { 0, 4, 4, 12 },//
            { 12, 16, 4, 12 } //
        };

        for (float[] f2 : types) {
            for (int i = 0; i < f2.length; i++) {
                f2[i] /= 16f;
            }
        }
        // connected
        QUADS[1] = new MutableQuad[6][8];
        QUADS_COLOURED[1] = new MutableQuad[6][8];
        for (EnumFacing side : EnumFacing.VALUES) {
            center = new Point3f(//
                    side.getFrontOffsetX() * 0.375f,//
                    side.getFrontOffsetY() * 0.375f,//
                    side.getFrontOffsetZ() * 0.375f //
            );
            radius = new Vector3f(//
                    side.getAxis() == Axis.X ? 0.125f : 0.25f,//
                    side.getAxis() == Axis.Y ? 0.125f : 0.25f,//
                    side.getAxis() == Axis.Z ? 0.125f : 0.25f //
            );//
            center.add(new Point3f(0.5f, 0.5f, 0.5f));

            int i = 0;
            for (EnumFacing face : EnumFacing.VALUES) {
                if (face.getAxis() == side.getAxis()) continue;
                MutableQuad quad = BCModelHelper.createFace(face, center, radius, types[i]);
                quad.rotateTextureUp(uvsRot[side.ordinal()][i]);

                MutableQuad col = new MutableQuad(quad);

                quad.setDiffuse(quad.getVertex(0).normal());
                QUADS[1][side.ordinal()][i] = quad;

                col.translatevd(faceOffset[face.ordinal()]);
                QUADS_COLOURED[1][side.ordinal()][i++] = col;
            }
            dupDarker(QUADS[1][side.ordinal()]);
            dupInverted(QUADS_COLOURED[1][side.ordinal()]);
        }
    }

    private static void dupDarker(MutableQuad[] quads) {
        int halfLength = quads.length / 2;
        for (int i = 0; i < halfLength; i++) {
            int n = i + halfLength;
            MutableQuad from = quads[i];
            if (from != null) {
                MutableQuad to = new MutableQuad(from);
                to.invertNormal();
                to.setCalculatedDiffuse();
                for (MutableVertex v : to.verticies()) {
                    Point4f colour = v.colourv();
                    colour.scale(OPTION_INSIDE_COLOUR_MULT.getAsFloat());
                    colour.w = 1;
                    v.colourv(colour);
                }
                quads[n] = to;
            }
        }
    }

    private static void dupInverted(MutableQuad[] quads) {
        int halfLength = quads.length / 2;
        for (int i = 0; i < halfLength; i++) {
            int n = i + halfLength;
            MutableQuad from = quads[i];
            if (from != null) {
                MutableQuad to = new MutableQuad(from);
                to.invertNormal();
                quads[n] = to;
            }
        }
    }

    private static List<MutableQuad> generateCutout(PipeBaseCutoutKey key) {
        List<MutableQuad> quads = new ArrayList<>();

        for (EnumFacing face : EnumFacing.values()) {
            float size = key.connections[face.ordinal()];
            if (size > 0) {
                addQuads(QUADS[1][face.ordinal()], quads, key.sides[face.ordinal()]);
            } else {
                addQuads(QUADS[0][face.ordinal()], quads, key.center);
            }
        }
        return quads;
    }

    private static List<MutableQuad> generateTranslucent(PipeBaseTransclucentKey key) {
        if (!key.shouldRender()) return ImmutableList.of();
        List<MutableQuad> quads = new ArrayList<>();
        TextureAtlasSprite sprite = BCTransportSprites.PIPE_COLOUR.getSprite();

        for (EnumFacing face : EnumFacing.values()) {
            float size = key.connections[face.ordinal()];
            if (size > 0) {
                addQuads(QUADS_COLOURED[1][face.ordinal()], quads, sprite);
            } else {
                addQuads(QUADS_COLOURED[0][face.ordinal()], quads, sprite);
            }
        }
        int colour = 0xFF_00_00_00 | ColourUtil.swapArgbToAbgr(ColourUtil.getLightHex(key.colour));
        for (MutableQuad q : quads) {
            q.colouri(colour);
        }
        return quads;
    }

    private static void addQuads(MutableQuad[] from, List<MutableQuad> to, TextureAtlasSprite sprite) {
        for (MutableQuad f : from) {
            if (f == null) {
                continue;
            }
            MutableQuad copy = new MutableQuad(f);
            copy.setSprite(sprite);
            for (MutableVertex v : copy.verticies()) {
                Point2f tex = v.tex();
                v.texf(sprite.getInterpolatedU(tex.x * 16), sprite.getInterpolatedV(tex.y * 16));
            }
            to.add(copy);
        }
    }

    private static float[] computeConnections(PipeRenderState state) {
        float[] f = new float[6];
        for (EnumFacing face : EnumFacing.values()) {
            int i = face.ordinal();
            if (state.pipeConnectionMatrix.isConnected(face)) {
                if (state.pipeConnectionExtensions.isConnected(face)) {
                    f[i] = 0.25f + state.customConnections[i];
                } else {
                    f[i] = 0.25f;
                }
            } else {
                f[i] = -100;// Random negative number
            }
        }
        return f;
    }

    public static final class PipeBaseCutoutKey {
        public final TextureAtlasSprite center;
        public final TextureAtlasSprite[] sides;
        public final float[] connections;
        private final int hashCode;

        public PipeBaseCutoutKey(PipeModelKey key) {
            center = key.center;
            sides = key.sides;
            connections = new float[] {//
                key.connected[0] ? 1 : 0,//
                key.connected[1] ? 1 : 0,//
                key.connected[2] ? 1 : 0,//
                key.connected[3] ? 1 : 0,//
                key.connected[4] ? 1 : 0,//
                key.connected[5] ? 1 : 0,//
            };
            hashCode = Objects.hash(center, Arrays.hashCode(sides), Arrays.hashCode(connections));
        }

        public PipeBaseCutoutKey(Pipe<?> pipe, PipeRenderState render) {
            this(render, getSprites(pipe, render));
        }

        private static Map<EnumFacing, TextureAtlasSprite> getSprites(Pipe<?> pipe, PipeRenderState render) {
            Map<EnumFacing, TextureAtlasSprite> spriteMap = Maps.newHashMap();
            for (EnumFacing face : EnumFacing.values()) {
                spriteMap.put(face, pipe.getIconProvider().getIcon(render.textureMatrix.getTextureIndex(face)));
            }
            spriteMap.put(null, pipe.getIconProvider().getIcon(render.textureMatrix.getTextureIndex(null)));
            return spriteMap;
        }

        public PipeBaseCutoutKey(PipeRenderState render, Map<EnumFacing, TextureAtlasSprite> sprites) {
            center = sprites.get(null);
            sides = new TextureAtlasSprite[6];
            for (EnumFacing face : EnumFacing.values()) {
                sides[face.ordinal()] = sprites.get(face);
            }
            connections = computeConnections(render);
            hashCode = Objects.hash(center, Arrays.hashCode(sides), Arrays.hashCode(connections));
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            PipeBaseCutoutKey other = (PipeBaseCutoutKey) obj;
            if (center != other.center) return false;
            if (!Arrays.equals(connections, other.connections)) return false;
            if (!Arrays.equals(sides, other.sides)) return false;
            return true;
        }

        @Override
        public String toString() {
            return "PipeBaseCutoutKey [center=" + center.getIconName() + ", sides=" + sidesToString() + ", connections=" + Arrays.toString(connections) + "]";
        }

        private String sidesToString() {
            String s = "[";
            for (int i = 0; i < sides.length; i++) {
                if (i != 0) s += ", ";
                TextureAtlasSprite sprite = sides[i];
                if (sprite == null) {
                    s += "null";
                } else {
                    s += sprite.getIconName();
                }
            }
            return s + "]";
        }
    }

    public static final class PipeBaseTransclucentKey {
        public final EnumDyeColor colour;
        public final float[] connections;
        private final int hashCode;

        public PipeBaseTransclucentKey(PipeModelKey key) {
            this.colour = key.colour;
            if (colour == null) {
                connections = null;
                hashCode = 0;
            } else {
                connections = new float[] {//
                    key.connected[0] ? 1 : 0,//
                    key.connected[1] ? 1 : 0,//
                    key.connected[2] ? 1 : 0,//
                    key.connected[3] ? 1 : 0,//
                    key.connected[4] ? 1 : 0,//
                    key.connected[5] ? 1 : 0,//
                };
                hashCode = Objects.hash(colour, Arrays.hashCode(connections));
            }
        }

        public boolean shouldRender() {
            return colour != null;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (!(obj instanceof PipeBaseTransclucentKey)) return false;
            PipeBaseTransclucentKey other = (PipeBaseTransclucentKey) obj;
            /* If we don't have any translucency and neither does the other then we don't care what the other variables
             * are and are considered equal to the other one. */
            if (!shouldRender() && !other.shouldRender()) return true;
            if (!Arrays.equals(connections, other.connections)) return false;
            return colour == other.colour;
        }

        @Override
        public String toString() {
            return "PipeBaseTransclucentKey [colour=" + colour + ", connections=" + Arrays.toString(connections) + "]";
        }
    }
}
