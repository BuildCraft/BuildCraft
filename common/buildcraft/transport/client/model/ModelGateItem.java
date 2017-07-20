/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.model;

import buildcraft.lib.client.model.ModelItemSimple;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.misc.StackUtil;
import buildcraft.transport.BCTransportModels;
import buildcraft.transport.gate.GateVariant;
import buildcraft.transport.item.ItemPluggableGate;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum ModelGateItem implements IBakedModel {
    INSTANCE;

    private static final Map<GateVariant, List<BakedQuad>> cached = new HashMap<>();

    public static void onModelBake() {
        cached.clear();
    }

    private static List<BakedQuad> getQuads(GateVariant variant) {
        if (!cached.containsKey(variant)) {
            List<BakedQuad> list = new ArrayList<>();
            MutableQuad[] quads = BCTransportModels.getGateStaticQuads(EnumFacing.WEST, variant);
            for (MutableQuad q : quads) {
                list.add(q.toBakedItem());
            }
            for (MutableQuad q : BCTransportModels.GATE_DYNAMIC.getCutoutQuads()) {
                list.add(q.toBakedItem());
            }

            cached.put(variant, list);
        }
        return cached.get(variant);
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
        return ModelItemSimple.TRANSFORM_PLUG_AS_ITEM_BIGGER;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return GateOverride.GATE_OVERRIDE;
    }

    public static final class GateOverride extends ItemOverrideList {
        public static final GateOverride GATE_OVERRIDE = new GateOverride();

        private GateOverride() {
            super(ImmutableList.of());
        }

        @Override
        public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity) {
            GateVariant variant = ItemPluggableGate.getVariant(StackUtil.asNonNull(stack));
            return new ModelItemSimple(getQuads(variant), ModelItemSimple.TRANSFORM_PLUG_AS_ITEM_BIGGER);
        }
    }
}
