/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport;

import buildcraft.api.transport.pipe.PipeApiClient;
import buildcraft.api.transport.pluggable.IPluggableStaticBaker;
import buildcraft.lib.client.model.ModelHolderStatic;
import buildcraft.lib.client.model.ModelHolderVariable;
import buildcraft.lib.client.model.ModelPluggableItem;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.expression.DefaultContexts;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.node.value.NodeVariableString;
import buildcraft.lib.misc.data.ModelVariableData;
import buildcraft.transport.client.model.GateMeshDefinition;
import buildcraft.transport.client.model.ModelGateItem;
import buildcraft.transport.client.model.ModelPipe;
import buildcraft.transport.client.model.ModelPipeItem;
import buildcraft.transport.client.model.key.*;
import buildcraft.transport.client.model.plug.*;
import buildcraft.transport.client.render.*;
import buildcraft.transport.gate.GateVariant;
import buildcraft.transport.pipe.behaviour.PipeBehaviourStripes;
import buildcraft.transport.pipe.flow.PipeFlowFluids;
import buildcraft.transport.pipe.flow.PipeFlowItems;
import buildcraft.transport.pipe.flow.PipeFlowPower;
import buildcraft.transport.plug.PluggableGate;
import buildcraft.transport.plug.PluggablePulsar;
import buildcraft.transport.tile.TilePipeHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BCTransportModels {
    public static final ModelHolderStatic BLOCKER;
    public static final ModelHolderStatic LIGHT_SENSOR;
    public static final ModelHolderStatic POWER_ADAPTER;

    public static final ModelHolderVariable GATE_STATIC, GATE_DYNAMIC;
    private static final ModelVariableData GATE_VAR_DATA_STATIC;

    private static final ModelHolderVariable LENS, FILTER;
    private static final NodeVariableString LENS_COLOUR, LENS_SIDE;

    public static final ModelHolderStatic PULSAR_STATIC;
    public static final ModelHolderVariable PULSAR_DYNAMIC;

    private static final ModelHolderVariable STRIPES;
    private static final NodeVariableString STRIPES_DIRECTION;

    public static final IPluggableStaticBaker<KeyPlugPulsar> BAKER_PLUG_PULSAR;
    public static final IPluggableStaticBaker<KeyPlugBlocker> BAKER_PLUG_BLOCKER;
    public static final IPluggableStaticBaker<KeyPlugLightSensor> BAKER_PLUG_LIGHT_SENSOR;

    static {
        BLOCKER = getModel("plugs/blocker.json");
        LIGHT_SENSOR = getModel("plugs/light_sensor.json");
        POWER_ADAPTER = getModel("plugs/power_adapter.json");
        PULSAR_STATIC = getModel("plugs/pulsar_static.json");

        GATE_STATIC = getModel("plugs/gate.json", PluggableGate.MODEL_FUNC_CTX_STATIC);
        GATE_DYNAMIC = getModel("plugs/gate_dynamic.json", PluggableGate.MODEL_FUNC_CTX_DYNAMIC);
        GATE_VAR_DATA_STATIC = new ModelVariableData();

        FunctionContext fnCtx = DefaultContexts.createWithAll();
        LENS_COLOUR = fnCtx.putVariableString("colour");
        LENS_SIDE = fnCtx.putVariableString("side");
        LENS = getModel("plugs/lens.json", fnCtx);
        FILTER = getModel("plugs/filter.json", fnCtx);

        PULSAR_DYNAMIC = getModel("plugs/pulsar_dynamic.json", PluggablePulsar.MODEL_FUNC_CTX);

        fnCtx = DefaultContexts.createWithAll();
        STRIPES_DIRECTION = fnCtx.putVariableString("side");
        STRIPES = getModel("pipes/stripes.json", fnCtx);

        BAKER_PLUG_PULSAR = new PlugBakerSimple<>(PULSAR_STATIC::getCutoutQuads);
        BAKER_PLUG_BLOCKER = new PlugBakerSimple<>(BLOCKER::getCutoutQuads);
        BAKER_PLUG_LIGHT_SENSOR = new PlugBakerSimple<>(LIGHT_SENSOR::getCutoutQuads);
    }

    private static ModelHolderStatic getModel(String loc) {
        return getModel(loc, null, false);
    }

    private static ModelHolderStatic getModel(String loc, String[][] textures, boolean allowTextureFallthrough) {
        return new ModelHolderStatic("buildcrafttransport:models/" + loc, textures, allowTextureFallthrough);
    }

    private static ModelHolderVariable getModel(String loc, FunctionContext fnCtx) {
        return new ModelHolderVariable("buildcrafttransport:models/" + loc, fnCtx);
    }

    public static void fmlPreInit() {
        MinecraftForge.EVENT_BUS.register(BCTransportModels.class);
    }

    public static void fmlInit() {
        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(BCTransportItems.plugGate, GateMeshDefinition.INSTANCE);
        ClientRegistry.bindTileEntitySpecialRenderer(TilePipeHolder.class, new RenderPipeHolder());

        PipeApiClient.registry.registerBaker(KeyPlugGate.class, PlugGateBaker.INSTANCE);
        PipeApiClient.registry.registerBaker(KeyPlugBlocker.class, BCTransportModels.BAKER_PLUG_BLOCKER);
        PipeApiClient.registry.registerBaker(KeyPlugPulsar.class, BCTransportModels.BAKER_PLUG_PULSAR);
        PipeApiClient.registry.registerBaker(KeyPlugLightSensor.class, BCTransportModels.BAKER_PLUG_LIGHT_SENSOR);
        PipeApiClient.registry.registerBaker(KeyPlugLens.class, PlugBakerLens.INSTANCE);
        PipeApiClient.registry.registerBaker(KeyPlugFacade.class, PlugBakerFacade.INSTANCE);

        PipeApiClient.registry.registerRenderer(PluggableGate.class, PlugGateRenderer.INSTANCE);
        PipeApiClient.registry.registerRenderer(PluggablePulsar.class, PlugPulsarRenderer.INSTANCE);

        PipeApiClient.registry.registerRenderer(PipeFlowItems.class, PipeFlowRendererItems.INSTANCE);
        PipeApiClient.registry.registerRenderer(PipeFlowFluids.class, PipeFlowRendererFluids.INSTANCE);
        PipeApiClient.registry.registerRenderer(PipeFlowPower.class, PipeFlowRendererPower.INSTANCE);

        PipeApiClient.registry.registerRenderer(PipeBehaviourStripes.class, PipeBehaviourRendererStripes.INSTANCE);
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        IRegistry<ModelResourceLocation, IBakedModel> modelRegistry = event.getModelRegistry();
        String start = "buildcrafttransport:";
        registerModel(modelRegistry, start + "pipe_holder#normal", ModelPipe.INSTANCE);
        registerModel(modelRegistry, start + "pipe_item#inventory", ModelPipeItem.INSTANCE);
        registerModel(modelRegistry, start + "gate_item#inventory", ModelGateItem.INSTANCE);
        registerModel(modelRegistry, start + "lens_item#inventory", ModelLensItem.INSTANCE);
        registerModel(modelRegistry, start + "plug_blocker#inventory", new ModelPluggableItem(BLOCKER.getCutoutQuads()));
        PluggablePulsar.setModelVariablesForItem();
        registerModel(modelRegistry, start + "plug_pulsar#inventory", new ModelPluggableItem(PULSAR_STATIC.getCutoutQuads(), PULSAR_DYNAMIC.getCutoutQuads()));
        registerModel(modelRegistry, start + "plug_light_sensor#inventory", new ModelPluggableItem(LIGHT_SENSOR.getCutoutQuads()));
        registerModel(modelRegistry, start + "plug_facade#inventory", ModelFacadeItem.INSTANCE);

        PlugGateBaker.onModelBake();
        PlugBakerLens.onModelBake();
        ModelGateItem.onModelBake();
        ModelLensItem.onModelBake();
        ModelFacadeItem.onModelBake();
        PlugPulsarRenderer.onModelBake();
        PlugGateRenderer.onModelBake();
    }

    private static void registerModel(IRegistry<ModelResourceLocation, IBakedModel> modelRegistry, String reg, IBakedModel val) {
        modelRegistry.putObject(new ModelResourceLocation(reg), val);
    }

    public static MutableQuad[] getGateStaticQuads(EnumFacing side, GateVariant variant) {
        PluggableGate.setClientModelVariables(side, variant);
        if (GATE_VAR_DATA_STATIC.hasNoNodes()) {
            GATE_VAR_DATA_STATIC.setNodes(GATE_STATIC.createTickableNodes());
        }
        GATE_VAR_DATA_STATIC.refresh();
        return GATE_STATIC.getCutoutQuads();
    }

    private static void setupLensVariables(ModelHolderVariable model, EnumFacing side, EnumDyeColor colour) {
        LENS_COLOUR.value = colour == null ? "clear" : colour.getName();
        LENS_SIDE.value = side.getName();
        ModelVariableData varData = new ModelVariableData();
        varData.setNodes(model.createTickableNodes());
        varData.tick();
        varData.refresh();
    }

    public static MutableQuad[] getLensCutoutQuads(EnumFacing side, EnumDyeColor colour) {
        setupLensVariables(LENS, side, colour);
        return LENS.getCutoutQuads();
    }

    public static MutableQuad[] getLensTranslucentQuads(EnumFacing side, EnumDyeColor colour) {
        setupLensVariables(LENS, side, colour);
        return LENS.getTranslucentQuads();
    }

    public static MutableQuad[] getFilterCutoutQuads(EnumFacing side, EnumDyeColor colour) {
        setupLensVariables(FILTER, side, colour);
        return FILTER.getCutoutQuads();
    }

    public static MutableQuad[] getFilterTranslucentQuads(EnumFacing side, EnumDyeColor colour) {
        setupLensVariables(FILTER, side, colour);
        return FILTER.getTranslucentQuads();
    }

    public static MutableQuad[] getStripesDynQuads(EnumFacing side) {
        STRIPES_DIRECTION.value = side.getName();
        return STRIPES.getCutoutQuads();
    }
}
