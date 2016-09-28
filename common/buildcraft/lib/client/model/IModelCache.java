package buildcraft.lib.client.model;

import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;

public interface IModelCache<K> {
    List<BakedQuad> bake(K key);

    /** Clears all cached models. */
    void clear();
}
