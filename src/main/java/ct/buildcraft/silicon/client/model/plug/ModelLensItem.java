/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.silicon.client.model.plug;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import ct.buildcraft.lib.client.model.ModelItemSimple;
import ct.buildcraft.lib.client.model.MutableQuad;
import ct.buildcraft.lib.misc.SpriteUtil;
import ct.buildcraft.silicon.BCSiliconModels;
import ct.buildcraft.silicon.item.ItemPluggableLens.LensData;
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

public enum ModelLensItem implements BakedModel {
    INSTANCE;

    private static final List<List<BakedQuad>> cached = new ArrayList<>(34);

    public static void onModelBake() {
        cached.clear();
    }

    private static List<BakedQuad> getQuads(int damage) {
        if (damage < 0 || damage >= 34) damage = 0;
        if (cached.isEmpty()) {
            for (int i = 0; i < 34; i++) {
                List<BakedQuad> list = new ArrayList<>();
                LensData data = new LensData(i);
                MutableQuad[] cutout, translucent;
                Direction side = Direction.WEST;
                if (data.isFilter) {
                    cutout = BCSiliconModels.getFilterCutoutQuads(side, data.colour);
                    translucent = BCSiliconModels.getFilterTranslucentQuads(side, data.colour);
                } else {
                    cutout = BCSiliconModels.getLensCutoutQuads(side, data.colour);
                    translucent = BCSiliconModels.getLensTranslucentQuads(side, data.colour);
                }
                for (MutableQuad q : cutout) {
                    list.add(q.toBakedItem());
                }
                for (MutableQuad q : translucent) {
                    list.add(q.toBakedItem());
                }
                cached.add(list);
            }
        }
        return cached.get(damage);
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
        return ModelItemSimple.TRANSFORM_PLUG_AS_ITEM;
    }

    @Override
    public ItemOverrides getOverrides() {
        return LensOverride.LENS_OVERRIDE;
    }

    public static class LensOverride extends ItemOverrides {
        public static final LensOverride LENS_OVERRIDE = new LensOverride();

        private LensOverride() {
            super();
        }

        @Override
        public BakedModel resolve(BakedModel originalModel, ItemStack stack, ClientLevel world, LivingEntity entity, int p) {
            return new ModelItemSimple(getQuads(stack.getDamageValue()), ModelItemSimple.TRANSFORM_PLUG_AS_ITEM, false);
        }
    }
}
