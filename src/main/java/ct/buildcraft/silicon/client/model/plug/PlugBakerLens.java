/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.silicon.client.model.plug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ct.buildcraft.api.transport.pluggable.IPluggableStaticBaker;
import ct.buildcraft.lib.client.model.MutableQuad;
import ct.buildcraft.silicon.BCSiliconModels;
import ct.buildcraft.silicon.client.model.key.KeyPlugLens;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;

public enum PlugBakerLens implements IPluggableStaticBaker<KeyPlugLens> {
    INSTANCE;

    private static final Map<KeyPlugLens, List<BakedQuad>> cached = new HashMap<>();

    private static final IQuadGetter lensCutout = BCSiliconModels::getLensCutoutQuads;
    private static final IQuadGetter lensTranslucent = BCSiliconModels::getLensTranslucentQuads;
    private static final IQuadGetter filterCutout = BCSiliconModels::getFilterCutoutQuads;
    private static final IQuadGetter filterTranslucent = BCSiliconModels::getFilterTranslucentQuads;

    public static void onModelBake() {
        cached.clear();
    }

    private static IQuadGetter getGetter(KeyPlugLens key) {
    	if(key.layer == RenderType.cutout())
    		return key.isFilter ? filterCutout : lensCutout;
    	if(key.layer == RenderType.translucent())
    		return key.isFilter ? filterTranslucent : lensTranslucent;
        throw new IllegalArgumentException("Unknown layer " + key.layer);
    }

    @Override
    public List<BakedQuad> bake(KeyPlugLens key) {
        if (!cached.containsKey(key)) {
            MutableQuad[] quads = getGetter(key).get(key.side, key.colour);
            MutableQuad c = new MutableQuad();
            List<BakedQuad> list = new ArrayList<>(quads.length);
            for (MutableQuad q : quads) {
            	q.vertex_0.colour_a = 64;//TODO find a better way
            	q.vertex_1.colour_a = 64;
            	q.vertex_2.colour_a = 64;
            	q.vertex_3.colour_a = 64;
                c.copyFrom(q);
                c.multShade();
                list.add(c.toBakedBlock());
            }
            cached.put(key, list);
        }
        return cached.get(key);
    }

    interface IQuadGetter {
        MutableQuad[] get(Direction side, DyeColor colour);
    }
}
