package buildcraft.silicon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import buildcraft.api.BCModules;
import buildcraft.api.transport.pipe.PipeApiClient;
import buildcraft.api.transport.pipe.PipeApiClient.IClientRegistry;
import buildcraft.api.transport.pluggable.IPluggableStaticBaker;

import buildcraft.lib.client.model.ModelHolderStatic;
import buildcraft.lib.client.model.ModelHolderVariable;
import buildcraft.lib.client.model.ModelPluggableItem;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.model.plug.PlugBakerSimple;
import buildcraft.lib.expression.DefaultContexts;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.node.value.NodeVariableBoolean;
import buildcraft.lib.expression.node.value.NodeVariableObject;
import buildcraft.lib.misc.RenderUtil;
import buildcraft.lib.misc.data.ModelVariableData;

import buildcraft.silicon.client.FacadeItemColours;
import buildcraft.silicon.client.model.GateMeshDefinition;
import buildcraft.silicon.client.model.ModelGateItem;
import buildcraft.silicon.client.model.key.KeyPlugFacade;
import buildcraft.silicon.client.model.key.KeyPlugGate;
import buildcraft.silicon.client.model.key.KeyPlugLens;
import buildcraft.silicon.client.model.key.KeyPlugLightSensor;
import buildcraft.silicon.client.model.key.KeyPlugPulsar;
import buildcraft.silicon.client.model.plug.ModelFacadeItem;
import buildcraft.silicon.client.model.plug.ModelLensItem;
import buildcraft.silicon.client.model.plug.PlugBakerFacade;
import buildcraft.silicon.client.model.plug.PlugBakerLens;
import buildcraft.silicon.client.model.plug.PlugGateBaker;
import buildcraft.silicon.client.render.PlugGateRenderer;
import buildcraft.silicon.client.render.PlugPulsarRenderer;
import buildcraft.silicon.client.render.RenderLaser;
import buildcraft.silicon.client.render.RenderProgrammingTable;
import buildcraft.silicon.gate.GateVariant;
import buildcraft.silicon.plug.PluggableGate;
import buildcraft.silicon.plug.PluggablePulsar;
import buildcraft.silicon.tile.TileLaser;
import buildcraft.silicon.tile.TileProgrammingTable_Neptune;

public class BCSiliconModels {
    public static final ModelHolderStatic LIGHT_SENSOR;

    public static final ModelHolderVariable GATE_STATIC;
    public static final ModelHolderVariable GATE_DYNAMIC;
    private static final ModelVariableData GATE_VAR_DATA_STATIC = new ModelVariableData();

    private static final ModelHolderVariable LENS, FILTER;
    private static final NodeVariableBoolean LENS_HAS_COLOUR;
    private static final NodeVariableObject<EnumDyeColor> LENS_COLOUR;
    private static final NodeVariableObject<EnumFacing> LENS_SIDE;

    public static final ModelHolderStatic PULSAR_STATIC;
    public static final ModelHolderVariable PULSAR_DYNAMIC;

    public static final IPluggableStaticBaker<KeyPlugPulsar> BAKER_PLUG_PULSAR;
    public static final IPluggableStaticBaker<KeyPlugLightSensor> BAKER_PLUG_LIGHT_SENSOR;

    static {
        LIGHT_SENSOR = getStaticModel("plugs/light_sensor");
        GATE_STATIC = getModel("plugs/gate", PluggableGate.MODEL_FUNC_CTX_STATIC);
        GATE_DYNAMIC = getModel("plugs/gate_dynamic", PluggableGate.MODEL_FUNC_CTX_DYNAMIC);
        PULSAR_STATIC = getStaticModel("plugs/pulsar_static");
        PULSAR_DYNAMIC = getModel("plugs/pulsar_dynamic", PluggablePulsar.MODEL_FUNC_CTX);

        BAKER_PLUG_PULSAR = new PlugBakerSimple<>(PULSAR_STATIC::getCutoutQuads);
        BAKER_PLUG_LIGHT_SENSOR = new PlugBakerSimple<>(LIGHT_SENSOR::getCutoutQuads);

        {
            FunctionContext fnCtx = DefaultContexts.createWithAll();
            LENS_COLOUR = fnCtx.putVariableObject("colour", EnumDyeColor.class);
            LENS_SIDE = fnCtx.putVariableObject("side", EnumFacing.class);
            LENS_HAS_COLOUR = fnCtx.putVariableBoolean("has_colour");
            LENS = getModel("plugs/lens", fnCtx);
            FILTER = getModel("plugs/filter", fnCtx);
        }
    }

    private static ModelHolderStatic getStaticModel(String str) {
        return new ModelHolderStatic("buildcraftsilicon:models/" + str + ".json");
    }

    private static ModelHolderVariable getModel(String str, FunctionContext fnCtx) {
        return new ModelHolderVariable("buildcraftsilicon:models/" + str + ".json", fnCtx);
    }

    public static void fmlPreInit() {
        MinecraftForge.EVENT_BUS.register(BCSiliconModels.class);
    }

    public static void fmlInit() {
        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(BCSiliconItems.plugGate,
            GateMeshDefinition.INSTANCE);
        ClientRegistry.bindTileEntitySpecialRenderer(TileLaser.class, new RenderLaser());
        ClientRegistry.bindTileEntitySpecialRenderer(TileProgrammingTable_Neptune.class, new RenderProgrammingTable());

        IClientRegistry pipeRegistryClient = PipeApiClient.registry;
        if (pipeRegistryClient != null) {
            pipeRegistryClient.registerBaker(KeyPlugGate.class, PlugGateBaker.INSTANCE);
            pipeRegistryClient.registerBaker(KeyPlugPulsar.class, BAKER_PLUG_PULSAR);
            pipeRegistryClient.registerBaker(KeyPlugLightSensor.class, BAKER_PLUG_LIGHT_SENSOR);
            pipeRegistryClient.registerBaker(KeyPlugLens.class, PlugBakerLens.INSTANCE);
            pipeRegistryClient.registerBaker(KeyPlugFacade.class, PlugBakerFacade.INSTANCE);

            pipeRegistryClient.registerRenderer(PluggableGate.class, PlugGateRenderer.INSTANCE);
            pipeRegistryClient.registerRenderer(PluggablePulsar.class, PlugPulsarRenderer.INSTANCE);
        }
    }

    public static void fmlPostInit() {
        RenderUtil.registerItemColour(BCSiliconItems.plugFacade, FacadeItemColours.INSTANCE);
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        putModel(event, "gate_item#inventory", ModelGateItem.INSTANCE);
        putModel(event, "lens_item#inventory", ModelLensItem.INSTANCE);
        PluggablePulsar.setModelVariablesForItem();
        putModel(event, "plug_pulsar#inventory",
            new ModelPluggableItem(PULSAR_STATIC.getCutoutQuads(), PULSAR_DYNAMIC.getCutoutQuads()));
        putModel(event, "plug_light_sensor#inventory", new ModelPluggableItem(LIGHT_SENSOR.getCutoutQuads()));
        putModel(event, "plug_facade#inventory", ModelFacadeItem.INSTANCE);

        PlugGateBaker.onModelBake();
        PlugBakerLens.onModelBake();
        ModelGateItem.onModelBake();
        ModelLensItem.onModelBake();
        ModelFacadeItem.onModelBake();
        PlugPulsarRenderer.onModelBake();
        PlugGateRenderer.onModelBake();
    }

    private static void putModel(ModelBakeEvent event, String str, IBakedModel model) {
        event.getModelRegistry().putObject(BCModules.SILICON.createModelLocation(str), model);
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
}
