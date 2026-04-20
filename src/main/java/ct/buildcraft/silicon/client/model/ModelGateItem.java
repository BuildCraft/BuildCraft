/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.silicon.client.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import ct.buildcraft.lib.client.model.ModelItemSimple;
import ct.buildcraft.lib.client.model.MutableQuad;
import ct.buildcraft.lib.misc.SpriteUtil;
import ct.buildcraft.lib.misc.StackUtil;
import ct.buildcraft.silicon.BCSiliconModels;
import ct.buildcraft.silicon.gate.GateVariant;
import ct.buildcraft.silicon.item.ItemPluggableGate;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public enum ModelGateItem implements BakedModel {
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
            	q.normald(0, 0, 0);//TODO check reason
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
    public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand) {
        return ImmutableList.of();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }
    
    public boolean usesBlockLight() {
    	return true;
    }

    @Override
    public boolean isGui3d() {
        return false;
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
        return ModelItemSimple.TRANSFORM_PLUG_AS_ITEM_BIGGER;
    }

    @Override
    public ItemOverrides getOverrides() {
        return GateOverride.GATE_OVERRIDE;
    }

    public static final class GateOverride extends ItemOverrides {
        public static final GateOverride GATE_OVERRIDE = new GateOverride();

        private GateOverride() {
            super();
        }

        @Override
        public BakedModel resolve(BakedModel originalModel, ItemStack stack, ClientLevel world, LivingEntity entity, int p) {
            GateVariant variant = ItemPluggableGate.getVariant(StackUtil.asNonNull(stack));
            return new ModelItemSimple(getQuads(variant), ModelItemSimple.TRANSFORM_PLUG_AS_ITEM_BIGGER, false);
        }
    }
}
