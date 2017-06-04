/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.model.plug;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import buildcraft.lib.client.model.ModelItemSimple;
import buildcraft.lib.client.model.MutableQuad;

import buildcraft.transport.BCTransportModels;
import buildcraft.transport.client.model.key.KeyPlugFacade;
import buildcraft.transport.item.ItemPluggableFacade;
import buildcraft.transport.plug.FacadeStateManager.FacadePhasedState;
import buildcraft.transport.plug.FacadeStateManager.FullFacadeInstance;

public enum ModelFacadeItem implements IBakedModel {
    INSTANCE;

    private static final LoadingCache<KeyPlugFacade, IBakedModel> cache = CacheBuilder.newBuilder()//
        .expireAfterAccess(1, TimeUnit.MINUTES)//
        .build(CacheLoader.from(key -> new ModelItemSimple(bakeForKey(key), ModelItemSimple.TRANSFORM_PLUG_AS_BLOCK)));

    public static void onModelBake() {
        cache.invalidateAll();
    }

    private static List<BakedQuad> bakeForKey(KeyPlugFacade key) {
        List<BakedQuad> quads = new ArrayList<>();
        for (MutableQuad quad : PlugBakerFacade.INSTANCE.bakeForKey(key)) {
            quads.add(quad.toBakedItem());
        }
        if (key.state.isFullBlock() && !key.isHollow) {
            for (MutableQuad quad : BCTransportModels.BLOCKER.getCutoutQuads()) {
                quads.add(quad.toBakedItem());
            }
        }
        return quads;
    }

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        return ImmutableList.of();
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
        return ModelItemSimple.TRANSFORM_PLUG_AS_BLOCK;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return FacadeOverride.FACADE_OVERRIDE;
    }

    public static class FacadeOverride extends ItemOverrideList {
        public static final FacadeOverride FACADE_OVERRIDE = new FacadeOverride();

        private FacadeOverride() {
            super(ImmutableList.of());
        }

        @Override
        public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity) {
            FullFacadeInstance inst = ItemPluggableFacade.getInstance(stack);
            FacadePhasedState state = inst.getCurrentStateForStack();
            return cache.getUnchecked(new KeyPlugFacade(BlockRenderLayer.TRANSLUCENT, EnumFacing.WEST, state.stateInfo.state, state.isHollow));
        }
    }
}
