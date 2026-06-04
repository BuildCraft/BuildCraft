/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): TileEntitySpecialRenderer (Forge) replaced by BlockEntityRenderer (Fabric) — deferred
package buildcraft.factory.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;

import buildcraft.factory.tile.TileHeatExchange;

@Environment(EnvType.CLIENT)
public class RenderHeatExchange implements BlockEntityRenderer<TileHeatExchange> {

    public RenderHeatExchange(BlockEntityRendererFactory.Context ctx) {
        // STUB
    }

    @Override
    public void render(TileHeatExchange be, float tickDelta, MatrixStack matrices,
            VertexConsumerProvider vcp, int light, int overlay) {
        // STUB
    }
}
