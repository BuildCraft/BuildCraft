/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.model;

import buildcraft.lib.client.model.ModelCache.IModelGenerator;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.block.model.BakedQuad;

import java.util.ArrayList;
import java.util.List;

public class ModelCacheJoiner<K> implements IModelCache<K> {
    private final IModelCache<K> mainCache;
    private final ImmutableList<ModelKeyWrapper<K, ?>> modelKeyWrappers;

    public ModelCacheJoiner(List<ModelKeyWrapper<K, ?>> wrappers) {
        this.modelKeyWrappers = ImmutableList.copyOf(wrappers);
        this.mainCache = new ModelCache<>(this::load);
    }

    private List<BakedQuad> load(K key) {
        List<BakedQuad> quads = new ArrayList<>();
        for (ModelKeyWrapper<K, ?> wrapper : modelKeyWrappers) {
            quads.addAll(wrapper.getQuads(key));
        }
        return quads;
    }

    @Override
    public List<BakedQuad> bake(K key) {
        if (ModelCache.cacheJoined) {
            return mainCache.bake(key);
        } else {
            return load(key);
        }
    }

    @Override
    public void clear() {
        mainCache.clear();
        for (ModelKeyWrapper<K, ?> wrapper : modelKeyWrappers) {
            wrapper.cache.clear();
        }
    }

    public static class ModelKeyWrapper<K, T> {
        private final IModelKeyMapper<K, T> mapper;
        private final IModelCache<T> cache;

        public ModelKeyWrapper(IModelKeyMapper<K, T> mapper, IModelGenerator<T> generator) {
            this.mapper = mapper;
            this.cache = new ModelCache<>(generator);
        }

        public ModelKeyWrapper(IModelKeyMapper<K, T> mapper, IModelCache<T> cache) {
            this.mapper = mapper;
            this.cache = cache;
        }

        public List<BakedQuad> getQuads(K key) {
            return cache.bake(mapper.getInternKey(key));
        }
    }

    public interface IModelKeyMapper<F, T> {
        T getInternKey(F key);
    }
}
