package buildcraft.transport.client.model.plug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Matrix4f;

import net.minecraft.client.renderer.block.model.BakedQuad;

import buildcraft.api.transport.pluggable.IPluggableModelBaker;
import buildcraft.core.lib.utils.MatrixUtils;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.transport.BCTransportModels;
import buildcraft.transport.client.model.key.KeyPlugGate;
import buildcraft.transport.gate.EnumGateLogic;

public enum PlugGateBaker implements IPluggableModelBaker<KeyPlugGate> {
    INSTANCE;

    private static final Map<KeyPlugGate, List<BakedQuad>> cached = new HashMap<>();
    private static final MutableQuad[][] lastSeen = { null, null };

    @Override
    public List<BakedQuad> bake(KeyPlugGate key) {
        MutableQuad[][] quads = { BCTransportModels.GATE_AND.getCutoutQuads(), BCTransportModels.GATE_OR.getCutoutQuads() };
        if (quads[0] != lastSeen[0]) {
            cached.clear();
            lastSeen[0] = quads[0];
            lastSeen[1] = quads[1];
        }
        if (!cached.containsKey(key)) {
            List<BakedQuad> list = new ArrayList<>();
            Matrix4f transform = MatrixUtils.rotateTowardsFace(key.side);
            for (MutableQuad q : quads[key.variant.logic == EnumGateLogic.AND ? 0 : 1]) {
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
