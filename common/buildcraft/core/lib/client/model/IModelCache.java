package buildcraft.core.lib.client.model;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.vertex.VertexFormat;

public interface IModelCache<K> {
    void appendAsMutable(K key, List<MutableQuad> quads);

    ImmutableList<BakedQuad> bake(K key, VertexFormat format);

    void render(K key, WorldRenderer wr);
}
