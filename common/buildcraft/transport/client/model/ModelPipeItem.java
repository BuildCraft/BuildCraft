/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.model;

import buildcraft.api.transport.pipe.EnumPipeColourType;
import buildcraft.api.transport.pipe.IItemPipe;
import buildcraft.api.transport.pipe.PipeDefinition;
import buildcraft.api.transport.pipe.PipeFaceTex;
import buildcraft.lib.client.model.ModelItemSimple;
import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.ModelUtil.UvFaceData;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.misc.SpriteUtil;
import buildcraft.transport.BCTransportSprites;
import com.google.common.collect.ImmutableList;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
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
            for (Direction face : Direction.VALUES) {
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
            for (Direction face : Direction.VALUES) {
                UvFaceData uvs = face.getAxis() == Axis.Y ? uvsY : uvsXZ;
                QUADS_COLOUR[face.ordinal()] = ModelUtil.createFace(face, center, radius, uvs);
            }
        }
    }

    @Override
    // public List<BakedQuad> getQuads(IBlockState state, Direction side, long rand)
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        return ImmutableList.of();
    }

    // private static List<BakedQuad> getQuads(PipeFaceTex center, PipeFaceTex top, PipeFaceTex bottom, TextureAtlasSprite[] sprites, int colour, EnumPipeColourType colourType)
    private static List<BakedQuad> getQuads(PipeFaceTex center, PipeFaceTex top, PipeFaceTex bottom, TextureAtlasSprite[] sprites, DyeColor rColour, EnumPipeColourType colourType) {
        // TEMP!
        top = center;
        bottom = center;

        List<BakedQuad> quads = new ArrayList<>();

        // if (center == top && center == bottom) {
        addQuads(QUADS_SAME, sprites, quads, center);
        // } else {
        // TODO: Differing sprite quads
        // }

//        if (colour > 0 && colour <= 16)
        if (rColour != null) {
//            DyeColor rColour = DyeColor.byId(colour - 1);
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

    private static void addQuads(MutableQuad[] from, TextureAtlasSprite[] sprites, List<BakedQuad> to, PipeFaceTex face) {
        MutableQuad copy = new MutableQuad();
        for (int i = 0; i < face.getCount(); i++) {
            int colour = face.getColour(i);
            int spriteIndex = face.getTexture(i);
            TextureAtlasSprite sprite = getSprite(sprites, spriteIndex);
            // Calen: when reloading resource packs, sprite may be null and cause NPE
            if (sprite == null) {
                continue;
            }
            for (MutableQuad f : from) {
                if (f == null) {
                    continue;
                }
                copy.copyFrom(f);
                copy.texFromSprite(sprite);
                copy.colouri(colour);
                to.add(copy.toBakedItem());
            }
        }
    }

    private static TextureAtlasSprite getSprite(TextureAtlasSprite[] sprites, int spriteIndex) {
        TextureAtlasSprite sprite;
        if (spriteIndex < 0 || spriteIndex >= sprites.length) {
            sprite = SpriteUtil.missingSprite().get();
        } else {
            sprite = sprites[spriteIndex];
        }
        return sprite;
    }

    private static void addQuadsColoured(MutableQuad[] from, List<BakedQuad> to, TextureAtlasSprite sprite, int colour) {
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
//    public boolean isAmbientOcclusion()
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
//    public boolean isBuiltInRenderer()
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
//    public TextureAtlasSprite getParticleTexture()
    public TextureAtlasSprite getParticleIcon() {
        return null;
    }

    @Override
//    public ItemCameraTransforms getItemCameraTransforms()
    public ItemTransforms getTransforms() {
//        return ItemCameraTransforms.DEFAULT;
        return ModelItemSimple.TRANSFORM_DEFAULT;
    }

    @Override
//    public ItemOverrideList getOverrides()
    public ItemOverrides getOverrides() {
        return PipeItemOverride.PIPE_OVERRIDE;
    }

    private static class PipeItemOverride extends ItemOverrides {
        public static final PipeItemOverride PIPE_OVERRIDE = new PipeItemOverride();

        public PipeItemOverride() {
//            super(rl,ImmutableList.of());
        }

        @Override
//        public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity)
        public BakedModel resolve(BakedModel originalModel, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int p_173469_) {
            Item item = stack.getItem();
            PipeFaceTex center = PipeFaceTex.NO_SPRITE;
            PipeFaceTex top = center;
            PipeFaceTex bottom = center;
            TextureAtlasSprite[] sprites = { SpriteUtil.missingSprite().get() };
            DyeColor colour = null;

            EnumPipeColourType type;
            if (item instanceof IItemPipe pipe) {
                PipeDefinition def = pipe.getDefinition();
                top = def.itemModelTop;
                center = def.itemModelCenter;
                bottom = def.itemModelBottom;
                type = def.getColourType();
                sprites = PipeModelCacheBase.generator.getItemSprites(def);
                colour = pipe.getColour();
            } else {
                type = EnumPipeColourType.TRANSLUCENT;
            }
            List<BakedQuad> quads = getQuads(center, top, bottom, sprites, colour, type);
            return new ModelItemSimple(quads, ModelItemSimple.TRANSFORM_BLOCK, true);
        }
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }
}
