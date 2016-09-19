package buildcraft.lib.client.model;

import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.vertex.VertexFormat;

public interface IModelCache<K> {
    void appendAsMutable(K key, List<MutableQuad> quads);

    List<BakedQuad> bake(K key, VertexFormat format);
}
