/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.client.render;

import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.render.tile.RenderEngine_BC8;

import buildcraft.core.BCCoreModels;
import buildcraft.core.tile.TileEngineCreative;

public class RenderEngineCreative extends RenderEngine_BC8<TileEngineCreative> {
    public static final RenderEngineCreative INSTANCE = new RenderEngineCreative();

    @Override
    protected MutableQuad[] getEngineModel(TileEngineCreative engine, float partialTicks) {
        return BCCoreModels.getCreativeEngineQuads(engine, partialTicks);
    }
}
