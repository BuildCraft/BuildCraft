package buildcraft.factory;

import buildcraft.factory.tile.TileDistiller_BC8;
import buildcraft.lib.client.model.ModelHolderVariable;
import buildcraft.lib.expression.FunctionContext;

public class BCFactoryModels {
    public static final ModelHolderVariable DISTILLER;

    static {
        DISTILLER = getModel("tiles/distiller.json", TileDistiller_BC8.MODEL_FUNC_CTX);
    }

    private static ModelHolderVariable getModel(String loc, FunctionContext fnCtx) {
        return new ModelHolderVariable("buildcraftfactory:models/" + loc, fnCtx);
    }

    public static void fmlPreInit() {

    }
}
