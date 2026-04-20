/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport.client.model;

import java.util.ArrayList;
import java.util.List;

import ct.buildcraft.api.transport.pipe.EnumPipeColourType;
import ct.buildcraft.api.transport.pipe.IItemPipe;
import ct.buildcraft.api.transport.pipe.PipeDefinition;
import ct.buildcraft.api.transport.pipe.PipeFaceTex;
import ct.buildcraft.lib.client.model.ModelItemSimple;
import ct.buildcraft.lib.client.model.ModelUtil;
import ct.buildcraft.lib.client.model.ModelUtil.UvFaceData;
import ct.buildcraft.lib.client.model.MutableQuad;
import ct.buildcraft.lib.misc.ColourUtil;
import ct.buildcraft.lib.misc.SpriteUtil;
import ct.buildcraft.transport.BCTransportSprites;
import com.google.common.collect.ImmutableList;
import com.mojang.math.Vector3f;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public enum ModelPipeItem implements BakedModel {
    INSTANCE;

    private static final MutableQuad[] QUADS_SAME;
    // private static final MutableQuad[][] QUADS_DIFFERENT;
    private static final MutableQuad[] QUADS_COLOUR;

    static {
        // Same sprite for all 3 sections
        {
            QUADS_SAME = new MutableQuad[6];
            Vector3f center = new Vector3f(0.5f, 0.5f, 0.5f);
            Vector3f radius = new Vector3f(0.25f, 0.5f, 0.25f);
            UvFaceData uvsY = UvFaceData.from16(4, 4, 12, 12);
            UvFaceData uvsXZ = UvFaceData.from16(4, 0, 12, 16);
            for (Direction face : Direction.values()) {
                UvFaceData uvs = face.getAxis() == Axis.Y ? uvsY : uvsXZ;
                QUADS_SAME[face.ordinal()] = ModelUtil.createFace(face, center, radius, uvs);
            }
        }

        // Different sprite for any of the 3 sections
        {
            // QUADS_DIFFERENT = new MutableQuad[3];
        }

        // Translucent Coloured pipes
        {
            QUADS_COLOUR = new MutableQuad[6];
            Vector3f center = new Vector3f(0.5f, 0.5f, 0.5f);
            Vector3f radius = new Vector3f(0.24f, 0.49f, 0.24f);
            UvFaceData uvsY = UvFaceData.from16(4, 4, 12, 12);
            UvFaceData uvsXZ = UvFaceData.from16(4, 0, 12, 16);
            for (Direction face : Direction.values()) {
                UvFaceData uvs = face.getAxis() == Axis.Y ? uvsY : uvsXZ;
                QUADS_COLOUR[face.ordinal()] = ModelUtil.createFace(face, center, radius, uvs);
            }
        }
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand) {
        return ImmutableList.of();
    }

    private static List<BakedQuad> getQuads(PipeFaceTex center, PipeFaceTex top, PipeFaceTex bottom,
        TextureAtlasSprite[] sprites, int colour, EnumPipeColourType colourType) {
        // TEMP!
        top = center;
        bottom = center;

        List<BakedQuad> quads = new ArrayList<>();

        // if (center == top && center == bottom) {
        addQuads(QUADS_SAME, sprites, quads, center);
        // } else {
        // TODO: Differing sprite quads
        // }

        if (colour > 0 && colour <= 16) {
            DyeColor rColour = DyeColor.byId(colour - 1);
            int rgb = 0xFF_00_00_00 | ColourUtil.swapArgbToAbgr(ColourUtil.getLightHex(rColour));
            if (colourType == EnumPipeColourType.TRANSLUCENT) {
                TextureAtlasSprite sprite = BCTransportSprites.PIPE_COLOUR.getSprite();
                addQuadsColoured(QUADS_COLOUR, quads, sprite, rgb);
            } else if (colourType == EnumPipeColourType.BORDER_OUTER) {
                TextureAtlasSprite sprite = BCTransportSprites.PIPE_COLOUR_BORDER_OUTER.getSprite();
                addQuadsColoured(QUADS_SAME, quads, sprite, rgb);
            } else if (colourType == EnumPipeColourType.BORDER_INNER) {
                TextureAtlasSprite sprite = BCTransportSprites.PIPE_COLOUR_BORDER_INNER.getSprite();
                addQuadsColoured(QUADS_SAME, quads, sprite, rgb);
            }
        }

        return quads;
    }

    private static void addQuads(MutableQuad[] from, TextureAtlasSprite[] sprites, List<BakedQuad> to,
        PipeFaceTex face) {
        MutableQuad copy = new MutableQuad();
        for (int i = 0; i < face.getCount(); i++) {
            int colour = face.getColour(i);
            int spriteIndex = face.getTexture(i);
            TextureAtlasSprite sprite = getSprite(sprites, spriteIndex);
            for (MutableQuad f : from) {
                if (f == null) {
                    continue;
                }
                copy.copyFrom(f);
                copy.texFromSprite(sprite);
                copy.normalf(0,0,0);//FIX Render bug
                to.add(copy.toBakedItem());
            }
        }
    }

    private static TextureAtlasSprite getSprite(TextureAtlasSprite[] sprites, int spriteIndex) {
        TextureAtlasSprite sprite;
        if (spriteIndex < 0 || spriteIndex >= sprites.length) {
            sprite = SpriteUtil.missingSprite();
        } else {
            sprite = sprites[spriteIndex];
        }
        return sprite;
    }

    private static void addQuadsColoured(MutableQuad[] from, List<BakedQuad> to, TextureAtlasSprite sprite,
        int colour) {
        for (MutableQuad f : from) {
            if (f == null) {
                continue;
            }
            MutableQuad copy = new MutableQuad(f);
            copy.texFromSprite(sprite);
            copy.colouri(colour);
            to.add(copy.toBakedItem());
        }
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return SpriteUtil.missingSprite();
    }

    @Override
    public ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }

    @Override
    public ItemOverrides getOverrides() {
        return PipeItemOverride.PIPE_OVERRIDE;
    }

    private static class PipeItemOverride extends ItemOverrides {
        public static final PipeItemOverride PIPE_OVERRIDE = new PipeItemOverride();

        public PipeItemOverride() {
            super();
        }

        @Override
        public BakedModel resolve(BakedModel originalModel, ItemStack stack, ClientLevel world,
            LivingEntity entity,int i) {
            Item item = stack.getItem();
            PipeFaceTex center = PipeFaceTex.NO_SPRITE;
            PipeFaceTex top = center;
            PipeFaceTex bottom = center;
            TextureAtlasSprite[] sprites = { SpriteUtil.missingSprite() };

            EnumPipeColourType type;
            if (item instanceof IItemPipe) {
                PipeDefinition def = ((IItemPipe) item).getDefinition();
                top = def.itemModelTop;
                center = def.itemModelCenter;
                bottom = def.itemModelBottom;
                type = def.getColourType();
                sprites = PipeModelCacheBase.generator.getItemSprites(def);
            } else {
                type = EnumPipeColourType.TRANSLUCENT;
            }
            List<BakedQuad> quads = getQuads(center, top, bottom, sprites,
            		stack.hasTag() ? (stack.getTag().contains("color")? stack.getTag().getInt("color") : 0) : 0, type);
            return new ModelItemSimple(quads, ModelItemSimple.TRANSFORM_BLOCK, true);
        }
    }

	@Override
	public boolean usesBlockLight() {
		return true;
	}
}
