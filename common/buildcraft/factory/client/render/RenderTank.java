/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): TileEntitySpecialRenderer → BlockEntityRenderer migration
package buildcraft.factory.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;

import buildcraft.factory.tile.TileTank;

@Environment(EnvType.CLIENT)
public class RenderTank implements BlockEntityRenderer<TileTank> {

    public RenderTank(BlockEntityRendererFactory.Context ctx) {
        // STUB
    }

    @Override
    public void render(TileTank be, float tickDelta, MatrixStack matrices,
            VertexConsumerProvider vcp, int light, int overlay) {
        // STUB
    }
}
