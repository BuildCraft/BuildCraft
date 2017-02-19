package buildcraft.transport.client.model.plug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.pluggable.IPluggableStaticBaker;

import buildcraft.lib.client.model.MutableQuad;
import buildcraft.transport.BCTransportModels;
import buildcraft.transport.client.model.key.KeyPlugLens;

public enum PlugBakerLens implements IPluggableStaticBaker<KeyPlugLens> {
    INSTANCE;

    private static final Map<KeyPlugLens, List<BakedQuad>> cached = new HashMap<>();

    private static final IQuadGetter lensCutout = BCTransportModels::getLensCutoutQuads;
    private static final IQuadGetter lensTranslucent = BCTransportModels::getLensTranslucentQuads;
    private static final IQuadGetter filterCutout = BCTransportModels::getFilterCutoutQuads;
    private static final IQuadGetter filterTranslucent = BCTransportModels::getFilterTranslucentQuads;

    public static void onModelBake() {
        cached.clear();
    }

    private static IQuadGetter getGetter(KeyPlugLens key) {
        switch (key.layer) {
            case CUTOUT: {
                return key.isFilter ? filterCutout : lensCutout;
            }
            case TRANSLUCENT: {
                return key.isFilter ? filterTranslucent : lensTranslucent;
            }
            default: {
                throw new IllegalArgumentException("Unknown layer " + key.layer);
            }
        }
    }

    @Override
    public List<BakedQuad> bake(KeyPlugLens key) {
        if (!cached.containsKey(key)) {
            MutableQuad[] quads = getGetter(key).get(key.side, key.colour);
            MutableQuad c = new MutableQuad();
            List<BakedQuad> list = new ArrayList<>(quads.length);
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
