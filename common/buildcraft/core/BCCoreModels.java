package buildcraft.core;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.registry.IRegistry;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import buildcraft.api.enums.EnumEngineType;
import buildcraft.api.enums.EnumPowerStage;

import buildcraft.core.tile.TileEngineCreative;
import buildcraft.core.tile.TileEngineRedstone_BC8;
import buildcraft.lib.client.model.ModelHolderVariable;
import buildcraft.lib.client.model.ModelItemSimple;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.engine.TileEngineBase_BC8;
import buildcraft.lib.expression.DefaultContexts;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.node.value.NodeVariableDouble;
import buildcraft.lib.expression.node.value.NodeVariableString;
import buildcraft.lib.misc.data.ModelVariableData;

public class BCCoreModels {

    private static final NodeVariableDouble ENGINE_PROGRESS;
    private static final NodeVariableString ENGINE_STAGE;
    private static final NodeVariableString ENGINE_FACING;

    private static final ModelHolderVariable ENGINE_REDSTONE;
    private static final ModelHolderVariable ENGINE_CREATIVE;

    static {
        FunctionContext fnCtx = DefaultContexts.createWithAll();
        ENGINE_PROGRESS = fnCtx.putVariableDouble("progress");
        ENGINE_STAGE = fnCtx.putVariableString("stage");
        ENGINE_FACING = fnCtx.putVariableString("facing");

        ENGINE_REDSTONE = getModel("block/engine_redstone.json", fnCtx);
        ENGINE_CREATIVE = getModel("block/engine_creative.json", fnCtx);
    }

    private static ModelHolderVariable getModel(String loc, FunctionContext fnCtx) {
        return new ModelHolderVariable("buildcraftcore:models/" + loc, fnCtx);
    }

    public static void fmlPreInit() {
        MinecraftForge.EVENT_BUS.register(BCCoreModels.class);
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        IRegistry<ModelResourceLocation, IBakedModel> modelRegistry = event.getModelRegistry();
        ENGINE_PROGRESS.value = 0.2;
        ENGINE_STAGE.value = EnumPowerStage.BLUE.getModelName();
        ENGINE_FACING.value = EnumFacing.UP.getName();
        ModelVariableData varData = new ModelVariableData();
        varData.setNodes(ENGINE_REDSTONE.createTickableNodes());
        varData.tick();
        varData.refresh();
        List<BakedQuad> quads = new ArrayList<>();
        for (MutableQuad quad : ENGINE_REDSTONE.getCutoutQuads()) {
            quads.add(quad.toBakedItem());
        }
        registerModel(modelRegistry, EnumEngineType.WOOD.getItemModelLocation() + "#inventory", new ModelItemSimple(quads, ModelItemSimple.TRANSFORM_BLOCK));

        quads = new ArrayList<>();
        ENGINE_STAGE.value = EnumPowerStage.BLACK.getModelName();
        varData.setNodes(ENGINE_CREATIVE.createTickableNodes());
        varData.tick();
        varData.refresh();
        for (MutableQuad quad : ENGINE_CREATIVE.getCutoutQuads()) {
            quads.add(quad.toBakedItem());
        }
        registerModel(modelRegistry, EnumEngineType.CREATIVE.getItemModelLocation() + "#inventory", new ModelItemSimple(quads, ModelItemSimple.TRANSFORM_BLOCK));
    }

    private static void registerModel(IRegistry<ModelResourceLocation, IBakedModel> modelRegistry, String reg, IBakedModel val) {
        modelRegistry.putObject(new ModelResourceLocation(reg), val);
    }

    private static MutableQuad[] getEngineQuads(ModelHolderVariable model, TileEngineBase_BC8 tile, float partialTicks) {
        ENGINE_PROGRESS.value = tile.getProgressClient(partialTicks);
        ENGINE_STAGE.value = tile.getPowerStage().getModelName();
        ENGINE_FACING.value = tile.getCurrentFacing().getName();
        if (tile.clientModelData.hasNoNodes()) {
            tile.clientModelData.setNodes(model.createTickableNodes());
        }
        tile.clientModelData.refresh();
        return model.getCutoutQuads();
    }

    public static final MutableQuad[] getRedstoneEngineQuads(TileEngineRedstone_BC8 tile, float partialTicks) {
        return getEngineQuads(ENGINE_REDSTONE, tile, partialTicks);
    }

    public static final MutableQuad[] getCreativeEngineQuads(TileEngineCreative tile, float partialTicks) {
        return getEngineQuads(ENGINE_CREATIVE, tile, partialTicks);
    }
}
