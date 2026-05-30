/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.transport;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;

import buildcraft.api.transport.pipe.PipeApiClient;

import buildcraft.transport.client.PipeRegistryClient;
import buildcraft.transport.client.render.PipeFlowRendererFluids;
import buildcraft.transport.client.render.PipeFlowRendererItems;
import buildcraft.transport.client.render.PipeFlowRendererPower;
import buildcraft.transport.client.render.PipeFlowRendererRf;
import buildcraft.transport.client.render.PipeBehaviourRendererStripes;
import buildcraft.transport.client.render.RenderPipeHolder;
import buildcraft.transport.pipe.behaviour.PipeBehaviourStripes;
import buildcraft.transport.pipe.flow.PipeFlowFluids;
import buildcraft.transport.pipe.flow.PipeFlowItems;
import buildcraft.transport.pipe.flow.PipeFlowPower;
import buildcraft.transport.pipe.flow.PipeFlowRedstoneFlux;

@Environment(EnvType.CLIENT)
public class BCTransportClientInitializer implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Register pipe tile entity renderer
        BlockEntityRendererRegistry.INSTANCE.register(
            BCTransportBlocks.pipeHolderTile, RenderPipeHolder::new);

        // Set the client-side pipe API registry so PipeApiClient.registry is available
        PipeApiClient.registry = PipeRegistryClient.INSTANCE;

        // Register flow renderers
        PipeRegistryClient.INSTANCE.registerRenderer(PipeFlowItems.class,        PipeFlowRendererItems.INSTANCE);
        PipeRegistryClient.INSTANCE.registerRenderer(PipeFlowPower.class,        PipeFlowRendererPower.INSTANCE);
        PipeRegistryClient.INSTANCE.registerRenderer(PipeFlowFluids.class,       PipeFlowRendererFluids.INSTANCE);
        PipeRegistryClient.INSTANCE.registerRenderer(PipeFlowRedstoneFlux.class, PipeFlowRendererRf.INSTANCE);

        // Register behaviour renderers
        PipeRegistryClient.INSTANCE.registerRenderer(PipeBehaviourStripes.class, PipeBehaviourRendererStripes.INSTANCE);

        // STUB(R.Chen): block colour registration → Phase 5 ColorProviderRegistry.BLOCK.register.
        // STUB(R.Chen): model loading plugin / sprite atlas registration → Phase 5.
    }
}
