/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.model;

import buildcraft.transport.block.BlockPipeHolder;
import buildcraft.transport.client.model.PipeModelCacheAll.PipeAllCutoutKey;
import buildcraft.transport.client.model.PipeModelCacheAll.PipeAllTranslucentKey;
import buildcraft.transport.client.model.PipeModelCacheBase.PipeBaseCutoutKey;
import buildcraft.transport.client.model.key.PipeModelKey;
import buildcraft.transport.tile.TilePipeHolder;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.property.IExtendedBlockState;

import java.lang.ref.WeakReference;
import java.util.List;

public enum ModelPipe implements IBakedModel {
    INSTANCE;

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        if (side != null) {
            return ImmutableList.of();
        }

        TilePipeHolder tile = null;
        if (state instanceof IExtendedBlockState) {
            IExtendedBlockState ext = (IExtendedBlockState) state;
            WeakReference<TilePipeHolder> ref = ext.getValue(BlockPipeHolder.PROP_TILE);
            if (ref != null) {
                tile = ref.get();
            }
        }

        BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();

        if (tile == null || tile.getPipe() == null) {
            if (layer == BlockRenderLayer.TRANSLUCENT) {
                return ImmutableList.of();
            }
            return PipeModelCacheBase.cacheCutout.bake(new PipeBaseCutoutKey(PipeModelKey.DEFAULT_KEY));
        }

        if (layer == BlockRenderLayer.TRANSLUCENT) {
            PipeAllTranslucentKey realKey = new PipeAllTranslucentKey(tile);
            return PipeModelCacheAll.cacheTranslucent.bake(realKey);
        } else {
            PipeAllCutoutKey realKey = new PipeAllCutoutKey(tile);
            return PipeModelCacheAll.cacheCutout.bake(realKey);
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
        return Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return ItemCameraTransforms.DEFAULT;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }
}
