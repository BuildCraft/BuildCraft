/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.render.tile;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.util.EnumFacing;

import buildcraft.lib.client.model.MutableVertex;

public class RenderMachineWave {
    private static final double SIZE = 1 / 16.0;

    public final MutableVertex centerStart = new MutableVertex();
    public double height = 4 / 16.0;
    public int length = 6;
    public EnumFacing direction = EnumFacing.NORTH;

    public RenderMachineWave() {
        // TODO Auto-generated constructor stub
    }

    public void render(VertexBuffer buffer) {
        // TODO: Sine wave (Make the tile return something?)
    }
}
