/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.client.render;

import buildcraft.core.BCCoreModels;
import buildcraft.core.tile.TileEngineRedstone_BC8;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.render.tile.RenderEngine_BC8;

public class RenderEngineWood extends RenderEngine_BC8<TileEngineRedstone_BC8> {
    public static final RenderEngineWood INSTANCE = new RenderEngineWood();

    @Override
    protected MutableQuad[] getEngineModel(TileEngineRedstone_BC8 engine, float partialTicks) {
        return BCCoreModels.getRedstoneEngineQuads(engine, partialTicks);
    }
}
