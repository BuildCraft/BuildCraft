/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.lib.client.model.plug;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.math.Direction;

import buildcraft.api.transport.pluggable.IPluggableStaticBaker;
import buildcraft.api.transport.pluggable.PluggableModelKey;

import buildcraft.lib.client.model.MutableQuad;

/** An {@link IPluggableStaticBaker} that rotates a given model to the correct side, and returns the quads. */
@Environment(EnvType.CLIENT)
public class PlugBakerSimple<K extends PluggableModelKey> implements IPluggableStaticBaker<K> {

    private final IQuadProvider provider;
    private final Map<Direction, List<BakedQuad>> cached = new EnumMap<>(Direction.class);
    private MutableQuad[] lastSeen;

    public PlugBakerSimple(IQuadProvider provider) {
        (this) = provider;
    }

    @Override
    public List<BakedQuad> bake(K key) {
        MutableQuad[] quads = provider.getCutoutQuads();
        if (quads != lastSeen) {
            cached.clear();
            MutableQuad copy = new MutableQuad();
            for (Direction to : Direction.values()) {
                List<BakedQuad> list = new ArrayList<>();
                for (MutableQuad q : quads) {
                    copy.copyFrom(q);
                    copy.rotate(Direction.WEST, to, 0.5f, 0.5f, 0.5f);
                    copy.multShade();
                    list.add(copy.toBakedBlock());
                }
                cached.put(to, list);
            }
            lastSeen = quads;
        }
        return cached.get(key.side);
    }

    public interface IQuadProvider {
        MutableQuad[] getCutoutQuads();
    }
}
