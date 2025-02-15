/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.model;

import buildcraft.api.transport.pipe.EnumPipeColourType;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeDefinition;
import buildcraft.api.transport.pipe.PipeFaceTex;
import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.ModelUtil.UvFaceData;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.transport.BCTransportSprites;
import buildcraft.transport.client.model.PipeModelCacheBase.PipeBaseCutoutKey;
import buildcraft.transport.client.model.PipeModelCacheBase.PipeBaseTranslucentKey;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.TextureStitchEvent;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public enum PipeBaseModelGenStandard implements IPipeBaseModelGen {
    INSTANCE;

    // Textures
    private static final Map<PipeDefinition, TextureAtlasSprite[]> SPRITES = new IdentityHashMap<>();

    @Override
//    public void onTextureStitchPre(TextureStitchEvent.Pre event)
    public void onTextureStitchPre() {
        SPRITES.clear();
        for (PipeDefinition def : PipeApi.pipeRegistry.getAllRegisteredPipes()) {
            TextureAtlasSprite[] array = new TextureAtlasSprite[def.textures.length];
//            for (int i = 0; i < array.length; i++) {
//                String name = def.textures[i];
//                event.addSprite(new ResourceLocation(name));
//            }
            SPRITES.put(def, array);
        }
    }

    // Calen 1.20.1
    @Override
    public void onDatagenTextureRegister(Consumer<ResourceLocation> consumer) {
        for (PipeDefinition def : PipeApi.pipeRegistry.getAllRegisteredPipes()) {
            for (int i = 0; i < def.textures.length; i++) {
                String name = def.textures[i];
                consumer.accept(new ResourceLocation(name));
            }
        }
    }

    @Override
    public void onTextureStitchPost(TextureStitchEvent.Post event) {
        if (!event.getAtlas().location().equals(TextureAtlas.LOCATION_BLOCKS)) {
            return;
        }
        Map<String, TextureAtlasSprite> sprites = new HashMap<>();
        for (PipeDefinition def : PipeApi.pipeRegistry.getAllRegisteredPipes()) {
            TextureAtlasSprite[] array = SPRITES.get(def);
            for (int i = 0; i < array.length; i++) {
                String name = def.textures[i];
                TextureAtlasSprite sprite = sprites.get(name);
                if (sprite == null) {
                    sprite = event.getAtlas().getSprite(new ResourceLocation(name));
                    sprites.put(name, sprite);
                }
                array[i] = sprite;
            }
        }
    }

    @Override
    public TextureAtlasSprite[] getItemSprites(PipeDefinition def) {
        return SPRITES.get(def);
    }

    // Models
    private static final MutableQuad[][][] QUADS;
    private static final MutableQuad[][][] QUADS_COLOURED;

    static {
        QUADS = new MutableQuad[2][][];
        QUADS_COLOURED = new MutableQuad[2][][];
        final double colourOffset = 0.01;
        Vec3[] faceOffset = new Vec3[6];
        for (Direction face : Direction.VALUES) {
            faceOffset[face.ordinal()] = Vec3.atLowerCornerOf(face.getOpposite().getNormal()).scale(colourOffset);
        }

        // not connected
        QUADS[0] = new MutableQuad[6][2];
        QUADS_COLOURED[0] = new MutableQuad[6][2];
        Vector3f center = new Vector3f(0.5f, 0.5f, 0.5f);
        Vector3f radius = new Vector3f(0.25f, 0.25f, 0.25f);
        UvFaceData uvs = new UvFaceData();
        uvs.minU = uvs.minV = 4 / 16f;
        uvs.maxU = uvs.maxV = 12 / 16f;
        for (Direction face : Direction.VALUES) {
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

        int[][] uvsRot = { //
                { 2, 0, 3, 3 }, //
                { 0, 2, 1, 1 }, //
                { 2, 0, 0, 2 }, //
                { 0, 2, 2, 0 }, //
                { 3, 3, 0, 2 }, //
                { 1, 1, 2, 0 } //
        };

        UvFaceData[] types = { //
                UvFaceData.from16(4, 0, 12, 4), //
                UvFaceData.from16(4, 12, 12, 16), //
                UvFaceData.from16(0, 4, 4, 12), //
                UvFaceData.from16(12, 4, 16, 12) //
        };

        // connected
        QUADS[1] = new MutableQuad[6][8];
        QUADS_COLOURED[1] = new MutableQuad[6][8];
        for (Direction side : Direction.VALUES) {
            center = new Vector3f(//
                    side.getStepX() * 0.375f, //
                    side.getStepY() * 0.375f, //
                    side.getStepZ() * 0.375f //
            );
            radius = new Vector3f(//
                    side.getAxis() == Axis.X ? 0.125f : 0.25f, //
                    side.getAxis() == Axis.Y ? 0.125f : 0.25f, //
                    side.getAxis() == Axis.Z ? 0.125f : 0.25f //
            );//
            center.add(new Vector3f(0.5f, 0.5f, 0.5f));

            int i = 0;
            for (Direction face : Direction.VALUES) {
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

        TextureAtlasSprite[] spriteArray = SPRITES.get(key.definition);
        TextureAtlasSprite borderSprite = getBorderSprite(key);
        int colour = borderSprite == null ? -1 : getPipeModelColour(key.colour);
        int border_r = (colour >> 0) & 0xFF;
        int border_g = (colour >> 8) & 0xFF;
        int border_b = (colour >> 16) & 0xFF;
        for (Direction face : Direction.VALUES) {
            float size = key.connections[face.ordinal()];
            PipeFaceTex tex = size > 0 ? key.sideSprites[face.ordinal()] : key.centerSprite;
            int quadsIndex = size > 0 ? 1 : 0;
            MutableQuad[] quadArray = QUADS[quadsIndex][face.ordinal()];

            int startIndex = quads.size();

            for (int i = 0; i < tex.getCount(); i++) {
                addQuads(quadArray, quads, getSprite(spriteArray, tex, i));

                int c = tex.getColour(i);
                int r = (c >> 0) & 0xFF;
                int g = (c >> 8) & 0xFF;
                int b = (c >> 16) & 0xFF;

                for (int q = startIndex; q < quads.size(); q++) {
                    quads.get(q).multColouri(r, g, b, 0xFF);
                }

                startIndex = quads.size();
            }

            if (borderSprite != null) {

                addQuads(quadArray, quads, borderSprite);

                for (int i = startIndex; i < quads.size(); i++) {
                    quads.get(i).multColouri(border_r, border_g, border_b, 0xFF);
                }
            }
        }
        List<BakedQuad> bakedQuads = new ArrayList<>();
        for (MutableQuad q : quads) {
            bakedQuads.add(q.toBakedBlock());
        }
        return bakedQuads;
    }

    @Nullable
    private static TextureAtlasSprite getBorderSprite(PipeBaseCutoutKey key) {
        if (key.colour == null) {
            return null;
        }
        if (key.colourType == EnumPipeColourType.BORDER_INNER) {
            return BCTransportSprites.PIPE_COLOUR_BORDER_INNER.getSprite();
        }
        if (key.colourType == EnumPipeColourType.BORDER_OUTER) {
            return BCTransportSprites.PIPE_COLOUR_BORDER_OUTER.getSprite();
        }
        return null;
    }

    private static TextureAtlasSprite getSprite(TextureAtlasSprite[] array, PipeFaceTex tex, int spriteIndex) {
        int index = tex.getTexture(spriteIndex);
        return getSprite(array, index);
    }

    private static TextureAtlasSprite getSprite(TextureAtlasSprite[] array, int index) {
        if (array == null || index < 0 || index >= array.length) {
//            return Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
            return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(MissingTextureAtlasSprite.getLocation());
        }
        return array[index];
    }

    @Override
    public List<BakedQuad> generateTranslucent(PipeBaseTranslucentKey key) {
        if (!key.shouldRender()) return ImmutableList.of();
        List<MutableQuad> quads = new ArrayList<>();
        TextureAtlasSprite sprite = BCTransportSprites.PIPE_COLOUR.getSprite();

        for (Direction face : Direction.VALUES) {
            float size = key.connections[face.ordinal()];
            if (size > 0) {
                addQuads(QUADS_COLOURED[1][face.ordinal()], quads, sprite);
            } else {
                addQuads(QUADS_COLOURED[0][face.ordinal()], quads, sprite);
            }
        }
        int colour = getPipeModelColour(key.colour);
        List<BakedQuad> bakedQuads = new ArrayList<>();
        for (MutableQuad q : quads) {
            q.colouri(colour);
            bakedQuads.add(q.toBakedBlock());
        }
        return bakedQuads;
    }

    private static int getPipeModelColour(DyeColor c) {
        return 0xFF_00_00_00 | ColourUtil.swapArgbToAbgr(ColourUtil.getLightHex(c));
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
