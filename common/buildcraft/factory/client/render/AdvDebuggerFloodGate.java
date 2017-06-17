/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.client.render;

import java.util.Deque;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.client.render.DetatchedRenderer.IDetachedRenderer;
import buildcraft.lib.debug.DebugRenderHelper;

import buildcraft.factory.tile.TileFloodGate;

public class AdvDebuggerFloodGate implements IDetachedRenderer {
    public final TileFloodGate target;

    public AdvDebuggerFloodGate(TileFloodGate target) {
        this.target = target;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void render(EntityPlayer player, float partialTicks) {
        BufferBuilder bb = Tessellator.getInstance().getBuffer();
        bb.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        TreeMap<Integer, Deque<BlockPos>> queues = target.clientLayerQueues;

        int r = 255;
        int g = 255;
        int b = 255;
        for (Entry<Integer, Deque<BlockPos>> entry : queues.entrySet()) {
            Deque<BlockPos> positions = entry.getValue();
            for (BlockPos p : positions) {
                int colour = 0xFF_00_00_00 | (r << 16) | (g << 8) | b;
                DebugRenderHelper.renderSmallCuboid(bb, p, colour);
                r -= 16;
                if (r < 0) {
                    r = 256;
                    g -= 16;
                    if (g < 0) {
                        g = 256;
                        b -= 16;
                        if (b < 0) {
                            b = 256;
                        }
                    }
                }
            }
        }

        Tessellator.getInstance().draw();
    }
}
