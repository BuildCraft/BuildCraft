/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

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
        // TODO: Item model
        // TODO: Proper textures for the side thingys
    }
}
