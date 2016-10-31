package buildcraft.transport;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.registry.IRegistry;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import buildcraft.lib.client.model.ModelHolderStatic;
import buildcraft.lib.client.model.ModelHolderVariable;
import buildcraft.lib.client.model.ModelPluggableItem;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.node.value.NodeMutableBoolean;
import buildcraft.lib.expression.node.value.NodeMutableDouble;
import buildcraft.lib.expression.node.value.NodeMutableString;
import buildcraft.transport.client.model.ModelGateItem;
import buildcraft.transport.client.model.ModelPipe;
import buildcraft.transport.client.model.ModelPipeItem;
import buildcraft.transport.client.model.plug.PlugGateBaker;
import buildcraft.transport.client.render.PlugGateRenderer;
import buildcraft.transport.client.render.PlugPulsarRenderer;
import buildcraft.transport.gate.GateVariant;

public class BCTransportModels {
    public static final ModelHolderStatic BLOCKER;
    public static final ModelHolderStatic POWER_ADAPTER;
    public static final ModelHolderStatic LENS;

    private static final ModelHolderVariable GATE_STATIC, GATE_DYNAMIC;
    /** Used in {@link #GATE_STATIC} */
    private static final NodeMutableString GATE_MATERIAL, GATE_MODIFIER, GATE_LOGIC;
    /** Used in {@link #GATE_DYNAMIC} */
    private static final NodeMutableBoolean GATE_ON;

    public static final ModelHolderStatic PULSAR_STATIC;
    private static final ModelHolderVariable PULSAR_DYNAMIC;
    private static final NodeMutableDouble PULSAR_STAGE;
    private static final NodeMutableBoolean PULSAR_ON;

    static {
        BLOCKER = getModel("plugs/blocker.json");
        POWER_ADAPTER = getModel("plugs/power_adapter.json");
        LENS = getModel("plugs/lens.json");
        PULSAR_STATIC = getModel("plugs/pulsar_static.json");

        FunctionContext fnCtx = new FunctionContext();
        GATE_MATERIAL = fnCtx.getOrAddString("material");
        GATE_MODIFIER = fnCtx.getOrAddString("modifier");
        GATE_LOGIC = fnCtx.getOrAddString("logic");
        GATE_STATIC = getModel("plugs/gate.json", fnCtx);

        fnCtx = new FunctionContext();
        GATE_ON = fnCtx.getOrAddBoolean("on");
        GATE_DYNAMIC = getModel("plugs/gate_dynamic.json", fnCtx);

        fnCtx = new FunctionContext();
        PULSAR_STAGE = fnCtx.getOrAddDouble("stage");
        PULSAR_ON = fnCtx.getOrAddBoolean("on");
        PULSAR_DYNAMIC = getModel("plugs/pulsar_dynamic.json", fnCtx);
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

    public static MutableQuad[] getPulsarDynQuads(boolean isPulsing, double stage) {
        PULSAR_STAGE.value = stage;
        PULSAR_ON.value = isPulsing;
        return PULSAR_DYNAMIC.getCutoutQuads();
    }
}
