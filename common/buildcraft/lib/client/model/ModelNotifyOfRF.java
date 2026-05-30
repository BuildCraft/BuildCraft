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

// TODO(R.Chen): Implement this — just a way to indicate that RF support is enabled.
@Environment(EnvType.CLIENT)
public class ModelNotifyOfRF implements BakedModel {

    final BakedModel parent;

    public ModelNotifyOfRF(BakedModel parent) {
        this.parent = parent;
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, Random random) {
        throw new UnsupportedOperationException("// TODO(R.Chen): Implement ModelNotifyOfRF");
    }

    @Override
    public boolean useAmbientOcclusion() {
        throw new UnsupportedOperationException("// TODO(R.Chen): Implement ModelNotifyOfRF");
    }

    @Override
    public boolean hasDepth() {
        throw new UnsupportedOperationException("// TODO(R.Chen): Implement ModelNotifyOfRF");
    }

    @Override
    public boolean isSideLit() {
        throw new UnsupportedOperationException("// TODO(R.Chen): Implement ModelNotifyOfRF");
    }

    @Override
    public boolean isBuiltin() {
        throw new UnsupportedOperationException("// TODO(R.Chen): Implement ModelNotifyOfRF");
    }

    @Override
    public Sprite getParticleSprite() {
        throw new UnsupportedOperationException("// TODO(R.Chen): Implement ModelNotifyOfRF");
    }

    @Override
    public ModelTransformation getTransformation() {
        return ModelTransformation.NONE;
    }

    @Override
    public ModelOverrideList getOverrides() {
        return ModelOverrideList.EMPTY;
    }
}
