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
import com.google.common.collect.Lists;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.util.LazyLoadedValue;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class BCTransportModels {
    public static final ModelHolderStatic BLOCKER;
    public static final ModelHolderStatic POWER_ADAPTER;

    private static final ModelHolderVariable STRIPES;
    private static final NodeVariableObject<Direction> STRIPES_DIRECTION;

    public static final IPluggableStaticBaker<KeyPlugBlocker> BAKER_PLUG_BLOCKER;
    public static final IPluggableStaticBaker<KeyPlugPowerAdaptor> BAKER_PLUG_POWER_ADAPTOR;

    static {
        // Calen: to ensure ExpressionCompat ENUM_FACING = new NodeType<>("Facing", Direction.UP); runned, or will cause IllegalArgumentException: Unknown NodeType class net.minecraft.core.Direction
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
        return new ModelHolderStatic("buildcrafttransport:models/" + str + ".jsonbc");
    }

    private static ModelHolderVariable getModel(String str, FunctionContext fnCtx) {
        return new ModelHolderVariable("buildcrafttransport:models/" + str + ".jsonbc", fnCtx);
    }

    public static void fmlPreInit() {
        // 1.18.2: following events are IModBusEvent
//        MinecraftForge.EVENT_BUS.register(BCTransportModels.class);
        IEventBus modEventBus = ((FMLModContainer) ModList.get().getModContainerById(BCTransport.MODID).get()).getEventBus();
        modEventBus.register(BCTransportModels.class);
    }

    public static void fmlInit() {
        // Moved to #onTesrReg
//        ClientRegistry.bindTileEntitySpecialRenderer(TilePipeHolder.class, new RenderPipeHolder());

        PipeApiClient.registry.registerBaker(KeyPlugBlocker.class, BAKER_PLUG_BLOCKER);
        PipeApiClient.registry.registerBaker(KeyPlugPowerAdaptor.class, BAKER_PLUG_POWER_ADAPTOR);

        PipeApiClient.registry.registerRenderer(PipeFlowItems.class, PipeFlowRendererItems.INSTANCE);
        PipeApiClient.registry.registerRenderer(PipeFlowFluids.class, PipeFlowRendererFluids.INSTANCE);
        PipeApiClient.registry.registerRenderer(PipeFlowPower.class, PipeFlowRendererPower.INSTANCE);

        PipeApiClient.registry.registerRenderer(PipeBehaviourStripes.class, PipeBehaviourRendererStripes.INSTANCE);
    }

    public static void fmlPostInit() {
        RenderUtil.registerBlockColour(BCTransportBlocks.pipeHolder.get(), PipeBlockColours.INSTANCE);
    }

    @SubscribeEvent
    public static void onTesrReg(RegisterRenderers event) {
        BlockEntityRenderers.register(BCTransportBlocks.pipeHolderTile.get(), RenderPipeHolder::new);
    }

    // Calen 1.20.1
    private static final List<Runnable> spriteTasks = Lists.newLinkedList();

    // Calen 1.20.1
    @SubscribeEvent
    public static void onTextureStitchEvent$Post(TextureStitchEvent.Post event) {
        if (event.getAtlas().location().equals(TextureAtlas.LOCATION_BLOCKS)) {
            spriteTasks.forEach(Runnable::run);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onModelBake(ModelEvent.ModifyBakingResult event) {
        event.getModels().replaceAll(((resourceLocation, bakedModel) ->
        {
            if (resourceLocation instanceof ModelResourceLocation m) {
                if (m.getNamespace().equals(BCTransport.MODID)) {
                    if (m.getVariant().equals("inventory") && m.getPath().startsWith("pipe_")) {
                        return ModelPipeItem.INSTANCE;
                    } else if (m.getPath().equals("pipe_holder")) {
                        return ModelPipe.INSTANCE;
                    } else if (m.getPath().contains("pipe")) {
                        BCLog.logger.warn("Found unexpected pipe at ModelEvent.BakingCompleted: " + m);
                    }
                }
            }
            return bakedModel;
        }));
//        putModel(event, "plug_blocker", "inventory", new ModelPluggableItem(BLOCKER.getCutoutQuads()));
        putModel(event, "plug_blocker", "inventory", new ModelPluggableItem(spriteTasks::add, new LazyLoadedValue<>(() -> BLOCKER.getCutoutQuads())));
//        putModel(event, "plug_power_adaptor", "inventory", new ModelPluggableItem(POWER_ADAPTER.getCutoutQuads()));
        putModel(event, "plug_power_adaptor", "inventory", new ModelPluggableItem(spriteTasks::add, new LazyLoadedValue<>(() -> POWER_ADAPTER.getCutoutQuads())));
    }

    private static void putModel(ModelEvent.ModifyBakingResult event, String str, String variant, BakedModel model) {
        event.getModels().replace(new ModelResourceLocation("buildcrafttransport", str, variant), model);
    }

    public static MutableQuad[] getStripesDynQuads(Direction side) {
        STRIPES_DIRECTION.value = side;
        return STRIPES.getCutoutQuads();
    }
}
