/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.client.render;

import buildcraft.energy.BCEnergyModels;
import buildcraft.energy.tile.TileEngineStone_BC8;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.render.tile.RenderEngine_BC8;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderEngineStone extends RenderEngine_BC8<TileEngineStone_BC8> {
//    public static final RenderEngineStone INSTANCE = new RenderEngineStone();

    public RenderEngineStone(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected MutableQuad[] getEngineModel(TileEngineStone_BC8 engine, float partialTicks) {
        return BCEnergyModels.getStoneEngineQuads(engine, partialTicks);
    }
}
