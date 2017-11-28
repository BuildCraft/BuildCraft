/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.model;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.world.World;

import buildcraft.api.transport.pipe.IItemPipe;
import buildcraft.api.transport.pipe.PipeDefinition;

import buildcraft.lib.client.model.ModelItemSimple;
import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.ModelUtil.UvFaceData;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.misc.ColourUtil;

import buildcraft.transport.BCTransportSprites;

public enum ModelPipeItem implements IBakedModel {
    INSTANCE;

    private static final MutableQuad[] QUADS_SAME;
    // private static final MutableQuad[][] QUADS_DIFFERENT;
    private static final MutableQuad[] QUADS_COLOUR;

    static {
        // Same sprite for all 3 sections
        {
            QUADS_SAME = new MutableQuad[6];
            Tuple3f center = new Point3f(0.5f, 0.5f, 0.5f);
            Tuple3f radius = new Vector3f(0.25f, 0.5f, 0.25f);
            UvFaceData uvsY = UvFaceData.from16(4, 4, 12, 12);
            UvFaceData uvsXZ = UvFaceData.from16(4, 0, 12, 16);
            for (EnumFacing face : EnumFacing.VALUES) {
                UvFaceData uvs = face.getAxis() == Axis.Y ? uvsY : uvsXZ;
                QUADS_SAME[face.ordinal()] = ModelUtil.createFace(face, center, radius, uvs);
            }
        }

        // Different sprite for any of the 3 sections
        {
            // QUADS_DIFFERENT = new MutableQuad[3];
        }

        // Coloured pipes
        {
            QUADS_COLOUR = new MutableQuad[6];
            Tuple3f center = new Point3f(0.5f, 0.5f, 0.5f);
            Tuple3f radius = new Vector3f(0.24f, 0.49f, 0.24f);
            UvFaceData uvsY = UvFaceData.from16(4, 4, 12, 12);
            UvFaceData uvsXZ = UvFaceData.from16(4, 0, 12, 16);
            for (EnumFacing face : EnumFacing.VALUES) {
                UvFaceData uvs = face.getAxis() == Axis.Y ? uvsY : uvsXZ;
                QUADS_COLOUR[face.ordinal()] = ModelUtil.createFace(face, center, radius, uvs);
            }
        }
    }

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        return ImmutableList.of();
    }

    private static List<BakedQuad> getQuads(TextureAtlasSprite center, TextureAtlasSprite top, TextureAtlasSprite bottom, int colour) {
        // TEMP!
        top = center;
        bottom = center;

        List<BakedQuad> quads = new ArrayList<>();

        // if (center == top && center == bottom) {
        addQuads(QUADS_SAME, quads, center);
        // } else {
        // TODO: Differing sprite quads
        // }

        if (colour > 0 && colour <= 16) {
            EnumDyeColor rColour = EnumDyeColor.byMetadata(colour - 1);
            int rgb = 0xFF_00_00_00 | ColourUtil.swapArgbToAbgr(ColourUtil.getLightHex(rColour));
            TextureAtlasSprite sprite = BCTransportSprites.PIPE_COLOUR.getSprite();
            addQuadsColoured(QUADS_COLOUR, quads, sprite, rgb);
        }

        return quads;
    }

    private static void addQuads(MutableQuad[] from, List<BakedQuad> to, TextureAtlasSprite sprite) {
        for (MutableQuad f : from) {
            if (f == null) {
                continue;
            }
            to.add(new MutableQuad(f).texFromSprite(sprite).toBakedItem());
        }
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
    public boolean isAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return null;
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return ItemCameraTransforms.DEFAULT;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return PipeItemOverride.PIPE_OVERRIDE;
    }

    private static class PipeItemOverride extends ItemOverrideList {
        public static final PipeItemOverride PIPE_OVERRIDE = new PipeItemOverride();

        public PipeItemOverride() {
            super(ImmutableList.of());
        }

        @Override
        public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity) {
            Item item = stack.getItem();
            TextureAtlasSprite center = Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
            TextureAtlasSprite top = center;
            TextureAtlasSprite bottom = center;

            if (item instanceof IItemPipe) {
                PipeDefinition def = ((IItemPipe) item).getDefinition();
                top = PipeModelCacheBase.generator.getItemSprite(def, def.itemTextureTop);
                center = PipeModelCacheBase.generator.getItemSprite(def, def.itemTextureCenter);
                bottom = PipeModelCacheBase.generator.getItemSprite(def, def.itemTextureBottom);
            }
            List<BakedQuad> quads = getQuads(center, top, bottom, stack.getMetadata());
            return new ModelItemSimple(quads, ModelItemSimple.TRANSFORM_BLOCK, true);
        }
    }
}
