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
import buildcraft.transport.client.model.key.KeyPlugBlocker;

public enum PlugBakerBlocker implements IPluggableModelBaker<KeyPlugBlocker> {
    INSTANCE;

    private static final Map<EnumFacing, List<BakedQuad>> cached = new EnumMap<>(EnumFacing.class);
    private static MutableQuad[] lastSeen = null;

    @Override
    public List<BakedQuad> bake(KeyPlugBlocker key) {
        MutableQuad[] quads = BCTransportModels.BLOCKER.getCutoutQuads();
        if (quads != lastSeen) {
            cached.clear();
            MutableQuad copy = new MutableQuad();
            for (EnumFacing to : EnumFacing.VALUES) {
                List<BakedQuad> list = new ArrayList<>();
                Matrix4f transform = MatrixUtil.rotateTowardsFace(to);
                for (MutableQuad q : quads) {
                    copy.copyFrom(q);
                    copy.transform(transform);
                    copy.multShade();
                    list.add(copy.toBakedBlock());
                }
                cached.put(to, list);
            }
            lastSeen = quads;
        }
        return cached.get(key.side);
    }
}
