/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.client.render;

import buildcraft.energy.BCEnergyModels;
import buildcraft.energy.tile.TileEngineIron_BC8;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.render.tile.RenderEngine_BC8;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderEngineIron extends RenderEngine_BC8<TileEngineIron_BC8> {
//    public static final RenderEngineIron INSTANCE = new RenderEngineIron();

    public RenderEngineIron(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected MutableQuad[] getEngineModel(TileEngineIron_BC8 engine, float partialTicks) {
        return BCEnergyModels.getIronEngineQuads(engine, partialTicks);
    }
}
