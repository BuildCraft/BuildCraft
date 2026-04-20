/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.client.model.plug;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import ct.buildcraft.api.transport.pluggable.IPluggableStaticBaker;
import ct.buildcraft.api.transport.pluggable.PluggableModelKey;
import ct.buildcraft.lib.client.model.MutableQuad;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraftforge.client.model.data.ModelData;

/** An {@link IPluggableStaticBaker} that rotates a given model to the correct side, and returns the quads. */
public class PlugBakerSimple<K extends PluggableModelKey> implements IPluggableStaticBaker<K> {

    private final IQuadProvider provider;
    private final Map<Direction, List<BakedQuad>> cached = new EnumMap<>(Direction.class);
    private MutableQuad[] lastSeen;

    public PlugBakerSimple(IQuadProvider provider) {
        this.provider = provider;
    }
    
    public PlugBakerSimple(BakedModel model) {
    	this.provider = () -> {
    		ArrayList<MutableQuad> a = new ArrayList<MutableQuad>();
    		for(BakedQuad face : model.getQuads(null, null, RandomSource.create(), ModelData.EMPTY, RenderType.cutout()))
    			a.add(new MutableQuad().fromBakedBlock(face));
    		return a.toArray(MutableQuad[]::new);
    		};
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
