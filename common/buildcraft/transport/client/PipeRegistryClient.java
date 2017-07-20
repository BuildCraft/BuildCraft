/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client;

import buildcraft.api.transport.pipe.IPipeBehaviourRenderer;
import buildcraft.api.transport.pipe.IPipeFlowRenderer;
import buildcraft.api.transport.pipe.PipeApiClient.IClientRegistry;
import buildcraft.api.transport.pipe.PipeBehaviour;
import buildcraft.api.transport.pipe.PipeFlow;
import buildcraft.api.transport.pluggable.IPlugDynamicRenderer;
import buildcraft.api.transport.pluggable.IPluggableStaticBaker;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableModelKey;

import java.util.HashMap;
import java.util.Map;

public enum PipeRegistryClient implements IClientRegistry {
    INSTANCE;

    private final Map<Class<?>, IPipeFlowRenderer<?>> flowRenderMap = new HashMap<>();
    private final Map<Class<?>, IPipeBehaviourRenderer<?>> behaviourRenderMap = new HashMap<>();
    private final Map<Class<?>, IPlugDynamicRenderer<?>> plugRenderMap = new HashMap<>();
    private final Map<Class<?>, IPluggableStaticBaker<?>> plugBakerMap = new HashMap<>();

    @Override
    public <F extends PipeFlow> void registerRenderer(Class<? extends F> flowClass, IPipeFlowRenderer<F> renderer) {
        flowRenderMap.put(flowClass, renderer);
    }

    @Override
    public <B extends PipeBehaviour> void registerRenderer(Class<? extends B> behaviourClass, IPipeBehaviourRenderer<B> renderer) {
        behaviourRenderMap.put(behaviourClass, renderer);
    }

    @Override
    public <P extends PipePluggable> void registerRenderer(Class<? extends P> plugClass, IPlugDynamicRenderer<P> renderer) {
        plugRenderMap.put(plugClass, renderer);
    }

    @Override
    public <P extends PluggableModelKey> void registerBaker(Class<? extends P> keyClass, IPluggableStaticBaker<P> renderer) {
        plugBakerMap.put(keyClass, renderer);
    }

    @SuppressWarnings("unchecked")
    public static <F extends PipeFlow> IPipeFlowRenderer<F> getFlowRenderer(F flow) {
        return (IPipeFlowRenderer<F>) INSTANCE.flowRenderMap.get(flow.getClass());
    }

    @SuppressWarnings("unchecked")
    public static <B extends PipeBehaviour> IPipeBehaviourRenderer<B> getBehaviourRenderer(B behaviour) {
        return (IPipeBehaviourRenderer<B>) INSTANCE.behaviourRenderMap.get(behaviour.getClass());
    }

    @SuppressWarnings("unchecked")
    public static <P extends PipePluggable> IPlugDynamicRenderer<P> getPlugRenderer(P plug) {
        return (IPlugDynamicRenderer<P>) INSTANCE.plugRenderMap.get(plug.getClass());
    }

    @SuppressWarnings("unchecked")
    public static <P extends PluggableModelKey> IPluggableStaticBaker<P> getPlugBaker(P key) {
        return (IPluggableStaticBaker<P>) INSTANCE.plugBakerMap.get(key.getClass());
    }
}
