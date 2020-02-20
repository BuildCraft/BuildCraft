/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import buildcraft.api.transport.pipe.PipeApiClient;
import buildcraft.api.transport.pluggable.IPluggableStaticBaker;

import buildcraft.lib.client.model.ModelHolderStatic;
import buildcraft.lib.client.model.ModelHolderVariable;
import buildcraft.lib.client.model.ModelPluggableItem;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.model.plug.PlugBakerSimple;
import buildcraft.lib.expression.DefaultContexts;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.node.value.NodeVariableObject;
import buildcraft.lib.misc.RenderUtil;

import buildcraft.transport.client.PipeBlockColours;
import buildcraft.transport.client.model.ModelPipe;
import buildcraft.transport.client.model.ModelPipeItem;
import buildcraft.transport.client.model.key.KeyPlugBlocker;
import buildcraft.transport.client.model.key.KeyPlugPowerAdaptor;
import buildcraft.transport.client.render.PipeBehaviourRendererStripes;
import buildcraft.transport.client.render.PipeFlowRendererFluids;
import buildcraft.transport.client.render.PipeFlowRendererItems;
import buildcraft.transport.client.render.PipeFlowRendererPower;
import buildcraft.transport.client.render.RenderPipeHolder;
import buildcraft.transport.pipe.behaviour.PipeBehaviourStripes;
import buildcraft.transport.pipe.flow.PipeFlowFluids;
import buildcraft.transport.pipe.flow.PipeFlowItems;
import buildcraft.transport.pipe.flow.PipeFlowPower;
import buildcraft.transport.tile.TilePipeHolder;

public class BCTransportModels {
    public static final ModelHolderStatic BLOCKER;
    public static final ModelHolderStatic POWER_ADAPTER;

    private static final ModelHolderVariable STRIPES;
    private static final NodeVariableObject<EnumFacing> STRIPES_DIRECTION;

    public static final IPluggableStaticBaker<KeyPlugBlocker> BAKER_PLUG_BLOCKER;
    public static final IPluggableStaticBaker<KeyPlugPowerAdaptor> BAKER_PLUG_POWER_ADAPTOR;

    static {
        BLOCKER = getStaticModel("plugs/blocker");
        POWER_ADAPTER = getStaticModel("plugs/power_adapter");

        BAKER_PLUG_BLOCKER = new PlugBakerSimple<>(BLOCKER::getCutoutQuads);
        BAKER_PLUG_POWER_ADAPTOR = new PlugBakerSimple<>(POWER_ADAPTER::getCutoutQuads);

        {
            FunctionContext fnCtx = DefaultContexts.createWithAll();
            STRIPES_DIRECTION = fnCtx.putVariableObject("side", EnumFacing.class);
            STRIPES = getModel("pipes/stripes", fnCtx);
        }
    }

    private static ModelHolderStatic getStaticModel(String str) {
        return new ModelHolderStatic("buildcrafttransport:models/" + str + ".json");
    }

    private static ModelHolderVariable getModel(String str, FunctionContext fnCtx) {
        return new ModelHolderVariable("buildcrafttransport:models/" + str + ".json", fnCtx);
    }

    public static void fmlPreInit() {
        MinecraftForge.EVENT_BUS.register(BCTransportModels.class);
    }

    public static void fmlInit() {
        ClientRegistry.bindTileEntitySpecialRenderer(TilePipeHolder.class, new RenderPipeHolder());

        PipeApiClient.registry.registerBaker(KeyPlugBlocker.class, BAKER_PLUG_BLOCKER);
        PipeApiClient.registry.registerBaker(KeyPlugPowerAdaptor.class, BAKER_PLUG_POWER_ADAPTOR);

        PipeApiClient.registry.registerRenderer(PipeFlowItems.class, PipeFlowRendererItems.INSTANCE);
        PipeApiClient.registry.registerRenderer(PipeFlowFluids.class, PipeFlowRendererFluids.INSTANCE);
        PipeApiClient.registry.registerRenderer(PipeFlowPower.class, PipeFlowRendererPower.INSTANCE);

        PipeApiClient.registry.registerRenderer(PipeBehaviourStripes.class, PipeBehaviourRendererStripes.INSTANCE);
    }

    public static void fmlPostInit() {
        RenderUtil.registerBlockColour(BCTransportBlocks.pipeHolder, PipeBlockColours.INSTANCE);
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        putModel(event, "pipe_holder#normal", ModelPipe.INSTANCE);
        putModel(event, "pipe_item#inventory", ModelPipeItem.INSTANCE);
        putModel(event, "plug_blocker#inventory", new ModelPluggableItem(BLOCKER.getCutoutQuads()));
        putModel(event, "plug_power_adaptor#inventory", new ModelPluggableItem(POWER_ADAPTER.getCutoutQuads()));
    }

    private static void putModel(ModelBakeEvent event, String str, IBakedModel model) {
        event.getModelRegistry().putObject(new ModelResourceLocation("buildcrafttransport:" + str), model);
    }

    public static MutableQuad[] getStripesDynQuads(EnumFacing side) {
        STRIPES_DIRECTION.value = side;
        return STRIPES.getCutoutQuads();
    }
}
