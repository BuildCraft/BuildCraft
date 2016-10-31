package buildcraft.transport.client.model.plug;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Matrix4f;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.pluggable.IPluggableModelBaker;

import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.misc.MatrixUtil;
import buildcraft.transport.BCTransportModels;
import buildcraft.transport.client.model.key.KeyPlugPulsar;

public enum PlugBakerPulsar implements IPluggableModelBaker<KeyPlugPulsar> {
    INSTANCE;

    private static final Map<EnumFacing, List<BakedQuad>> cached = new EnumMap<>(EnumFacing.class);
    private static MutableQuad[] lastSeen = null;

    @Override
    public List<BakedQuad> bake(KeyPlugPulsar key) {
        MutableQuad[] quads = BCTransportModels.PULSAR_STATIC.getCutoutQuads();
        if (quads != lastSeen) {
            cached.clear();
            for (EnumFacing to : EnumFacing.VALUES) {
                List<BakedQuad> list = new ArrayList<>();
                Matrix4f transform = MatrixUtil.rotateTowardsFace(to);
                for (MutableQuad q : quads) {
                    MutableQuad c = new MutableQuad(q);
                    c.transform(transform);
                    c.setCalculatedDiffuse();
                    list.add(c.toBakedBlock());
                }
                cached.put(to, list);
            }
            lastSeen = quads;
        }
        return cached.get(key.side);
    }
}
