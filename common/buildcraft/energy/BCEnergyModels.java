package buildcraft.energy;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.registry.IRegistry;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelFluid;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import buildcraft.api.enums.EnumEngineType;
import buildcraft.api.enums.EnumPowerStage;

import buildcraft.energy.tile.TileEngineIron_BC8;
import buildcraft.energy.tile.TileEngineStone_BC8;
import buildcraft.lib.client.model.ModelHolderVariable;
import buildcraft.lib.client.model.ModelItemSimple;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.engine.TileEngineBase_BC8;
import buildcraft.lib.expression.DefaultContexts;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.node.value.NodeVariableDouble;
import buildcraft.lib.expression.node.value.NodeVariableString;
import buildcraft.lib.fluid.BCFluid;
import buildcraft.lib.misc.data.ModelVariableData;

public class BCEnergyModels {

    private static final NodeVariableDouble ENGINE_PROGRESS;
    private static final NodeVariableString ENGINE_STAGE;
    private static final NodeVariableString ENGINE_FACING;

    private static final ModelHolderVariable ENGINE_STONE;
    private static final ModelHolderVariable ENGINE_IRON;

    static {
        FunctionContext fnCtx = DefaultContexts.createWithAll();
        ENGINE_PROGRESS = fnCtx.putVariableDouble("progress");
        ENGINE_STAGE = fnCtx.putVariableString("stage");
        ENGINE_FACING = fnCtx.putVariableString("facing");
        // TODO: Item models from "item/engine_stone.json"
        ENGINE_STONE = getModel("block/engine_stone.json", fnCtx);
        ENGINE_IRON = getModel("block/engine_iron.json", fnCtx);
    }

    private static ModelHolderVariable getModel(String loc, FunctionContext fnCtx) {
        return new ModelHolderVariable("buildcraftenergy:models/" + loc, fnCtx);
    }

    public static void fmlPreInit() {
        MinecraftForge.EVENT_BUS.register(BCEnergyModels.class);
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        IRegistry<ModelResourceLocation, IBakedModel> modelRegistry = event.getModelRegistry();
        ENGINE_PROGRESS.value = 0.2;
        ENGINE_STAGE.value = EnumPowerStage.BLUE.getModelName();
        ENGINE_FACING.value = EnumFacing.UP.getName();

        ModelVariableData varData = new ModelVariableData();
        varData.setNodes(ENGINE_STONE.createTickableNodes());
        varData.tick();
        varData.refresh();
        List<BakedQuad> quads = new ArrayList<>();
        for (MutableQuad quad : ENGINE_STONE.getCutoutQuads()) {
            quads.add(quad.toBakedItem());
        }
        registerModel(modelRegistry, EnumEngineType.STONE.getItemModelLocation() + "#inventory", new ModelItemSimple(quads, ModelItemSimple.TRANSFORM_BLOCK));

        quads = new ArrayList<>();
        varData.setNodes(ENGINE_IRON.createTickableNodes());
        varData.tick();
        varData.refresh();
        for (MutableQuad quad : ENGINE_IRON.getCutoutQuads()) {
            quads.add(quad.toBakedItem());
        }
        registerModel(modelRegistry, EnumEngineType.IRON.getItemModelLocation() + "#inventory", new ModelItemSimple(quads, ModelItemSimple.TRANSFORM_BLOCK));

        for (BCFluid fluid : BCEnergyFluids.allFluids) {
            String mrl = "buildcraftenergy:fluid_block_" + fluid.getBlockName();
            ModelFluid mdl = new ModelFluid(fluid);
            registerModel(modelRegistry, mrl, mdl.bake(mdl.getDefaultState(), DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter()));
        }
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

    public static final MutableQuad[] getStoneEngineQuads(TileEngineStone_BC8 tile, float partialTicks) {
        return getEngineQuads(ENGINE_STONE, tile, partialTicks);
    }

    public static final MutableQuad[] getIronEngineQuads(TileEngineIron_BC8 tile, float partialTicks) {
        return getEngineQuads(ENGINE_IRON, tile, partialTicks);
    }
}
