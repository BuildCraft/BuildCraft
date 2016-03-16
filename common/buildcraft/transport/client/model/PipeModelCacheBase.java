package buildcraft.transport.client.model;

import java.util.*;

import javax.vecmath.Tuple3f;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.Vec3;

import buildcraft.core.CoreConstants;
import buildcraft.core.lib.EntityResizableCuboid;
import buildcraft.core.lib.client.model.BCModelHelper;
import buildcraft.core.lib.client.model.IModelCache;
import buildcraft.core.lib.client.model.ModelCache;
import buildcraft.core.lib.client.model.MutableQuad;
import buildcraft.core.lib.client.render.RenderResizableCuboid;
import buildcraft.core.lib.config.DetailedConfigOption;
import buildcraft.core.lib.utils.ColorUtils;
import buildcraft.core.lib.utils.Utils;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeRenderState;

public class PipeModelCacheBase {
    private static final DetailedConfigOption OPTION_INSIDE_COLOUR_MULT = new DetailedConfigOption("render.pipe.misc.inside.shade", "0.67");

    static final IModelCache<PipeBaseCutoutKey> cacheCutout;
    static final IModelCache<PipeBaseTransclucentKey> cacheTranslucent;

    static {
        cacheCutout = new ModelCache<>("pipe.base.cutout", PipeModelCacheBase::generateCutout);
        cacheTranslucent = new ModelCache<>("pipe.base.transclucent", PipeModelCacheBase::generateTranslucent);
    }

    private static List<MutableQuad> generateCutout(PipeBaseCutoutKey key) {
        TextureAtlasSprite center = key.center;
        float min = CoreConstants.PIPE_MIN_POS;
        float max = CoreConstants.PIPE_MAX_POS;

        float minUV = min * 16;
        float maxUV = max * 16;

        float[] centerUV = new float[4];
        centerUV[BCModelHelper.U_MIN] = center.getInterpolatedU(minUV);
        centerUV[BCModelHelper.U_MAX] = center.getInterpolatedU(maxUV);
        centerUV[BCModelHelper.V_MIN] = center.getInterpolatedV(minUV);
        centerUV[BCModelHelper.V_MAX] = center.getInterpolatedV(maxUV);

        List<MutableQuad> mutable = new ArrayList<>();
        List<MutableQuad> mutableIn = new ArrayList<>();

        for (EnumFacing face : EnumFacing.values()) {
            float size = key.connections[face.ordinal()];
            if (size <= 0) {
                renderPipeCenterFace(centerUV, face, false, mutable, mutableIn);
            } else {
                renderPipeConnection(size, face, key.sides[face.ordinal()], false, mutable, mutableIn);
            }
        }

        float mult = OPTION_INSIDE_COLOUR_MULT.getAsFloatCapped(0, 1);

        for (MutableQuad q : mutableIn) {
            q.colourf(mult, mult, mult, 1);
            mutable.add(q);
        }

        return mutable;
    }

    private static List<MutableQuad> generateTranslucent(PipeBaseTransclucentKey key) {
        if (!key.shouldRender()) return ImmutableList.of();
        TextureAtlasSprite sprite = PipeIconProvider.TYPE.PipeStainedOverlay.getIcon();
        float min = CoreConstants.PIPE_MIN_POS;
        float max = CoreConstants.PIPE_MAX_POS;

        float minUV = min * 16;
        float maxUV = max * 16;

        float[] centerUV = new float[4];
        centerUV[BCModelHelper.U_MIN] = sprite.getInterpolatedU(minUV);
        centerUV[BCModelHelper.U_MAX] = sprite.getInterpolatedU(maxUV);
        centerUV[BCModelHelper.V_MIN] = sprite.getInterpolatedV(minUV);
        centerUV[BCModelHelper.V_MAX] = sprite.getInterpolatedV(maxUV);

        List<MutableQuad> mutable = new ArrayList<>();
        List<MutableQuad> mutableIn = new ArrayList<>();

        for (EnumFacing face : EnumFacing.values()) {
            float size = key.connections[face.ordinal()];
            if (size <= 0) {
                renderPipeCenterFace(centerUV, face, true, mutable, mutableIn);
            } else {
                renderPipeConnection(size, face, sprite, true, mutable, mutableIn);
            }
        }
        float mult = OPTION_INSIDE_COLOUR_MULT.getAsFloatCapped(0, 1);
        for (MutableQuad q : mutableIn) {
            q.colourf(mult, mult, mult, 1);
            mutable.add(q);
        }
        int colour = ColorUtils.getRGBColor(key.colour);
        for (MutableQuad q : mutable) {
            q.setTint(colour);
        }
        return mutable;
    }

    private static void renderPipeCenterFace(float[] uvs, EnumFacing face, boolean smaller, List<MutableQuad> mutable, List<MutableQuad> mutableIn) {
        Vec3 radius = Utils.vec3(0.25);
        if (smaller) {
            double smallerValue = Utils.getValue(radius, face.getAxis()) - 0.01f;
            radius = Utils.withValue(radius, face.getAxis(), smallerValue);
        }

        Tuple3f center = Utils.vec3f(0.5f);
        Tuple3f radiusf = Utils.convertFloat(radius);

        BCModelHelper.appendQuads(mutable, BCModelHelper.createFace(face, center, radiusf, uvs));
        BCModelHelper.appendQuads(mutableIn, BCModelHelper.createInverseFace(face, center, radiusf, uvs).setFace(face.getOpposite()));
    }

    private static void renderPipeConnection(float extension, EnumFacing face, TextureAtlasSprite sprite, boolean smaller, List<MutableQuad> mutable,
            List<MutableQuad> mutableIn) {
        Vec3 actualCenter = Utils.convert(face, 0.25 + extension / 2).add(Utils.VEC_HALF);

        EnumFacing positive = Utils.convertPositive(face);

        Vec3 actualSize = Utils.convert(positive, extension);
        actualSize = actualSize.add(Utils.convertExcept(positive, 0.5));

        if (smaller) {
            // Decrease the entire size
            Vec3 allSmaller = actualSize.subtract(Utils.vec3(0.02));
            // Increase the size of axis the connection is in.
            actualSize = allSmaller.add(Utils.convert(positive, 0.02));
        }

        Vec3 pos = actualCenter.subtract(Utils.multiply(actualSize, 1 / 2d));

        EntityResizableCuboid cuboid = new EntityResizableCuboid(null);
        cuboid.texture = sprite;
        cuboid.makeClient();

        double start = face.getAxisDirection() == AxisDirection.POSITIVE ? 12 : 0;

        cuboid.textureStartX = face.getAxis() == Axis.X ? start : 4;
        cuboid.textureStartY = face.getAxis() == Axis.Y ? start : 4;
        cuboid.textureStartZ = face.getAxis() == Axis.Z ? start : 4;

        cuboid.textureSizeX = face.getAxis() == Axis.X ? 4 : 8;
        cuboid.textureSizeY = face.getAxis() == Axis.Y ? 4 : 8;
        cuboid.textureSizeZ = face.getAxis() == Axis.Z ? 4 : 8;

        cuboid.textures[face.ordinal()] = null;
        cuboid.textures[face.getOpposite().ordinal()] = null;

        cuboid.setSize(actualSize);
        cuboid.setPosition(pos.xCoord, pos.yCoord, pos.zCoord);

        RenderResizableCuboid.bakeCube(mutable, cuboid, true, false);
        RenderResizableCuboid.bakeCube(mutableIn, cuboid, false, true);
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
            return "PipeBaseCutoutKey [center=" + center.getIconName() + ", sides=" + sidesToString() + ", connections=" + Arrays.toString(
                    connections) + "]";
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
        public final byte colour;
        public final float[] connections;
        private final int hashCode;

        public PipeBaseTransclucentKey(PipeRenderState render) {
            this.colour = render.getGlassColor();
            if (shouldRender()) {
                connections = computeConnections(render);
                hashCode = Objects.hash(colour, Arrays.hashCode(connections));
            } else {
                /* If we don't have any translucency then set our hash code to 0. We don't care what the other variables
                 * are, we will never render anything */
                hashCode = 0;
                // Will never be used
                connections = null;
            }
        }

        public boolean shouldRender() {
            return colour >= 0 && colour < 16;
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
