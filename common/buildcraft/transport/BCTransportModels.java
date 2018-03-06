/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.EnumDyeColor;
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
import buildcraft.lib.expression.DefaultContexts;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.node.value.NodeVariableBoolean;
import buildcraft.lib.expression.node.value.NodeVariableObject;
import buildcraft.lib.misc.RenderUtil;
import buildcraft.lib.misc.data.ModelVariableData;

import buildcraft.transport.client.FacadeItemColours;
import buildcraft.transport.client.PipeBlockColours;
import buildcraft.transport.client.model.GateMeshDefinition;
import buildcraft.transport.client.model.ModelGateItem;
import buildcraft.transport.client.model.ModelPipe;
import buildcraft.transport.client.model.ModelPipeItem;
import buildcraft.transport.client.model.key.KeyPlugBlocker;
import buildcraft.transport.client.model.key.KeyPlugFacade;
import buildcraft.transport.client.model.key.KeyPlugGate;
import buildcraft.transport.client.model.key.KeyPlugLens;
import buildcraft.transport.client.model.key.KeyPlugLightSensor;
import buildcraft.transport.client.model.key.KeyPlugPowerAdaptor;
import buildcraft.transport.client.model.key.KeyPlugPulsar;
import buildcraft.transport.client.model.plug.ModelFacadeItem;
import buildcraft.transport.client.model.plug.ModelLensItem;
import buildcraft.transport.client.model.plug.PlugBakerFacade;
import buildcraft.transport.client.model.plug.PlugBakerLens;
import buildcraft.transport.client.model.plug.PlugBakerSimple;
import buildcraft.transport.client.model.plug.PlugGateBaker;
import buildcraft.transport.client.render.PipeBehaviourRendererStripes;
import buildcraft.transport.client.render.PipeFlowRendererFluids;
import buildcraft.transport.client.render.PipeFlowRendererItems;
import buildcraft.transport.client.render.PipeFlowRendererPower;
import buildcraft.transport.client.render.PlugGateRenderer;
import buildcraft.transport.client.render.PlugPulsarRenderer;
import buildcraft.transport.client.render.RenderPipeHolder;
import buildcraft.transport.gate.GateVariant;
import buildcraft.transport.pipe.behaviour.PipeBehaviourStripes;
import buildcraft.transport.pipe.flow.PipeFlowFluids;
import buildcraft.transport.pipe.flow.PipeFlowItems;
import buildcraft.transport.pipe.flow.PipeFlowPower;
import buildcraft.transport.plug.PluggableGate;
import buildcraft.transport.plug.PluggablePulsar;
import buildcraft.transport.tile.TilePipeHolder;

public class BCTransportModels {
    public static final ModelHolderStatic BLOCKER = new ModelHolderStatic(
        "buildcrafttransport:models/plugs/blocker.json",
        (String[][]) null,
        false
    );
    public static final ModelHolderStatic LIGHT_SENSOR = new ModelHolderStatic(
        "buildcrafttransport:models/plugs/light_sensor.json",
        (String[][]) null,
        false
    );
    public static final ModelHolderStatic POWER_ADAPTER = new ModelHolderStatic(
        "buildcrafttransport:models/plugs/power_adapter.json",
        (String[][]) null,
        false
    );

    public static final ModelHolderVariable GATE_STATIC = new ModelHolderVariable(
        "buildcrafttransport:models/plugs/gate.json",
        PluggableGate.MODEL_FUNC_CTX_STATIC
    );
    public static final ModelHolderVariable GATE_DYNAMIC = new ModelHolderVariable(
        "buildcrafttransport:models/plugs/gate_dynamic.json",
        PluggableGate.MODEL_FUNC_CTX_DYNAMIC
    );
    private static final ModelVariableData GATE_VAR_DATA_STATIC = new ModelVariableData();

    private static final ModelHolderVariable LENS, FILTER;
    private static final NodeVariableBoolean LENS_HAS_COLOUR;
    private static final NodeVariableObject<EnumDyeColor> LENS_COLOUR;
    private static final NodeVariableObject<EnumFacing> LENS_SIDE;

    public static final ModelHolderStatic PULSAR_STATIC = new ModelHolderStatic(
        "buildcrafttransport:models/plugs/pulsar_static.json",
        (String[][]) null,
        false
    );
    public static final ModelHolderVariable PULSAR_DYNAMIC = new ModelHolderVariable(
        "buildcrafttransport:models/plugs/pulsar_dynamic.json",
        PluggablePulsar.MODEL_FUNC_CTX
    );

    private static final ModelHolderVariable STRIPES;
    private static final NodeVariableObject<EnumFacing> STRIPES_DIRECTION;

    public static final IPluggableStaticBaker<KeyPlugPulsar> BAKER_PLUG_PULSAR = new PlugBakerSimple<>(
        PULSAR_STATIC::getCutoutQuads
    );
    public static final IPluggableStaticBaker<KeyPlugBlocker> BAKER_PLUG_BLOCKER = new PlugBakerSimple<>(
        BLOCKER::getCutoutQuads
    );
    public static final IPluggableStaticBaker<KeyPlugLightSensor> BAKER_PLUG_LIGHT_SENSOR = new PlugBakerSimple<>(
        LIGHT_SENSOR::getCutoutQuads
    );
    public static final IPluggableStaticBaker<KeyPlugPowerAdaptor> BAKER_PLUG_POWER_ADAPTOR = new PlugBakerSimple<>(
        POWER_ADAPTER::getCutoutQuads
    );

    static {
        {
            FunctionContext fnCtx = DefaultContexts.createWithAll();
            LENS_COLOUR = fnCtx.putVariableObject("colour", EnumDyeColor.class);
            LENS_SIDE = fnCtx.putVariableObject("side", EnumFacing.class);
            LENS_HAS_COLOUR = fnCtx.putVariableBoolean("has_colour");
            LENS = new ModelHolderVariable("buildcrafttransport:models/plugs/lens.json", fnCtx);
            FILTER = new ModelHolderVariable("buildcrafttransport:models/plugs/filter.json", fnCtx);
        }
        {
            FunctionContext fnCtx = DefaultContexts.createWithAll();
            STRIPES_DIRECTION = fnCtx.putVariableObject("side", EnumFacing.class);
            STRIPES = new ModelHolderVariable("buildcrafttransport:models/pipes/stripes.json", fnCtx);
        }
    }

    public static void fmlPreInit() {
        MinecraftForge.EVENT_BUS.register(BCTransportModels.class);
    }

    public static void fmlInit() {
        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(
            BCTransportItems.plugGate,
            GateMeshDefinition.INSTANCE
        );
        ClientRegistry.bindTileEntitySpecialRenderer(TilePipeHolder.class, new RenderPipeHolder());

        PipeApiClient.registry.registerBaker(KeyPlugGate.class, PlugGateBaker.INSTANCE);
        PipeApiClient.registry.registerBaker(KeyPlugBlocker.class, BCTransportModels.BAKER_PLUG_BLOCKER);
        PipeApiClient.registry.registerBaker(KeyPlugPulsar.class, BCTransportModels.BAKER_PLUG_PULSAR);
        PipeApiClient.registry.registerBaker(KeyPlugLightSensor.class, BCTransportModels.BAKER_PLUG_LIGHT_SENSOR);
        PipeApiClient.registry.registerBaker(KeyPlugPowerAdaptor.class, BCTransportModels.BAKER_PLUG_POWER_ADAPTOR);
        PipeApiClient.registry.registerBaker(KeyPlugLens.class, PlugBakerLens.INSTANCE);
        PipeApiClient.registry.registerBaker(KeyPlugFacade.class, PlugBakerFacade.INSTANCE);

        PipeApiClient.registry.registerRenderer(PluggableGate.class, PlugGateRenderer.INSTANCE);
        PipeApiClient.registry.registerRenderer(PluggablePulsar.class, PlugPulsarRenderer.INSTANCE);

        PipeApiClient.registry.registerRenderer(PipeFlowItems.class, PipeFlowRendererItems.INSTANCE);
        PipeApiClient.registry.registerRenderer(PipeFlowFluids.class, PipeFlowRendererFluids.INSTANCE);
        PipeApiClient.registry.registerRenderer(PipeFlowPower.class, PipeFlowRendererPower.INSTANCE);

        PipeApiClient.registry.registerRenderer(PipeBehaviourStripes.class, PipeBehaviourRendererStripes.INSTANCE);
    }

    public static void fmlPostInit() {
        RenderUtil.registerBlockColour(BCTransportBlocks.pipeHolder, PipeBlockColours.INSTANCE);
        RenderUtil.registerItemColour(BCTransportItems.plugFacade, FacadeItemColours.INSTANCE);
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        event.getModelRegistry().putObject(
            new ModelResourceLocation("buildcrafttransport:pipe_holder#normal"),
            ModelPipe.INSTANCE
        );
        event.getModelRegistry().putObject(
            new ModelResourceLocation("buildcrafttransport:pipe_item#inventory"),
            ModelPipeItem.INSTANCE
        );
        event.getModelRegistry().putObject(
            new ModelResourceLocation("buildcrafttransport:gate_item#inventory"),
            ModelGateItem.INSTANCE
        );
        event.getModelRegistry().putObject(
            new ModelResourceLocation("buildcrafttransport:lens_item#inventory"),
            ModelLensItem.INSTANCE
        );
        event.getModelRegistry().putObject(
            new ModelResourceLocation("buildcrafttransport:plug_blocker#inventory"),
            new ModelPluggableItem(BLOCKER.getCutoutQuads())
        );
        PluggablePulsar.setModelVariablesForItem();
        event.getModelRegistry().putObject(
            new ModelResourceLocation("buildcrafttransport:plug_pulsar#inventory"),
            new ModelPluggableItem(PULSAR_STATIC.getCutoutQuads(), PULSAR_DYNAMIC.getCutoutQuads())
        );
        event.getModelRegistry().putObject(
            new ModelResourceLocation("buildcrafttransport:plug_light_sensor#inventory"),
            new ModelPluggableItem(LIGHT_SENSOR.getCutoutQuads())
        );
        event.getModelRegistry().putObject(
            new ModelResourceLocation("buildcrafttransport:plug_power_adaptor#inventory"),
            new ModelPluggableItem(POWER_ADAPTER.getCutoutQuads())
        );
        event.getModelRegistry().putObject(
            new ModelResourceLocation("buildcrafttransport:plug_facade#inventory"),
            ModelFacadeItem.INSTANCE
        );

        PlugGateBaker.onModelBake();
        PlugBakerLens.onModelBake();
        ModelGateItem.onModelBake();
        ModelLensItem.onModelBake();
        ModelFacadeItem.onModelBake();
        PlugPulsarRenderer.onModelBake();
        PlugGateRenderer.onModelBake();
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
        LENS_COLOUR.value = colour == null ? EnumDyeColor.WHITE : colour;
        LENS_SIDE.value = side;
        LENS_HAS_COLOUR.value = colour != null;
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
        STRIPES_DIRECTION.value = side;
        return STRIPES.getCutoutQuads();
    }
}
