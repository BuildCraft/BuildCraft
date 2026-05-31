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

/**
 * STUB(R.Chen): client model — Phase 5.
 *
 * The Forge original implemented {@code BakedModel} using {@code ItemCameraTransforms} /
 * {@code ItemTransformVec3f} (LWJGL Vector3f Euler angles). In Fabric 1.20.1 the equivalent is
 * {@code BakedModel} + {@code ModelTransformation} (JOML Quaternionf). Mapping Euler-angle
 * definitions to Quaternionf is non-trivial and is deferred to Phase 5. The static
 * {@code TRANSFORM_*} constants are exposed as {@code null} until then.
 */
@Environment(EnvType.CLIENT)
public class ModelItemSimple implements BakedModel {
    // STUB(R.Chen): Phase 5 — Euler-angle transforms → Quaternionf ModelTransformation.
    public static final ModelTransformation TRANSFORM_DEFAULT = ModelTransformation.NONE;
    public static final ModelTransformation TRANSFORM_BLOCK = null;
    public static final ModelTransformation TRANSFORM_PLUG_AS_ITEM = null;
    public static final ModelTransformation TRANSFORM_PLUG_AS_ITEM_BIGGER = null;
    public static final ModelTransformation TRANSFORM_PLUG_AS_BLOCK = null;
    public static final ModelTransformation TRANSFORM_ITEM = null;

    private final boolean isGui3d;
    private final List<BakedQuad> quads;
    private final Sprite particle;
    private final ModelTransformation transforms;

    public ModelItemSimple(List<BakedQuad> quads, ModelTransformation transforms, boolean isGui3d) {
        this.quads = quads == null ? ImmutableList.of() : quads;
        this.isGui3d = isGui3d;
        this.particle = this.quads.isEmpty() ? null : this.quads.get(0).getSprite();
        this.transforms = transforms != null ? transforms : ModelTransformation.NONE;
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
        return isGui3d;
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
        return particle;
    }

    @Override
    public ModelTransformation getTransformation() {
        return transforms;
    }

    @Override
    public ModelOverrideList getOverrides() {
        return ModelOverrideList.EMPTY;
    }
}
