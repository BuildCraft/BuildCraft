/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.client.model;

import buildcraft.lib.client.model.ModelItemSimple;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.misc.StackUtil;
import buildcraft.silicon.BCSiliconModels;
import buildcraft.silicon.gate.GateVariant;
import buildcraft.silicon.item.ItemPluggableGate;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;

import javax.annotation.Nullable;
import java.util.*;

public enum ModelGateItem implements IBakedModel {
    INSTANCE;

    private static final Map<GateVariant, List<BakedQuad>> cached = new HashMap<>();

    public static void onModelBake() {
        cached.clear();
    }

    private static List<BakedQuad> getQuads(GateVariant variant) {
        if (!cached.containsKey(variant)) {
            List<BakedQuad> list = new ArrayList<>();
            MutableQuad[] quads = BCSiliconModels.getGateStaticQuads(Direction.WEST, variant);
            for (MutableQuad q : quads) {
                list.add(q.toBakedItem());
            }
            for (MutableQuad q : BCSiliconModels.GATE_DYNAMIC.getCutoutQuads()) {
                list.add(q.toBakedItem());
            }

            cached.put(variant, list);
        }
        return cached.get(variant);
    }

    @Override
//    public List<BakedQuad> getQuads(BlockState state, Direction side, long rand)
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) {
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
        return ModelItemSimple.TRANSFORM_PLUG_AS_ITEM_BIGGER;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return GateOverride.GATE_OVERRIDE;
    }

    public static final class GateOverride extends ItemOverrideList {
        public static final GateOverride GATE_OVERRIDE = new GateOverride();

        private GateOverride() {
//            super(ImmutableList.of());
        }

        @Override
//        public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, LivingEntity entity)
        public IBakedModel resolve(IBakedModel originalModel, ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity) {
            GateVariant variant = ItemPluggableGate.getVariant(StackUtil.asNonNull(stack));
            return new ModelItemSimple(getQuads(variant), ModelItemSimple.TRANSFORM_PLUG_AS_ITEM_BIGGER, false);
        }
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }
}
