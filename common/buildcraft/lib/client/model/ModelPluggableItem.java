/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ModelPluggableItem implements BakedModel {

    // private final List<BakedQuad> quads;
    private List<BakedQuad> quads;

    // public ModelPluggableItem(MutableQuad[]... quads)
    public ModelPluggableItem(Consumer<Runnable> consumerRunOnTextureStitchEvent$Post, LazyLoadedValue<MutableQuad[]>... lazyLoadedQuads) {
//        ImmutableList.Builder<BakedQuad> list = ImmutableList.builder();
//        for (MutableQuad[] qa : quads) {
//            for (MutableQuad q : qa) {
//                list.add(q.toBakedItem());
//            }
//        }
//        this.quads = list.build();

        consumerRunOnTextureStitchEvent$Post.accept(
                () -> {
                    ImmutableList.Builder<BakedQuad> list = ImmutableList.builder();

                    List<MutableQuad[]> quads = Arrays.stream(lazyLoadedQuads).map(LazyLoadedValue::get).toList();
                    for (MutableQuad[] qa : quads) {
                        for (MutableQuad q : qa) {
                            list.add(q.toBakedItem());
                        }
                    }
                    this.quads = list.build();
                }
        );
    }

    @Override
//    public List<BakedQuad> getQuads(BlockState state, Direction side, long rand)
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        return side == null ? quads : ImmutableList.of();
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
        return ModelItemSimple.TRANSFORM_PLUG_AS_ITEM;
    }

    @Override
//    public ItemOverrideList getOverrides()
    public ItemOverrides getOverrides() {
//        return ItemOverrideList.NONE;
        return ItemOverrides.EMPTY;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }
}

