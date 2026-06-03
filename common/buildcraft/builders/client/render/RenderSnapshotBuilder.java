/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): snapshot builder render deferred — legacy GL render path removed in 1.20.1
package buildcraft.builders.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

import buildcraft.builders.snapshot.ITileForSnapshotBuilder;

@Environment(EnvType.CLIENT)
public class RenderSnapshotBuilder {

    public static <T extends ITileForSnapshotBuilder> void render(T tile, double x, double y, double z,
            float partialTicks, MatrixStack matrices, VertexConsumerProvider vcp, int light) {
        // STUB
    }
}
