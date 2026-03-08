/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.client.model.plug;

import buildcraft.api.BCModules;
import buildcraft.lib.client.model.ModelItemSimple;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.silicon.client.model.key.KeyPlugFacade;
import buildcraft.silicon.item.ItemPluggableFacade;
import buildcraft.silicon.plug.FacadeInstance;
import buildcraft.silicon.plug.FacadePhasedState;
import buildcraft.transport.BCTransportModels;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.EmptyBlockReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public enum ModelFacadeItem implements IBakedModel {
    INSTANCE;

    private static final LoadingCache<KeyPlugFacade, IBakedModel> cache = CacheBuilder.newBuilder()//
            .expireAfterAccess(1, TimeUnit.MINUTES)//
            .build(CacheLoader.from(key -> new ModelItemSimple(bakeForKey(key), ModelItemSimple.TRANSFORM_PLUG_AS_BLOCK, false)));

    public static void onModelBake() {
        cache.invalidateAll();
    }

    private static List<BakedQuad> bakeForKey(KeyPlugFacade key) {
        List<BakedQuad> quads = new ArrayList<>();
        for (MutableQuad quad : PlugBakerFacade.INSTANCE.bakeForKey(key)) {
            quads.add(quad.toBakedItem());
        }

//        if (BCModules.TRANSPORT.isLoaded() && key.state.isFullBlock() && !key.isHollow)
        if (BCModules.TRANSPORT.isLoaded() && key.state.getShape(EmptyBlockReader.INSTANCE, BlockPos.ZERO) == VoxelShapes.block() && !key.isHollow) {
            for (MutableQuad quad : BCTransportModels.BLOCKER.getCutoutQuads()) {
                quads.add(quad.toBakedItem());
            }
        }
        return quads;
    }

    @Nonnull
    @Override
//    public List<BakedQuad> getQuads(BlockState state, Direction side, long rand)
    public List<BakedQuad> getQuads(@Nullable BlockState p_119123_, @Nullable Direction p_119124_, Random rand) {
        return ImmutableList.of();
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
    public ItemCameraTransforms getTransforms() {
        return ModelItemSimple.TRANSFORM_PLUG_AS_BLOCK;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return FacadeOverride.FACADE_OVERRIDE;
    }

    public static class FacadeOverride extends ItemOverrideList {
        public static final FacadeOverride FACADE_OVERRIDE = new FacadeOverride();

        private FacadeOverride() {
//            super(ImmutableList.of());
        }

        @Override
//        public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, LivingEntity entity)
        public IBakedModel resolve(IBakedModel originalModel, ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity) {
            FacadeInstance inst = ItemPluggableFacade.getStates(stack);
            FacadePhasedState state = inst.getCurrentStateForStack();
            return cache.getUnchecked(
                    new KeyPlugFacade(RenderType.translucent(), Direction.WEST, state.stateInfo.state, inst.isHollow));
        }
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }
}
