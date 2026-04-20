package ct.buildcraft.silicon;

import ct.buildcraft.api.BCModules;
import ct.buildcraft.api.transport.pipe.PipeApiClient;
import ct.buildcraft.api.transport.pipe.PipeApiClient.IClientRegistry;
import ct.buildcraft.api.transport.pluggable.IPluggableStaticBaker;
import ct.buildcraft.lib.client.model.ModelHolderStatic;
import ct.buildcraft.lib.client.model.ModelHolderVariable;
import ct.buildcraft.lib.client.model.ModelPluggableItem;
import ct.buildcraft.lib.client.model.MutableQuad;
import ct.buildcraft.lib.client.model.plug.PlugBakerSimple;
import ct.buildcraft.lib.expression.DefaultContexts;
import ct.buildcraft.lib.expression.FunctionContext;
import ct.buildcraft.lib.expression.node.value.NodeVariableBoolean;
import ct.buildcraft.lib.expression.node.value.NodeVariableObject;
import ct.buildcraft.lib.misc.data.ModelVariableData;
import ct.buildcraft.silicon.client.FacadeItemColours;
import ct.buildcraft.silicon.client.model.ModelGateItem;
import ct.buildcraft.silicon.client.model.key.KeyPlugFacade;
import ct.buildcraft.silicon.client.model.key.KeyPlugGate;
import ct.buildcraft.silicon.client.model.key.KeyPlugLens;
import ct.buildcraft.silicon.client.model.key.KeyPlugLightSensor;
import ct.buildcraft.silicon.client.model.key.KeyPlugPulsar;
import ct.buildcraft.silicon.client.model.plug.ModelFacadeItem;
import ct.buildcraft.silicon.client.model.plug.ModelLensItem;
import ct.buildcraft.silicon.client.model.plug.PlugBakerFacade;
import ct.buildcraft.silicon.client.model.plug.PlugBakerLens;
import ct.buildcraft.silicon.client.model.plug.PlugGateBaker;
import ct.buildcraft.silicon.client.render.PlugGateRenderer;
import ct.buildcraft.silicon.client.render.PlugPulsarRenderer;
import ct.buildcraft.silicon.client.render.RenderLaser;
import ct.buildcraft.silicon.client.render.RenderProgrammingTable;
import ct.buildcraft.silicon.gate.GateVariant;
import ct.buildcraft.silicon.plug.PluggableGate;
import ct.buildcraft.silicon.plug.PluggablePulsar;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent.BakingCompleted;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.model.DynamicFluidContainerModel;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class BCSiliconModels {
    public static final ModelHolderStatic LIGHT_SENSOR;

    public static final ModelHolderVariable GATE_STATIC;
    public static final ModelHolderVariable GATE_DYNAMIC;
    private static final ModelVariableData GATE_VAR_DATA_STATIC = new ModelVariableData();

    private static final ModelHolderVariable LENS, FILTER;
    private static final NodeVariableBoolean LENS_HAS_COLOUR;
    private static final NodeVariableObject<DyeColor> LENS_COLOUR;
    private static final NodeVariableObject<Direction> LENS_SIDE;

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
            LENS_COLOUR = fnCtx.putVariableObject("colour", DyeColor.class);
            LENS_SIDE = fnCtx.putVariableObject("side", Direction.class);
            LENS_HAS_COLOUR = fnCtx.putVariableBoolean("has_colour");
            LENS = getModel("plugs/lens", fnCtx);
            FILTER = getModel("plugs/filter", fnCtx);
        }
    }

    private static ModelHolderStatic getStaticModel(String str) {
        return new ModelHolderStatic("buildcraftsilicon:" + str);
    }

    private static ModelHolderVariable getModel(String str, FunctionContext fnCtx) {
        return new ModelHolderVariable("buildcraftsilicon:models/" + str, fnCtx);
    }

    public static void fmlPreInit() {
      //  MinecraftForge.EVENT_BUS.register(BCSiliconModels.class);
    }

    public static void init() {
 //       Minecraft.getInstance().getItemRenderer().getItemModelShaper().register(BCSiliconItems.PLUG_GATE_ITEM.get(),
   //         GateMeshDefinition.INSTANCE);//TODO

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
    
    public static void onBlockEntityRender(EntityRenderersEvent.RegisterRenderers event) {
    	event.registerBlockEntityRenderer(BCSiliconBlocks.LASER_TILE.get(), RenderLaser::new);
    	event.registerBlockEntityRenderer(BCSiliconBlocks.PROGRAMMING_TABLE_TILE.get(), RenderProgrammingTable::new);
    }

    public static void RegisterItemColor(RegisterColorHandlersEvent.Item event) {
    	event.register(FacadeItemColours.INSTANCE, BCSiliconItems.PLUG_FACADE_ITEM.get());
//    	event.register(new DynamicFluidContainerModel.Colors(), BCSiliconItems.PLUG_LENS_ITEM.get());
    }
    
    public static void onModelBake(BakingCompleted event) {
        putModel(event, "plug/gate#inventory", ModelGateItem.INSTANCE);
        putModel(event, "plug/lens#inventory", ModelLensItem.INSTANCE);
        PluggablePulsar.setModelVariablesForItem();
        putModel(event, "plug/pulsar#inventory",
            new ModelPluggableItem(PULSAR_STATIC.getCutoutQuads(), PULSAR_DYNAMIC.getCutoutQuads()));
        putModel(event, "plug/light_sensor#inventory", new ModelPluggableItem(LIGHT_SENSOR.getCutoutQuads()));
        putModel(event, "plug/facade#inventory", ModelFacadeItem.INSTANCE);

        PlugGateBaker.onModelBake();
        PlugBakerLens.onModelBake();
        ModelGateItem.onModelBake();
        ModelLensItem.onModelBake();
        ModelFacadeItem.onModelBake();
        PlugPulsarRenderer.onModelBake();
        PlugGateRenderer.onModelBake();
    }

    private static void putModel(BakingCompleted event, String str, BakedModel model) {
        event.getModels().put(BCModules.SILICON.createModelLocation(str), model);
    }

    public static MutableQuad[] getGateStaticQuads(Direction side, GateVariant variant) {
        PluggableGate.setClientModelVariables(side, variant);
        if (GATE_VAR_DATA_STATIC.hasNoNodes()) {
            GATE_VAR_DATA_STATIC.setNodes(GATE_STATIC.createTickableNodes());
        }
        GATE_VAR_DATA_STATIC.refresh();
        return GATE_STATIC.getCutoutQuads();
    }

    private static void setupLensVariables(ModelHolderVariable model, Direction side, DyeColor colour) {
        LENS_COLOUR.value = colour == null ? DyeColor.WHITE : colour;
        LENS_SIDE.value = side;
        LENS_HAS_COLOUR.value = colour != null;
        ModelVariableData varData = new ModelVariableData();
        varData.setNodes(model.createTickableNodes());
        varData.tick();
        varData.refresh();
    }

    public static MutableQuad[] getLensCutoutQuads(Direction side, DyeColor colour) {
        setupLensVariables(LENS, side, colour);
        return LENS.getCutoutQuads();
    }

    public static MutableQuad[] getLensTranslucentQuads(Direction side, DyeColor colour) {
        setupLensVariables(LENS, side, colour);
        return LENS.getTranslucentQuads();
    }

    public static MutableQuad[] getFilterCutoutQuads(Direction side, DyeColor colour) {
        setupLensVariables(FILTER, side, colour);
        return FILTER.getCutoutQuads();
    }

    public static MutableQuad[] getFilterTranslucentQuads(Direction side, DyeColor colour) {
        setupLensVariables(FILTER, side, colour);
        return FILTER.getTranslucentQuads();
    }
}
