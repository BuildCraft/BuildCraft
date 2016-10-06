package buildcraft.transport;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.registry.IRegistry;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import buildcraft.lib.client.model.ModelHolderStatic;
import buildcraft.lib.client.model.ModelHolderVariable;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.node.simple.NodeMutableString;
import buildcraft.transport.client.model.ModelBlockerItem;
import buildcraft.transport.client.model.ModelGateItem;
import buildcraft.transport.client.model.ModelPipe;
import buildcraft.transport.client.model.ModelPipeItem;
import buildcraft.transport.client.model.plug.PlugGateBaker;
import buildcraft.transport.gate.GateVariant;

public class BCTransportModels {
    public static final ModelHolderStatic BLOCKER;
    public static final ModelHolderStatic POWER_ADAPTER;
    public static final ModelHolderStatic LENS;

    private static final ModelHolderVariable GATE;
    private static final NodeMutableString GATE_MATERIAL, GATE_MODIFIER, GATE_LOGIC;

    static {
        BLOCKER = getModel("plugs/blocker.json");
        POWER_ADAPTER = getModel("plugs/power_adapter.json");
        LENS = getModel("plugs/lens.json");

        FunctionContext fnCtx = new FunctionContext();
        GATE_MATERIAL = fnCtx.getOrAddString("material");
        GATE_MODIFIER = fnCtx.getOrAddString("modifier");
        GATE_LOGIC = fnCtx.getOrAddString("logic");
        GATE = new ModelHolderVariable("buildcrafttransport:models/plugs/gate.json", fnCtx);
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

    public static void fmlPreInit() {
        MinecraftForge.EVENT_BUS.register(BCTransportModels.class);
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        IRegistry<ModelResourceLocation, IBakedModel> modelRegistry = event.getModelRegistry();
        registerModel(modelRegistry, "buildcrafttransport:pipe_holder#normal", ModelPipe.INSTANCE);
        registerModel(modelRegistry, "buildcrafttransport:pipe_item#inventory", ModelPipeItem.INSTANCE);
        registerModel(modelRegistry, "buildcrafttransport:gate_item#inventory", ModelGateItem.INSTANCE);
        registerModel(modelRegistry, "buildcrafttransport:pluggable/blocker#inventory", ModelBlockerItem.INSTANCE);

        PlugGateBaker.onModelBake();
        ModelGateItem.onModelBake();
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
        return GATE.getCutoutQuads();
    }
}
