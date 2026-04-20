/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.client.model;

import java.util.List;

import com.google.common.collect.ImmutableList;

import ct.buildcraft.lib.misc.SpriteUtil;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public class ModelPluggableItem implements BakedModel {

    private final List<BakedQuad> quads;

    public ModelPluggableItem(MutableQuad[]... quads) {
        ImmutableList.Builder<BakedQuad> list = ImmutableList.builder();
        for (MutableQuad[] qa : quads) {
            for (MutableQuad q : qa) {
                list.add(q.toBakedBlock());
            }
        }
        this.quads = list.build();
    }
    
    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand) {
        return side == null ? quads : ImmutableList.of();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
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
        return quads.isEmpty() ? SpriteUtil.missingSprite() : quads.get(0).getSprite();
    }

    @Override
    public ItemTransforms getTransforms() {
        return ModelItemSimple.TRANSFORM_PLUG_AS_ITEM;
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

	@Override
	public boolean usesBlockLight() {
		return false;
	}


}
