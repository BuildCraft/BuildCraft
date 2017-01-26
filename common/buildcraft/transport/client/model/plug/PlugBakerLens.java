package buildcraft.transport.client.model.plug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.pluggable.IPluggableModelBaker;

import buildcraft.lib.client.model.MutableQuad;
import buildcraft.transport.BCTransportModels;
import buildcraft.transport.client.model.key.KeyPlugLens;

public enum PlugBakerLens implements IPluggableModelBaker<KeyPlugLens> {
    CUTOUT(BCTransportModels::getLensCutoutQuads, BCTransportModels::getFilterCutoutQuads),
    TRANSLUCENT(BCTransportModels::getLensTranslucentQuads, BCTransportModels::getFilterTranslucentQuads);

    private static final Map<KeyPlugLens, List<BakedQuad>> cached = new HashMap<>();

    private final IQuadGetter lens, filter;

    private PlugBakerLens(IQuadGetter lens, IQuadGetter filter) {
        this.lens = lens;
        this.filter = filter;
    }

    public static void onModelBake() {
        cached.clear();
    }

    @Override
    public List<BakedQuad> bake(KeyPlugLens key) {
        if (!cached.containsKey(key)) {
            List<BakedQuad> list = new ArrayList<>();
            EnumDyeColor colour = key.colour;
            MutableQuad[] quads = key.isFilter ? filter.get(key.side, colour) : lens.get(key.side, colour);
            MutableQuad c = new MutableQuad();
            for (MutableQuad q : quads) {
                c.copyFrom(q);
                c.multShade();
                list.add(c.toBakedBlock());
            }
            cached.put(key, list);
        }
        return cached.get(key);
    }

    interface IQuadGetter {
        MutableQuad[] get(EnumFacing side, EnumDyeColor colour);
    }
}
