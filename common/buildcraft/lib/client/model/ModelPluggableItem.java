/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.lib.client.model;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;

@Environment(EnvType.CLIENT)
public class ModelPluggableItem implements BakedModel {

    private final List<BakedQuad> quads;

    public ModelPluggableItem(MutableQuad[]... quads) {
        ImmutableList.Builder<BakedQuad> list = ImmutableList.builder();
        for (MutableQuad[] qa : quads) {
            for (MutableQuad q : qa) {
                list.add(q.toBakedItem());
            }
        }
        this.quads = list.build();
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, Random random) {
        return side == null ? quads : ImmutableList.of();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean hasDepth() {
        return false;
    }

    @Override
    public boolean isSideLit() {
        return false;
    }

    @Override
    public boolean isBuiltin() {
        return false;
    }

    @Override
    public Sprite getParticleSprite() {
        return null;
    }

    @Override
    public ModelTransformation getTransformation() {
        return ModelItemSimple.TRANSFORM_PLUG_AS_ITEM != null
            ? ModelItemSimple.TRANSFORM_PLUG_AS_ITEM
            : ModelTransformation.NONE;
    }

    @Override
    public ModelOverrideList getOverrides() {
        return ModelOverrideList.EMPTY;
    }
}
