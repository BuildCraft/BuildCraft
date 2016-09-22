package buildcraft.transport.client.model;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.Maps;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;

import buildcraft.lib.client.model.IModelCache;
import buildcraft.lib.client.model.ModelCache;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeRenderState;
import buildcraft.transport.client.model.key.PipeModelKey;

public class PipeModelCacheBase {
    public static IPipeBaseModelGen generator = StandardPipeBaseModelGen.INSTANCE;

    static final IModelCache<PipeBaseCutoutKey> cacheCutout;
    static final IModelCache<PipeBaseTransclucentKey> cacheTranslucent;

    static {
        cacheCutout = new ModelCache<>("pipe.base.cutout", PipeModelCacheBase::generateCutout);
        cacheTranslucent = new ModelCache<>("pipe.base.transclucent", PipeModelCacheBase::generateTranslucent);
    }

    private static List<MutableQuad> generateCutout(PipeBaseCutoutKey key) {
        return generator.generateCutout(key);
    }

    private static List<MutableQuad> generateTranslucent(PipeBaseTransclucentKey key) {
        return generator.generateTranslucent(key);
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
