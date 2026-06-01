/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.client.render;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.render.BufferBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

import buildcraft.lib.chunkload.ChunkLoaderManager;
import buildcraft.lib.client.render.DetachedRenderer;
import buildcraft.lib.debug.DebugRenderHelper;

import buildcraft.builders.tile.TileQuarry;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.render.VertexFormat;

public class AdvDebuggerQuarry implements DetachedRenderer.IDetachedRenderer {
    private static final int COLOUR_CHUNK = 0x55_99_FF_99;

    private final WeakReference<TileQuarry> tileReference;

    public AdvDebuggerQuarry(TileQuarry tile) {
        tileReference = new WeakReference<>(tile);
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public void render(PlayerEntity player, float partialTicks) {
        TileQuarry tile = tileReference.get();
        if (tile == null || !tile.frameBox.isInitialized()) {
            return;
        }
        List<ChunkPos> chunkPoses = new ArrayList<>(ChunkLoaderManager.getChunksToLoad(tile));
        chunkPoses.sort(
            Comparator.comparingDouble(chunkPos ->
                -player.getPositionEyes(partialTicks).distanceTo(
                    new Vec3d(
                        chunkPos.getXStart() + 0.5 + (chunkPos.getXEnd() - chunkPos.getXStart()) / 2,
                        player.getPositionEyes(partialTicks).y,
                        chunkPos.getZStart() + 0.5 + (chunkPos.getZEnd() - chunkPos.getZStart()) / 2
                    )
                )
            )
        );
        RenderSystem.enableBlend();
        BufferBuilder bb = Tessellator.getInstance().getBuffer();
        bb.begin(VertexFormat.DrawMode.QUADS, DefaultVertexFormats.BLOCK);
        for (ChunkPos chunkPos : chunkPoses) {
            DebugRenderHelper.renderAABB(
                bb,
                new Box(
                    chunkPos.getXStart() + 0.5D,
                    tile.frameBox.min().getY() + 0.5D,
                    chunkPos.getZStart() + 0.5D,
                    chunkPos.getXEnd() + 0.5D,
                    tile.frameBox.max().getY() + 0.5D,
                    chunkPos.getZEnd() + 0.5D
                ),
                COLOUR_CHUNK
            );
        }
        Tessellator.getInstance().draw();
        RenderSystem.disableBlend();
    }
}
