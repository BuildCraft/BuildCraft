package buildcraft.transport.client.model.plug;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Matrix4f;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.pluggable.IPluggableModelBaker;
import buildcraft.api.transport.pluggable.PluggableModelKey;

import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.misc.MatrixUtil;

/** An {@link IPluggableModelBaker} that rotates a given model to the correct side, and returns the quads. */
public class PlugBakerSimple<K extends PluggableModelKey<K>> implements IPluggableModelBaker<K> {

    private final IQuadProvider provider;
    private final Map<EnumFacing, List<BakedQuad>> cached = new EnumMap<>(EnumFacing.class);
    private MutableQuad[] lastSeen;

    public PlugBakerSimple(IQuadProvider provider) {
        this.provider = provider;
    }

    @Override
    public List<BakedQuad> bake(K key) {
        MutableQuad[] quads = provider.getCutoutQuads();
        if (quads != lastSeen) {
            cached.clear();
            MutableQuad copy = new MutableQuad();
            for (EnumFacing to : EnumFacing.VALUES) {
                List<BakedQuad> list = new ArrayList<>();
                // TODO: Replace with "MutableQuad.rotate(WEST, to)" as it will probably be faster
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

    public interface IQuadProvider {
        MutableQuad[] getCutoutQuads();
    }
}
