package ct.buildcraft.api.transport.pipe;

import net.minecraftforge.api.distmarker.OnlyIn;

import ct.buildcraft.api.transport.pluggable.IPlugDynamicRenderer;
import ct.buildcraft.api.transport.pluggable.IPluggableStaticBaker;
import ct.buildcraft.api.transport.pluggable.PipePluggable;
import ct.buildcraft.api.transport.pluggable.PluggableModelKey;

import net.minecraftforge.api.distmarker.Dist;

@OnlyIn(Dist.CLIENT)
public enum PipeApiClient {
    INSTANCE;

    public static IClientRegistry registry;

    public interface IClientRegistry {

        /** Registers a dynamic renderer for the given pipe flow. Most {@link PipeFlow} types will have no use for
         * this. */
        <F extends PipeFlow> void registerRenderer(Class<? extends F> flowClass, IPipeFlowRenderer<F> renderer);

        /** Registers a dynamic renderer for the given pipe behaviour. Most {@link PipeBehaviour} types will have no use
         * for this. */
        <B extends PipeBehaviour> void registerRenderer(Class<? extends B> behaviourClass,
            IPipeBehaviourRenderer<B> renderer);

        <P extends PipePluggable> void registerRenderer(Class<? extends P> plugClass, IPlugDynamicRenderer<P> renderer);

        <P extends PluggableModelKey> void registerBaker(Class<? extends P> keyClass,
            IPluggableStaticBaker<P> renderer);
    }
}
