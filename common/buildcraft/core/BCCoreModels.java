package buildcraft.core;

import buildcraft.core.tile.TileEngineRedstone_BC8;
import buildcraft.lib.client.model.ModelHolderVariable;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.engine.TileEngineBase_BC8;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.node.value.NodeMutableDouble;
import buildcraft.lib.expression.node.value.NodeMutableString;

public class BCCoreModels {

    private static final NodeMutableDouble ENGINE_PROGRESS;
    private static final NodeMutableString ENGINE_STAGE;
    private static final NodeMutableString ENGINE_FACING;

    private static final ModelHolderVariable ENGINE_REDSTONE;
    private static final ModelHolderVariable ENGINE_CREATIVE;

    static {
        FunctionContext fnCtx = new FunctionContext();
        ENGINE_PROGRESS = fnCtx.getOrAddDouble("progress");
        ENGINE_STAGE = fnCtx.getOrAddString("stage");
        ENGINE_FACING = fnCtx.getOrAddString("facing");

        ENGINE_REDSTONE = getModel("engine_redstone", fnCtx);
        ENGINE_CREATIVE = getModel("engine_creative", fnCtx);
    }

    private static ModelHolderVariable getModel(String loc, FunctionContext fnCtx) {
        return new ModelHolderVariable("buildcraftcore:models/" + loc, fnCtx);
    }

    /** Just loads this class. */
    public static void fmlPreInit() {}

    private static MutableQuad[] getEngineQuads(ModelHolderVariable model, TileEngineBase_BC8 tile, float partialTicks) {
        ENGINE_PROGRESS.value = tile.getProgressClient(partialTicks);
        ENGINE_STAGE.value = tile.getPowerStage().getModelName();
        ENGINE_FACING.value = tile.getCurrentFacing().getName();
        return model.getCutoutQuads();
    }

    public static final MutableQuad[] getRedstoneEngineQuads(TileEngineRedstone_BC8 tile, float partialTicks) {
        return getEngineQuads(ENGINE_REDSTONE, tile, partialTicks);
    }

    public static final MutableQuad[] getCreativeEngineQuads(TileEngineBase_BC8 tile, float partialTicks) {
        return getEngineQuads(ENGINE_CREATIVE, tile, partialTicks);
    }
}
