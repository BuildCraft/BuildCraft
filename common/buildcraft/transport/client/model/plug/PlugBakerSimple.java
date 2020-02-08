package buildcraft.transport.client.model.plug;

import buildcraft.api.transport.pluggable.PluggableModelKey;

import buildcraft.lib.client.model.MutableQuad;

/** @deprecated Moved to BC lib. */
@Deprecated
public class PlugBakerSimple<K extends PluggableModelKey> extends buildcraft.lib.client.model.plug.PlugBakerSimple<K> {

    @Deprecated
    public PlugBakerSimple(IQuadProvider provider) {
        super(provider);
    }

    @Deprecated
    public interface IQuadProvider extends buildcraft.lib.client.model.plug.PlugBakerSimple.IQuadProvider {

        @Override
        MutableQuad[] getCutoutQuads();
    }
}
