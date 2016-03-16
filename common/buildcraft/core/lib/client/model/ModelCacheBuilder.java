package buildcraft.core.lib.client.model;

import net.minecraft.client.renderer.vertex.VertexFormat;

import buildcraft.core.lib.client.model.ModelCache.IModelGenerator;

public class ModelCacheBuilder<K> {
    final String detailedName;
    final IModelGenerator<K> generator;
    int maxSize = 1600;
    boolean keepMutable = true;
    boolean needGL = false;
    VertexFormat glVertexFormat;

    public ModelCacheBuilder(String detailedName, IModelGenerator<K> generator) {
        this.detailedName = detailedName;
        this.generator = generator;
    }

    public ModelCacheBuilder<K> setMaxSize(int maxSize) {
        this.maxSize = maxSize;
        return this;
    }

    /** Toggle whether mutable quad versions should be kept or not. This is useful for {@link #enableGL(boolean)} when
     * you never actually use the quads after they have been upoaded to the GPU */
    public ModelCacheBuilder<K> setKeepMutable(boolean keep) {
        this.keepMutable = keep;
        return this;
    }

    /** Toggle whether an openGL display list is generated from the cached model. Does not imply anything, but you don't
     * have to */
    public ModelCacheBuilder<K> enableGL(VertexFormat glVertexFormat) {
        needGL = true;
        this.glVertexFormat = glVertexFormat;
        return this;
    }

    public ModelCacheBuilder<K> disableGL() {
        needGL = false;
        glVertexFormat = null;
        return this;
    }

    /** Builds a model cache from this builder. */
    public ModelCache<K> build() {
        return new ModelCache<>(this);
    }
}
