/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.model;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

import buildcraft.api.core.BCLog;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeDefinition;

import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.ModelUtil.UvFaceData;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.sprite.AtlasSpriteVariants;
import buildcraft.lib.misc.ColourUtil;

import buildcraft.transport.BCTransportSprites;
import buildcraft.transport.client.model.PipeModelCacheBase.PipeBaseCutoutKey;
import buildcraft.transport.client.model.PipeModelCacheBase.PipeBaseTranslucentKey;

public enum PipeBaseModelGenStandard implements IPipeBaseModelGen {
    INSTANCE;

    // Textures
    private static final Map<PipeDefinition, TextureAtlasSprite[]> SPRITES = new IdentityHashMap<>();

    @Override
    public void onTextureStitchPre(TextureMap map) {
        SPRITES.clear();
        for (PipeDefinition def : PipeApi.pipeRegistry.getAllRegisteredPipes()) {
            TextureAtlasSprite[] array = new TextureAtlasSprite[def.textures.length];
            for (int i = 0; i < array.length; i++) {
                AtlasSpriteVariants sprite = AtlasSpriteVariants.createForConfig(new ResourceLocation(def.textures[i]));
                if (map.setTextureEntry(sprite)) {
                    array[i] = sprite;
                } else {
                    array[i] = map.getAtlasSprite(def.textures[i]);
                    BCLog.logger.warn("Couldn't override " + def.textures[i] + ", using existing sprite " + array[i].getClass());
                }
            }
            SPRITES.put(def, array);
        }
    }

    @Override
    public TextureAtlasSprite getItemSprite(PipeDefinition def, int index) {
        return getSprite(SPRITES.get(def), index);
    }

    // Models
    // whole for not connected
    private static final MutableQuad[][] NOT_CONNECTED_QUADS;
    private static final MutableQuad[][] NOT_CONNECTED_QUADS_COLOURED;
    // only part which is in pipe block (4/16) for connected, second = false in #getConnected
    private static final MutableQuad[][] CONNECTED_QUADS;
    private static final MutableQuad[][] CONNECTED_QUADS_COLOURED;
    // remaining parts, second = true in #getConnected
    private static final LoadingCache<Float, Pair<MutableQuad[][], MutableQuad[][]>> CONNECTED_QUADS_CACHE;

    static {
        Vec3d[] faceOffsets = getFaceOffsets();
        NOT_CONNECTED_QUADS = new MutableQuad[6][2];
        NOT_CONNECTED_QUADS_COLOURED = new MutableQuad[6][2];
        Tuple3f center = new Point3f(0.5f, 0.5f, 0.5f);
        Tuple3f radius = new Vector3f(0.25f, 0.25f, 0.25f);
        UvFaceData uvs = new UvFaceData();
        uvs.minU = uvs.minV = 4 / 16f;
        uvs.maxU = uvs.maxV = 12 / 16f;
        for (EnumFacing face : EnumFacing.VALUES) {
            MutableQuad quad = ModelUtil.createFace(face, center, radius, uvs);
            quad.setDiffuse(quad.normalvf());
            NOT_CONNECTED_QUADS[face.ordinal()][0] = quad;
            dupDarker(NOT_CONNECTED_QUADS[face.ordinal()]);
            MutableQuad[] colQuads = ModelUtil.createDoubleFace(face, center, radius, uvs);
            for (MutableQuad q : colQuads) {
                q.translatevd(faceOffsets[face.ordinal()]);
            }
            NOT_CONNECTED_QUADS_COLOURED[face.ordinal()] = colQuads;
        }

        CONNECTED_QUADS = getConnected(0.25f, false).getLeft();
        CONNECTED_QUADS_COLOURED = getConnected(0.25f, false).getRight();

        // noinspection ConstantConditions
        CONNECTED_QUADS_CACHE = CacheBuilder.newBuilder()
            .build(CacheLoader.from(size -> getConnected(size, true)));
    }

    private static Pair<MutableQuad[][], MutableQuad[][]> getConnected(float size, boolean second) {
        Vec3d[] faceOffsets = getFaceOffsets();

        int[][] uvsRot = { //
            { 2, 0, 3, 3 },//
            { 0, 2, 1, 1 },//
            { 2, 0, 0, 2 },//
            { 0, 2, 2, 0 },//
            { 3, 3, 0, 2 },//
            { 1, 1, 2, 0 } //
        };

        UvFaceData[] types;
        if (second) {
            int o = Math.round((size - 0.25f) * 16);
            types = new UvFaceData[] {//
                UvFaceData.from16(4, 16 - o, 12, 16),//
                UvFaceData.from16(4, 0, 12, o),//
                UvFaceData.from16(16 - o, 4, 16, 12),//
                UvFaceData.from16(0, 4, o, 12)//
            };
        } else {
            types = new UvFaceData[] {//
                UvFaceData.from16(4, 0, 12, 4),//
                UvFaceData.from16(4, 12, 12, 16),//
                UvFaceData.from16(0, 4, 4, 12),//
                UvFaceData.from16(12, 4, 16, 12)//
            };
        }

        MutableQuad[][] quads = new MutableQuad[6][8];
        MutableQuad[][] quadsColoured = new MutableQuad[6][8];
        for (EnumFacing side : EnumFacing.VALUES) {
            Tuple3f center, radius;
            if (second) {
                center = new Point3f(//
                    0.5f + side.getFrontOffsetX() * (0.5f + (size - 0.25f) / 2),//
                    0.5f + side.getFrontOffsetY() * (0.5f + (size - 0.25f) / 2),//
                    0.5f + side.getFrontOffsetZ() * (0.5f + (size - 0.25f) / 2) //
                );
                radius = new Vector3f(//
                    side.getAxis() == Axis.X ? (size - 0.25f) / 2 : 0.25f,//
                    side.getAxis() == Axis.Y ? (size - 0.25f) / 2 : 0.25f,//
                    side.getAxis() == Axis.Z ? (size - 0.25f) / 2 : 0.25f //
                );
            } else {
                center = new Point3f(//
                    0.5f + side.getFrontOffsetX() * 0.375f,//
                    0.5f + side.getFrontOffsetY() * 0.375f,//
                    0.5f + side.getFrontOffsetZ() * 0.375f //
                );
                radius = new Vector3f(//
                    side.getAxis() == Axis.X ? 0.125f : 0.25f,//
                    side.getAxis() == Axis.Y ? 0.125f : 0.25f,//
                    side.getAxis() == Axis.Z ? 0.125f : 0.25f //
                );
            }

            int i = 0;
            for (EnumFacing face : EnumFacing.VALUES) {
                if (face.getAxis() == side.getAxis()) continue;
                MutableQuad quad = ModelUtil.createFace(face, center, radius, types[i]);
                quad.rotateTextureUp(uvsRot[side.ordinal()][i]);

                MutableQuad col = new MutableQuad(quad);

                quad.setDiffuse(quad.normalvf());
                quads[side.ordinal()][i] = quad;

                col.translatevd(faceOffsets[face.ordinal()]);
                quadsColoured[side.ordinal()][i] = col;
                i++;
            }
            dupDarker(quads[side.ordinal()]);
            dupInverted(quadsColoured[side.ordinal()]);
        }

        return Pair.of(quads, quadsColoured);
    }

    @Nonnull
    private static Vec3d[] getFaceOffsets() {
        final double colourOffset = 0.01;
        Vec3d[] faceOffset = new Vec3d[6];
        for (EnumFacing face : EnumFacing.VALUES) {
            faceOffset[face.ordinal()] = new Vec3d(face.getOpposite().getDirectionVec()).scale(colourOffset);
        }
        return faceOffset;
    }

    private static void dupDarker(MutableQuad[] quads) {
        int halfLength = quads.length / 2;
        float multiplier = OPTION_INSIDE_COLOUR_MULT.getAsFloat();
        for (int i = 0; i < halfLength; i++) {
            int n = i + halfLength;
            MutableQuad from = quads[i];
            if (from != null) {
                MutableQuad to = from.copyAndInvertNormal();
                to.setCalculatedDiffuse();
                to.multColourd(multiplier);
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

        TextureAtlasSprite[] spriteArray = SPRITES.get(key.definition);
        for (EnumFacing face : EnumFacing.VALUES) {
            float size = key.connections[face.ordinal()];
            if (size > 0) {
                addQuads(CONNECTED_QUADS[face.ordinal()], quads, getSprite(spriteArray, key.sideSprites[face.ordinal()]));
                addQuads(CONNECTED_QUADS_CACHE.getUnchecked(size).getLeft()[face.ordinal()], quads, getSprite(spriteArray, key.sideSprites[face.ordinal()]));
            } else {
                addQuads(NOT_CONNECTED_QUADS[face.ordinal()], quads, getSprite(spriteArray, key.centerSprite));
            }
        }
        List<BakedQuad> bakedQuads = new ArrayList<>();
        for (MutableQuad q : quads) {
            bakedQuads.add(q.toBakedBlock());
        }
        return bakedQuads;
    }

    private static TextureAtlasSprite getSprite(TextureAtlasSprite[] array, int index) {
        if (array == null || index < 0 || index >= array.length) {
            return Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
        }
        return array[index];
    }

    @Override
    public List<BakedQuad> generateTranslucent(PipeBaseTranslucentKey key) {
        if (!key.shouldRender()) return ImmutableList.of();
        List<MutableQuad> quads = new ArrayList<>();
        TextureAtlasSprite sprite = BCTransportSprites.PIPE_COLOUR.getSprite();

        for (EnumFacing face : EnumFacing.VALUES) {
            // noinspection ConstantConditions
            float size = key.connections[face.ordinal()];
            if (size > 0) {
                addQuads(CONNECTED_QUADS_COLOURED[face.ordinal()], quads, sprite);
                addQuads(CONNECTED_QUADS_CACHE.getUnchecked(size).getRight()[face.ordinal()], quads, sprite);
            } else {
                addQuads(NOT_CONNECTED_QUADS_COLOURED[face.ordinal()], quads, sprite);
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
