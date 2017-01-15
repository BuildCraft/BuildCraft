package buildcraft.transport.client.model;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

import buildcraft.api.transport.neptune.PipeAPI;
import buildcraft.api.transport.neptune.PipeDefinition;

import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.ModelUtil.UvFaceData;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.transport.BCTransportSprites;
import buildcraft.transport.client.model.PipeModelCacheBase.PipeBaseCutoutKey;
import buildcraft.transport.client.model.PipeModelCacheBase.PipeBaseTransclucentKey;

public enum PipeBaseModelGenStandard implements IPipeBaseModelGen {
    INSTANCE;

    // Textures
    private static final Map<PipeDefinition, TextureAtlasSprite[]> sprites = new IdentityHashMap<>();

    @Override
    public void onTextureStitchPre(TextureMap map) {
        sprites.clear();
        for (PipeDefinition def : PipeAPI.pipeRegistry.getAllRegisteredPipes()) {
            TextureAtlasSprite[] array = new TextureAtlasSprite[def.textures.length];
            for (int i = 0; i < array.length; i++) {
                array[i] = map.registerSprite(new ResourceLocation(def.textures[i]));
            }
            sprites.put(def, array);
        }
    }

    @Override
    public TextureAtlasSprite getItemSprite(PipeDefinition def, int index) {
        return getSprite(sprites.get(def), index);
    }

    // Models
    private static final MutableQuad[][][] QUADS;
    private static final MutableQuad[][][] QUADS_COLOURED;

    static {
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
        UvFaceData uvs = new UvFaceData();
        uvs.uMin = uvs.vMin = 4;
        uvs.uMax = uvs.vMax = 12;
        for (EnumFacing face : EnumFacing.VALUES) {
            MutableQuad quad = ModelUtil.createFace(face, center, radius, uvs);
            quad.setDiffuse(quad.normalvf());
            QUADS[0][face.ordinal()][0] = quad;
            dupDarker(QUADS[0][face.ordinal()]);

            MutableQuad[] colQuads = ModelUtil.createDoubleFace(face, center, radius, uvs);
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

        UvFaceData[] types = {//
            new UvFaceData(4, 12, 0, 4),//
            new UvFaceData(4, 12, 12, 16),//
            new UvFaceData(0, 4, 4, 12),//
            new UvFaceData(12, 16, 4, 12) //
        };

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
                MutableQuad quad = ModelUtil.createFace(face, center, radius, types[i]);
                quad.rotateTextureUp(uvsRot[side.ordinal()][i]);

                MutableQuad col = new MutableQuad(quad);

                quad.setDiffuse(quad.normalvf());
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
        float mult = OPTION_INSIDE_COLOUR_MULT.getAsFloat();
        for (int i = 0; i < halfLength; i++) {
            int n = i + halfLength;
            MutableQuad from = quads[i];
            if (from != null) {
                MutableQuad to = from.copyAndInvertNormal();
                to.setCalculatedDiffuse();
                to.multColourd(mult);
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
                quads[n] = from.copyAndInvertNormal();
            }
        }
    }

    // Model Usage

    @Override
    public List<BakedQuad> generateCutout(PipeBaseCutoutKey key) {
        List<MutableQuad> quads = new ArrayList<>();

        TextureAtlasSprite[] spriteArray = sprites.get(key.definition);
        for (EnumFacing face : EnumFacing.values()) {
            float size = key.connections[face.ordinal()];
            if (size > 0) {
                addQuads(QUADS[1][face.ordinal()], quads, getSprite(spriteArray, key.sideSprites[face.ordinal()]));
            } else {
                addQuads(QUADS[0][face.ordinal()], quads, getSprite(spriteArray, key.centerSprite));
            }
        }
        List<BakedQuad> bakedQuads = new ArrayList<>();
        for (MutableQuad q : quads) {
            bakedQuads.add(q.toBakedBlock());
        }
        return ImmutableList.of();//bakedQuads;
    }

    private static TextureAtlasSprite getSprite(TextureAtlasSprite[] array, int index) {
        if (array == null || index < 0 || index >= array.length) {
            return Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
        }
        return array[index];
    }

    @Override
    public List<BakedQuad> generateTranslucent(PipeBaseTransclucentKey key) {
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
        List<BakedQuad> bakedQuads = new ArrayList<>();
        for (MutableQuad q : quads) {
            q.colouri(colour);
            bakedQuads.add(q.toBakedBlock());
        }
        return bakedQuads;
    }

    private static void addQuads(MutableQuad[] from, List<MutableQuad> to, TextureAtlasSprite sprite) {
        for (MutableQuad f : from) {
            if (f == null) {
                continue;
            }
            MutableQuad copy = new MutableQuad(f);
            copy.setSprite(sprite);
            copy.texFromSprite(sprite);
            to.add(copy);
        }
    }
}
