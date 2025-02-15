/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.render.tile;

import buildcraft.lib.client.model.MutableVertex;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderMachineWave {
    private static final double SIZE = 1 / 16.0;

    public final MutableVertex centerStart = new MutableVertex();
    public double height = 4 / 16.0;
    public int length = 6;
    public Direction direction = Direction.NORTH;

    public RenderMachineWave() {
        // TODO Auto-generated constructor stub
    }

    public void render(VertexConsumer buffer) {
        // TODO: Sine wave (Make the tile return something?)
    }
}
