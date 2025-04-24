/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport;

import buildcraft.api.core.BCLog;
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
import buildcraft.lib.misc.ExpressionCompat;
import buildcraft.lib.misc.RenderUtil;
import buildcraft.transport.client.PipeBlockColours;
import buildcraft.transport.client.model.ModelPipe;
import buildcraft.transport.client.model.ModelPipeItem;
import buildcraft.transport.client.model.key.KeyPlugBlocker;
import buildcraft.transport.client.model.key.KeyPlugPowerAdaptor;
import buildcraft.transport.client.render.*;
import buildcraft.transport.pipe.behaviour.PipeBehaviourStripes;
import buildcraft.transport.pipe.flow.PipeFlowFluids;
import buildcraft.transport.pipe.flow.PipeFlowItems;
import buildcraft.transport.pipe.flow.PipeFlowPower;
import buildcraft.transport.pipe.flow.PipeFlowRedstoneFlux;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.util.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;

public class BCTransportModels {
    public static final ModelHolderStatic BLOCKER;
    public static final ModelHolderStatic POWER_ADAPTER;

    private static final ModelHolderVariable STRIPES;
    private static final NodeVariableObject<Direction> STRIPES_DIRECTION;

    public static final IPluggableStaticBaker<KeyPlugBlocker> BAKER_PLUG_BLOCKER;
    public static final IPluggableStaticBaker<KeyPlugPowerAdaptor> BAKER_PLUG_POWER_ADAPTOR;

    static {
        // Calen: to ensure ExpressionCompat ENUM_FACING = new NodeType<>("Facing", Direction.UP); runned, or will cause IllegalArgumentException: Unknown NodeType class net.minecraft.util.Direction
        ExpressionCompat.setup();

        BLOCKER = getStaticModel("plugs/blocker");
        POWER_ADAPTER = getStaticModel("plugs/power_adapter");

        BAKER_PLUG_BLOCKER = new PlugBakerSimple<>(BLOCKER::getCutoutQuads);
        BAKER_PLUG_POWER_ADAPTOR = new PlugBakerSimple<>(POWER_ADAPTER::getCutoutQuads);

        {
            FunctionContext fnCtx = DefaultContexts.createWithAll();
            STRIPES_DIRECTION = fnCtx.putVariableObject("side", Direction.class);
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
        // 1.18.2: following events are IModBusEvent
//        MinecraftForge.EVENT_BUS.register(BCTransportModels.class);
        IEventBus modEventBus = ((FMLModContainer) ModList.get().getModContainerById(BCTransport.MODID).get()).getEventBus();
        modEventBus.register(BCTransportModels.class);
    }

    public static void fmlInit() {
        ClientRegistry.bindTileEntityRenderer(BCTransportBlocks.pipeHolderTile.get(), RenderPipeHolder::new);

        PipeApiClient.registry.registerBaker(KeyPlugBlocker.class, BAKER_PLUG_BLOCKER);
        PipeApiClient.registry.registerBaker(KeyPlugPowerAdaptor.class, BAKER_PLUG_POWER_ADAPTOR);

        PipeApiClient.registry.registerRenderer(PipeFlowItems.class, PipeFlowRendererItems.INSTANCE);
        PipeApiClient.registry.registerRenderer(PipeFlowFluids.class, PipeFlowRendererFluids.INSTANCE);
        PipeApiClient.registry.registerRenderer(PipeFlowPower.class, PipeFlowRendererPower.INSTANCE);
        PipeApiClient.registry.registerRenderer(PipeFlowRedstoneFlux.class, PipeFlowRendererRf.INSTANCE);

        PipeApiClient.registry.registerRenderer(PipeBehaviourStripes.class, PipeBehaviourRendererStripes.INSTANCE);
    }

    public static void fmlPostInit() {
        RenderUtil.registerBlockColour(BCTransportBlocks.pipeHolder.get(), PipeBlockColours.INSTANCE);
    }

//    @SubscribeEvent
//    public static void onTesrReg(RegisterRenderers event) {
//        ClientRegistry.bindTileEntityRenderer(BCTransportBlocks.pipeHolderTile.get(), RenderPipeHolder::new);
//    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onModelBake(ModelBakeEvent event) {
        event.getModelRegistry().replaceAll(((resourceLocation, bakedModel) ->
        {
            if (resourceLocation instanceof ModelResourceLocation) {
                ModelResourceLocation m = (ModelResourceLocation) resourceLocation;
                if (m.getNamespace().equals(BCTransport.MODID)) {
                    if (m.getVariant().equals("inventory") && m.getPath().startsWith("pipe_")) {
                        return ModelPipeItem.INSTANCE;
                    } else if (m.getPath().equals("pipe_holder")) {
                        return ModelPipe.INSTANCE;
                    } else if (m.getPath().contains("pipe")) {
                        BCLog.logger.warn("Found unexpected pipe at ModelBakeEvent: " + m);
                    }
                }
            }
            return bakedModel;
        }));
        putModel(event, "plug_blocker#inventory", new ModelPluggableItem(BLOCKER.getCutoutQuads()));
        putModel(event, "plug_power_adaptor#inventory", new ModelPluggableItem(POWER_ADAPTER.getCutoutQuads()));
    }

    private static void putModel(ModelBakeEvent event, String str, IBakedModel model) {
        event.getModelRegistry().replace(new ModelResourceLocation("buildcrafttransport:" + str), model);
    }

    public static MutableQuad[] getStripesDynQuads(Direction side) {
        STRIPES_DIRECTION.value = side;
        return STRIPES.getCutoutQuads();
    }
}
