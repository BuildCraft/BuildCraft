/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport.client;

import java.util.HashMap;
import java.util.Map;

import ct.buildcraft.api.transport.pipe.IPipeBehaviourBaker;
import ct.buildcraft.api.transport.pipe.IPipeBehaviourRenderer;
import ct.buildcraft.api.transport.pipe.IPipeFlowBaker;
import ct.buildcraft.api.transport.pipe.IPipeFlowRenderer;
import ct.buildcraft.api.transport.pipe.PipeApiClient.IClientRegistry;
import ct.buildcraft.api.transport.pipe.PipeBehaviour;
import ct.buildcraft.api.transport.pipe.PipeFlow;
import ct.buildcraft.api.transport.pluggable.IPlugDynamicRenderer;
import ct.buildcraft.api.transport.pluggable.IPluggableStaticBaker;
import ct.buildcraft.api.transport.pluggable.PipePluggable;
import ct.buildcraft.api.transport.pluggable.PluggableModelKey;

public enum PipeRegistryClient implements IClientRegistry {
    INSTANCE;

    private final Map<Class<?>, IPipeFlowRenderer<?>> flowRenderMap = new HashMap<>();
    private final Map<Class<?>, IPipeBehaviourRenderer<?>> behaviourRenderMap = new HashMap<>();
    private final Map<Class<?>, IPlugDynamicRenderer<?>> plugRenderMap = new HashMap<>();
    private final Map<Class<?>, IPipeFlowBaker<?>> flowBakerMap = new HashMap<>();
    private final Map<Class<?>, IPipeBehaviourBaker<?>> behaviourBakerMap = new HashMap<>();
    private final Map<Class<?>, IPluggableStaticBaker<?>> plugBakerMap = new HashMap<>();

    @Override
    public <F extends PipeFlow> void registerRenderer(Class<? extends F> flowClass, IPipeFlowRenderer<F> renderer) {
        flowRenderMap.put(flowClass, renderer);
    }

    @Override
    public <B extends PipeBehaviour> void registerRenderer(Class<? extends B> behaviourClass,
        IPipeBehaviourRenderer<B> renderer) {
        behaviourRenderMap.put(behaviourClass, renderer);
    }

    @Override
    public <P extends PipePluggable> void registerRenderer(Class<? extends P> plugClass,
        IPlugDynamicRenderer<P> renderer) {
        plugRenderMap.put(plugClass, renderer);
    }

    // @Override
    public <F extends PipeFlow> void registerBaker(Class<? extends F> flowClass, IPipeFlowBaker<F> baker) {
        flowBakerMap.put(flowClass, baker);
    }

    // @Override
    public <B extends PipeBehaviour> void registerBaker(Class<? extends B> flowClass, IPipeBehaviourBaker<B> baker) {
        behaviourBakerMap.put(flowClass, baker);
    }

    @Override
    public <P extends PluggableModelKey> void registerBaker(Class<? extends P> keyClass,
        IPluggableStaticBaker<P> renderer) {
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
    public static <F extends PipeFlow> IPipeFlowBaker<F> getFlowBaker(F flow) {
        return (IPipeFlowBaker<F>) INSTANCE.flowBakerMap.get(flow.getClass());
    }

    @SuppressWarnings("unchecked")
    public static <B extends PipeBehaviour> IPipeBehaviourBaker<B> getBehaviourBaker(B behaviour) {
        return (IPipeBehaviourBaker<B>) INSTANCE.behaviourBakerMap.get(behaviour.getClass());
    }

    @SuppressWarnings("unchecked")
    public static <P extends PluggableModelKey> IPluggableStaticBaker<P> getPlugBaker(P key) {
        return (IPluggableStaticBaker<P>) INSTANCE.plugBakerMap.get(key.getClass());
    }
}
