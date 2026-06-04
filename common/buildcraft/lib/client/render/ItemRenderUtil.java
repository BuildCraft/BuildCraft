/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): entire file stubbed — legacy GL/display-list item render removed in 1.20.1
package buildcraft.lib.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.item.ItemStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;

@Environment(EnvType.CLIENT)
public class ItemRenderUtil {

    public static void renderItemAbsolute(ItemStack stack, double x, double y, double z,
            Direction face, int brightness, MatrixStack matrices, VertexConsumerProvider vcp) {
        // STUB
    }
}
