/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.silicon.client.model.plug;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;

import ct.buildcraft.api.BCModules;
import ct.buildcraft.lib.client.model.ModelItemSimple;
import ct.buildcraft.lib.client.model.MutableQuad;
import ct.buildcraft.lib.misc.SpriteUtil;
import ct.buildcraft.lib.world.SingleBlockAccess;
import ct.buildcraft.silicon.client.model.key.KeyPlugFacade;
import ct.buildcraft.silicon.item.ItemPluggableFacade;
import ct.buildcraft.silicon.plug.FacadeInstance;
import ct.buildcraft.silicon.plug.FacadePhasedState;
import ct.buildcraft.transport.BCTransportModels;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public enum ModelFacadeItem implements BakedModel {
    INSTANCE;

    private static final LoadingCache<KeyPlugFacade, BakedModel> cache = CacheBuilder.newBuilder()//
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

        if (BCModules.TRANSPORT.isLoaded() && key.state.isSolidRender(new SingleBlockAccess(key.state), BlockPos.ZERO) && !key.isHollow) {
            for (MutableQuad quad : BCTransportModels.BLOCKER.getCutoutQuads()) {
                quads.add(quad.toBakedItem());
            }
        }
        return quads;
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand) {
        return ImmutableList.of();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }
    
	@Override
	public boolean usesBlockLight() {
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
        return ModelItemSimple.TRANSFORM_PLUG_AS_BLOCK;
    }

    @Override
    public ItemOverrides getOverrides() {
        return FacadeOverride.FACADE_OVERRIDE;
    }

    public static class FacadeOverride extends ItemOverrides {
        public static final FacadeOverride FACADE_OVERRIDE = new FacadeOverride();

        private FacadeOverride() {
            super();
        }

        @Override
        public BakedModel resolve(BakedModel originalModel, ItemStack stack, ClientLevel world,
            LivingEntity entity, int p_173469_) {
            FacadeInstance inst = ItemPluggableFacade.getStates(stack);
            FacadePhasedState state = inst.getCurrentStateForStack();
            return cache.getUnchecked(
                new KeyPlugFacade(RenderType.translucent(), Direction.WEST, state.stateInfo.state, inst.isHollow));
        }
    }
}
