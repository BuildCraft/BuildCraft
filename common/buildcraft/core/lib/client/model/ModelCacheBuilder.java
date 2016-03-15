package buildcraft.core.lib.client.model;

import buildcraft.core.lib.client.model.ModelCache.IModelGenerator;

public class ModelCacheBuilder<K> {
    final String detailedName;
    final IModelGenerator<K> generator;
    int maxSize = 160;
    boolean keepMutable = true;
    boolean needGL = false;

    public ModelCacheBuilder(String detailedName, IModelGenerator<K> generator) {
        this.detailedName = detailedName;
        this.generator = generator;
    }

    public ModelCacheBuilder<K> setMaxSize(int maxSize) {
        this.maxSize = maxSize;
        return this;
    }

    /** Toggle whether mutable quad versions should be kept or not. This is useful for {@link #setNeedGL(boolean)} when
     * you never actually use the quads after they have been upoaded to the GPU */
    public ModelCacheBuilder<K> setKeepMutable(boolean keep) {
        this.keepMutable = keep;
        return this;
    }

    /** Toggle whether an openGL display list is generated from the cached model. Does not imply anything, but you don't
     * have to */
    public ModelCacheBuilder<K> setNeedGL(boolean need) {
        this.needGL = need;
        return this;
    }

    /** Builds a model cache from this builder. */
    public ModelCache<K> build() {
        return new ModelCache<>(this);
    }
}
