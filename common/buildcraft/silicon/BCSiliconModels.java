package buildcraft.silicon;

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
import buildcraft.lib.misc.ExpressionCompat;
import buildcraft.lib.misc.RenderUtil;
import buildcraft.lib.misc.data.ModelVariableData;
import buildcraft.silicon.client.FacadeItemColours;
import buildcraft.silicon.client.model.ModelGateItem;
import buildcraft.silicon.client.model.key.*;
import buildcraft.silicon.client.model.plug.*;
import buildcraft.silicon.client.render.PlugGateRenderer;
import buildcraft.silicon.client.render.PlugPulsarRenderer;
import buildcraft.silicon.client.render.RenderLaser;
import buildcraft.silicon.client.render.RenderProgrammingTable;
import buildcraft.silicon.gate.GateVariant;
import buildcraft.silicon.plug.PluggableGate;
import buildcraft.silicon.plug.PluggablePulsar;
import com.google.common.collect.Lists;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;

import java.util.List;

@OnlyIn(Dist.CLIENT)
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
        // Calen: ensure ExpressionCompat ENUM_FACING = new NodeType<>("Facing", Direction.UP); run, or will cause IllegalArgumentException: Unknown NodeType class net.minecraft.core.Direction
        ExpressionCompat.setup();

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
        return new ModelHolderStatic("buildcraftsilicon:models/" + str + ".jsonbc");
    }

    private static ModelHolderVariable getModel(String str, FunctionContext fnCtx) {
        return new ModelHolderVariable("buildcraftsilicon:models/" + str + ".jsonbc", fnCtx);
    }

    public static void fmlPreInit() {
        // 1.18.2: following events are IModBusEvent
//        MinecraftForge.EVENT_BUS.register(BCSiliconModels.class);
        IEventBus modEventBus = ((FMLModContainer) ModList.get().getModContainerById(BCSilicon.MODID).get()).getEventBus();
        modEventBus.register(BCSiliconModels.class);
    }

    public static void fmlInit() {
//        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(BCSiliconItems.plugGate, GateMeshDefinition.INSTANCE);

        // Calen: moved to #onTesrReg
//        ClientRegistry.bindTileEntitySpecialRenderer(TileLaser.class, new RenderLaser());
//        ClientRegistry.bindTileEntitySpecialRenderer(TileProgrammingTable_Neptune.class, new RenderProgrammingTable());

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
        RenderUtil.registerItemColour(((Item) BCSiliconItems.plugFacade.get()), FacadeItemColours.INSTANCE);
    }

    @SubscribeEvent
    public static void onTesrReg(RegisterRenderers event) {
        BlockEntityRenderers.register(BCSiliconBlocks.laserTile.get(), RenderLaser::new);
        BlockEntityRenderers.register(BCSiliconBlocks.programmingTableTile.get(), RenderProgrammingTable::new);
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

    @SubscribeEvent
    public static void onModelBake(ModelEvent.ModifyBakingResult event) {
//        putModel(event, "gate_item#inventory", ModelGateItem.INSTANCE);
        event.getModels().replaceAll((rl, model) ->
                {
                    if (rl instanceof ModelResourceLocation m && m.getPath().startsWith("plug_gate")) {
                        return ModelGateItem.INSTANCE;
                    } else {
                        return model;
                    }
                }
        );
//        putModel(event, "lens_item#inventory", ModelLensItem.INSTANCE);
        putModel(event, "plug_lens#inventory", ModelLensItem.INSTANCE);
        PluggablePulsar.setModelVariablesForItem();
//        putModel(event, "plug_pulsar#inventory", new ModelPluggableItem(PULSAR_STATIC.getCutoutQuads(), PULSAR_DYNAMIC.getCutoutQuads()));
        putModel(event, "plug_pulsar#inventory", new ModelPluggableItem(spriteTasks::add, new LazyLoadedValue<>(() -> PULSAR_STATIC.getCutoutQuads()), new LazyLoadedValue<>(() -> PULSAR_DYNAMIC.getCutoutQuads())));
//        putModel(event, "plug_light_sensor#inventory", new ModelPluggableItem(LIGHT_SENSOR.getCutoutQuads()));
        putModel(event, "plug_light_sensor#inventory", new ModelPluggableItem(spriteTasks::add, new LazyLoadedValue<>(() -> LIGHT_SENSOR.getCutoutQuads())));
        putModel(event, "plug_facade#inventory", ModelFacadeItem.INSTANCE);

        PlugGateBaker.onModelBake();
        PlugBakerLens.onModelBake();
        ModelGateItem.onModelBake();
        ModelLensItem.onModelBake();
        ModelFacadeItem.onModelBake();
        PlugPulsarRenderer.onModelBake();
        PlugGateRenderer.onModelBake();
    }

    private static void putModel(ModelEvent.ModifyBakingResult event, String str, BakedModel model) {
//        event.getModelRegistry().put(BCModules.SILICON.createModelLocation(str), model);
        event.getModels().replace(BCModules.SILICON.createModelLocation(str), model);
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
