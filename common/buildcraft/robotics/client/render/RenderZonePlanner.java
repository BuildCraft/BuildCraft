/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): full zone-planner dynamic-texture rendering deferred — Phase 10. Legacy
// 1.12.2 render(double...) path removed; the live BlockEntityRenderer override is a no-op.
package buildcraft.robotics.client.render;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

import buildcraft.robotics.tile.TileZonePlanner;

public class RenderZonePlanner extends TileEntitySpecialRenderer<TileZonePlanner> {
    @Override
    public void render(TileZonePlanner entity, float tickDelta, net.minecraft.client.util.math.MatrixStack matrices,
        net.minecraft.client.render.VertexConsumerProvider vertexConsumers, int light, int overlay) {
        // STUB(R.Chen): rendering deferred — Phase 10
    }
}
