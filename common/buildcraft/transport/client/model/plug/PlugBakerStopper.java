package buildcraft.transport.client.model.plug;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Matrix4f;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.pluggable.IPluggableModelBaker;
import buildcraft.core.lib.utils.MatrixUtils;
import buildcraft.lib.client.model.CustomModelLoader.ModelHolder;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.transport.client.model.key.KeyPlugStopper;

public enum PlugBakerStopper implements IPluggableModelBaker<KeyPlugStopper> {
    INSTANCE;

    private static final ModelHolder MODEL_PLUG = new ModelHolder("buildcrafttransport:models/plugs/stopper.json");
    private static final Map<EnumFacing, List<BakedQuad>> cached = new EnumMap<>(EnumFacing.class);
    private static MutableQuad[] lastSeen = null;

    @Override
    public List<BakedQuad> bake(KeyPlugStopper key) {
        MutableQuad[] quads = MODEL_PLUG.getQuads();
        if (quads != lastSeen) {
            cached.clear();
            for (EnumFacing to : EnumFacing.VALUES) {
                List<BakedQuad> list = new ArrayList<>();
                Matrix4f transform = MatrixUtils.rotateTowardsFace(to);
                for (MutableQuad q : quads) {
                    MutableQuad c = new MutableQuad(q);
                    c.transform(transform);
                    list.add(c.toBakedBlock());
                }
                cached.put(to, list);
            }
            lastSeen = quads;
        }
        return cached.get(key.side);
    }
}
