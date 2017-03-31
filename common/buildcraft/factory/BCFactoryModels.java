package buildcraft.factory;

import buildcraft.lib.client.model.ModelHolderVariable;
import buildcraft.lib.expression.DefaultContexts;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.node.value.NodeVariableDouble;
import buildcraft.lib.expression.node.value.NodeVariableLong;
import buildcraft.lib.expression.node.value.NodeVariableString;

public class BCFactoryModels {

    private static final ModelHolderVariable DISTILLER;
    private static final NodeVariableString DISTILLER_FACING;
    private static final NodeVariableDouble DISTILLER_PROGRESS;
    private static final NodeVariableLong DISTILLER_POWER_AVG;

    static {
        FunctionContext fnCtx = DefaultContexts.createWithAll();
        DISTILLER_FACING = fnCtx.putVariableString("facing");
        DISTILLER_PROGRESS = fnCtx.putVariableDouble("progress");
        DISTILLER_POWER_AVG = fnCtx.putVariableLong("power_average");
        DISTILLER = getModel("tiles/distiller.json", fnCtx);
    }

    private static ModelHolderVariable getModel(String loc, FunctionContext fnCtx) {
        return new ModelHolderVariable("buildcraftfactory:models/" + loc, fnCtx);
    }

    public static void fmlPreInit() {

    }

    public static void fmlInit() {

    }
}
