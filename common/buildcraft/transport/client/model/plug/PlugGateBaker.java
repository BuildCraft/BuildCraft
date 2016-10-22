package buildcraft.transport.client.model.plug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Matrix4f;

import net.minecraft.client.renderer.block.model.BakedQuad;

import buildcraft.api.transport.pluggable.IPluggableModelBaker;

import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.misc.MatrixUtil;
import buildcraft.transport.BCTransportModels;
import buildcraft.transport.client.model.key.KeyPlugGate;

public enum PlugGateBaker implements IPluggableModelBaker<KeyPlugGate> {
    INSTANCE;

    private static final Map<KeyPlugGate, List<BakedQuad>> cached = new HashMap<>();

    public static void onModelBake() {
        cached.clear();
    }

    @Override
    public List<BakedQuad> bake(KeyPlugGate key) {
        if (!cached.containsKey(key)) {
            List<BakedQuad> list = new ArrayList<>();
            Matrix4f transform = MatrixUtil.rotateTowardsFace(key.side);
            MutableQuad[] quads = BCTransportModels.getGateQuads(key.variant);
            for (MutableQuad q : quads) {
                MutableQuad c = new MutableQuad(q);
                c.transform(transform);
                c.setCalculatedDiffuse();
                list.add(c.toBakedBlock());
            }
            cached.put(key, list);
        }
        return cached.get(key);
    }
}
