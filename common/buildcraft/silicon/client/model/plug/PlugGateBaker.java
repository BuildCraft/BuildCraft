/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.client.model.plug;

import buildcraft.api.transport.pluggable.IPluggableStaticBaker;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.silicon.BCSiliconModels;
import buildcraft.silicon.client.model.key.KeyPlugGate;
import net.minecraft.client.renderer.block.model.BakedQuad;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum PlugGateBaker implements IPluggableStaticBaker<KeyPlugGate> {
    INSTANCE;

    private static final Map<KeyPlugGate, List<BakedQuad>> cached = new HashMap<>();

    public static void onModelBake() {
        cached.clear();
    }

    @Override
    public List<BakedQuad> bake(KeyPlugGate key) {
        if (!cached.containsKey(key)) {
            List<BakedQuad> list = new ArrayList<>();
            MutableQuad[] quads = BCSiliconModels.getGateStaticQuads(key.side, key.variant);
            for (MutableQuad q : quads) {
                MutableQuad c = new MutableQuad(q);
                c.multShade();
                list.add(c.toBakedBlock());
            }
            cached.put(key, list);
        }
        return cached.get(key);
    }
}
