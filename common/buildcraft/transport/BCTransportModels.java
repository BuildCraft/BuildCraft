package buildcraft.transport;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.registry.IRegistry;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import buildcraft.api.transport.pluggable.IPluggableModelBaker;

import buildcraft.lib.client.model.ModelHolderStatic;
import buildcraft.lib.client.model.ModelHolderVariable;
import buildcraft.lib.client.model.ModelPluggableItem;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.expression.DefaultContexts;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.node.value.NodeVariableBoolean;
import buildcraft.lib.expression.node.value.NodeVariableDouble;
import buildcraft.lib.expression.node.value.NodeVariableString;
import buildcraft.transport.client.model.ModelGateItem;
import buildcraft.transport.client.model.ModelPipe;
import buildcraft.transport.client.model.ModelPipeItem;
import buildcraft.transport.client.model.key.KeyPlugBlocker;
import buildcraft.transport.client.model.key.KeyPlugLightSensor;
import buildcraft.transport.client.model.key.KeyPlugPulsar;
import buildcraft.transport.client.model.plug.PlugBakerLens;
import buildcraft.transport.client.model.plug.PlugBakerSimple;
import buildcraft.transport.client.model.plug.PlugGateBaker;
import buildcraft.transport.client.render.PlugGateRenderer;
import buildcraft.transport.client.render.PlugPulsarRenderer;
import buildcraft.transport.gate.GateVariant;

public class BCTransportModels {
    public static final ModelHolderStatic BLOCKER;
    public static final ModelHolderStatic LIGHT_SENSOR;
    public static final ModelHolderStatic POWER_ADAPTER;

    private static final ModelHolderVariable GATE_STATIC, GATE_DYNAMIC;
    private static final NodeVariableString GATE_MATERIAL, GATE_MODIFIER, GATE_LOGIC;
    private static final NodeVariableBoolean GATE_ON;

    private static final ModelHolderVariable LENS, FILTER;
    private static final NodeVariableString LENS_COLOUR, LENS_SIDE;

    public static final ModelHolderStatic PULSAR_STATIC;
    private static final ModelHolderVariable PULSAR_DYNAMIC;
    private static final NodeVariableDouble PULSAR_STAGE;
    private static final NodeVariableBoolean PULSAR_ON;

    public static final IPluggableModelBaker<KeyPlugPulsar> BAKER_PLUG_PULSAR;
    public static final IPluggableModelBaker<KeyPlugBlocker> BAKER_PLUG_BLOCKER;
    public static final IPluggableModelBaker<KeyPlugLightSensor> BAKER_PLUG_LIGHT_SENSOR;

    static {
        BLOCKER = getModel("plugs/blocker.json");
        LIGHT_SENSOR = getModel("plugs/light_sensor.json");
        POWER_ADAPTER = getModel("plugs/power_adapter.json");
        PULSAR_STATIC = getModel("plugs/pulsar_static.json");

        FunctionContext fnCtx = new FunctionContext(DefaultContexts.CONTEXT_DEFAULT);
        GATE_MATERIAL = fnCtx.putVariableString("material");
        GATE_MODIFIER = fnCtx.putVariableString("modifier");
        GATE_LOGIC = fnCtx.putVariableString("logic");
        GATE_STATIC = getModel("plugs/gate.json", fnCtx);

        fnCtx = new FunctionContext(DefaultContexts.CONTEXT_DEFAULT);
        GATE_ON = fnCtx.putVariableBoolean("on");
        GATE_DYNAMIC = getModel("plugs/gate_dynamic.json", fnCtx);

        fnCtx = new FunctionContext(DefaultContexts.CONTEXT_DEFAULT);
        LENS_COLOUR = fnCtx.putVariableString("colour");
        LENS_SIDE = fnCtx.putVariableString("side");
        LENS = getModel("plugs/lens.json", fnCtx);
        FILTER = getModel("plugs/filter.json", fnCtx);

        fnCtx = new FunctionContext(DefaultContexts.CONTEXT_DEFAULT);
        PULSAR_STAGE = fnCtx.putVariableDouble("stage");
        PULSAR_ON = fnCtx.putVariableBoolean("on");
        PULSAR_DYNAMIC = getModel("plugs/pulsar_dynamic.json", fnCtx);

        BAKER_PLUG_PULSAR = new PlugBakerSimple<>(PULSAR_STATIC::getCutoutQuads);
        BAKER_PLUG_BLOCKER = new PlugBakerSimple<>(BLOCKER::getCutoutQuads);
        BAKER_PLUG_LIGHT_SENSOR = new PlugBakerSimple<>(LIGHT_SENSOR::getCutoutQuads);
    }

    private static ModelHolderStatic getModel(String loc) {
        return getModel(loc, null, false);
    }

    private static ModelHolderStatic getModel(String loc, String[][] textures) {
        return getModel(loc, textures, false);
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

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        IRegistry<ModelResourceLocation, IBakedModel> modelRegistry = event.getModelRegistry();
        String start = "buildcrafttransport:";
        registerModel(modelRegistry, start + "pipe_holder#normal", ModelPipe.INSTANCE);
        registerModel(modelRegistry, start + "pipe_item#inventory", ModelPipeItem.INSTANCE);
        registerModel(modelRegistry, start + "gate_item#inventory", ModelGateItem.INSTANCE);
        registerModel(modelRegistry, start + "plug_blocker#inventory", new ModelPluggableItem(BLOCKER.getCutoutQuads()));
        registerModel(modelRegistry, start + "plug_pulsar#inventory", new ModelPluggableItem(PULSAR_STATIC.getCutoutQuads(), getPulsarDynQuads(true, 0.5)));
        registerModel(modelRegistry, start + "plug_light_sensor#inventory", new ModelPluggableItem(LIGHT_SENSOR.getCutoutQuads()));

        PlugBakerLens.onModelBake();
        PlugGateBaker.onModelBake();
        ModelGateItem.onModelBake();

        PlugGateRenderer.onModelBake();
        PlugPulsarRenderer.onModelBake();
    }

    private static void registerModel(IRegistry<ModelResourceLocation, IBakedModel> modelRegistry, String reg, IBakedModel val) {
        modelRegistry.putObject(new ModelResourceLocation(reg), val);
    }

    public static MutableQuad[] getGateQuads(GateVariant variant) {
        return getGateQuads(variant.material.tag, variant.modifier.tag, variant.logic.tag);
    }

    public static MutableQuad[] getGateQuads(String material, String modifier, String logic) {
        GATE_MATERIAL.value = material;
        GATE_MODIFIER.value = modifier;
        GATE_LOGIC.value = logic;
        return GATE_STATIC.getCutoutQuads();
    }

    public static MutableQuad[] getGateDynQuads(boolean isOn) {
        GATE_ON.value = isOn;
        return GATE_DYNAMIC.getCutoutQuads();
    }

    private static void setupLensVariables(EnumFacing side, EnumDyeColor colour) {
        LENS_COLOUR.value = colour == null ? "clear" : colour.getName();
        LENS_SIDE.value = side.getName();
    }

    public static MutableQuad[] getLensCutoutQuads(EnumFacing side, EnumDyeColor colour) {
        setupLensVariables(side, colour);
        return LENS.getCutoutQuads();
    }

    public static MutableQuad[] getLensTranslucentQuads(EnumFacing side, EnumDyeColor colour) {
        setupLensVariables(side, colour);
        return LENS.getTranslucentQuads();
    }

    public static MutableQuad[] getFilterCutoutQuads(EnumFacing side, EnumDyeColor colour) {
        setupLensVariables(side, colour);
        return FILTER.getCutoutQuads();
    }

    public static MutableQuad[] getFilterTranslucentQuads(EnumFacing side, EnumDyeColor colour) {
        setupLensVariables(side, colour);
        return FILTER.getTranslucentQuads();
    }

    public static MutableQuad[] getPulsarDynQuads(boolean isPulsing, double stage) {
        PULSAR_STAGE.value = stage;
        PULSAR_ON.value = isPulsing;
        return PULSAR_DYNAMIC.getCutoutQuads();
    }
}
