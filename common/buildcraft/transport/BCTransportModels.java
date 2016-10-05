package buildcraft.transport;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.registry.IRegistry;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import buildcraft.lib.client.model.ModelHolderStatic;
import buildcraft.transport.client.model.ModelBlockerItem;
import buildcraft.transport.client.model.ModelGateItem;
import buildcraft.transport.client.model.ModelPipe;
import buildcraft.transport.client.model.ModelPipeItem;

public class BCTransportModels {
    public static final ModelHolderStatic BLOCKER;
    public static final ModelHolderStatic POWER_ADAPTER;
    public static final ModelHolderStatic LENS;
    public static final ModelHolderStatic GATE_AND;
    public static final ModelHolderStatic GATE_OR;

    static {
        BLOCKER = getModel("plugs/blocker.json");
        POWER_ADAPTER = getModel("plugs/power_adapter.json");
        LENS = getModel("plugs/lens.json");
        // TODO: Replace this with "~material:<variant>" so that this is actually useful
        // (and "~modifier:<variant>")
        String[][] gateTextures = {//
            { "~brick", "buildcrafttransport:gates/material_brick" },//
            { "~iron", "buildcrafttransport:gates/material_iron" },//
            { "~gold", "buildcrafttransport:gates/material_gold" },//
            { "~nether_brick", "buildcrafttransport:gates/material_nether_brick" },//
            { "~prismarine", "buildcrafttransport:gates/material_prismarine" },//
        };
        GATE_AND = getModel("plugs/gate_and.json", gateTextures);
        GATE_OR = getModel("plugs/gate_or.json", gateTextures);
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
    }

    private static void registerModel(IRegistry<ModelResourceLocation, IBakedModel> modelRegistry, String reg, IBakedModel val) {
        modelRegistry.putObject(new ModelResourceLocation(reg), val);
    }
}
