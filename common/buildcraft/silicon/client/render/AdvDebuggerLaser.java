/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.client.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import buildcraft.api.properties.BuildCraftProperties;

import buildcraft.lib.client.render.DetatchedRenderer.IDetachedRenderer;
import buildcraft.lib.debug.DebugRenderHelper;
import buildcraft.lib.misc.VolumeUtil;

import buildcraft.silicon.BCSiliconBlocks;
import buildcraft.silicon.tile.TileLaser;

public enum AdvDebuggerLaser implements IDetachedRenderer {
    INSTANCE;

    private static final int COLOUR_VISIBLE = 0xFF_99_FF_99;
    private static final int COLOUR_NOT_VISIBLE = 0xFF_11_11_99;

    private BlockPos pos;
    private EnumFacing face;

    public static AdvDebuggerLaser getForTile(TileLaser tile) {
        INSTANCE.pos = tile.getPos();
        IBlockState state = tile.getWorld().getBlockState(INSTANCE.pos);
        if (state.getBlock() == BCSiliconBlocks.laser) {
            INSTANCE.face = state.getValue(BuildCraftProperties.BLOCK_FACING_6);
        } else {
            INSTANCE.face = null;
        }
        return INSTANCE;
    }

    @Override
    public void render(EntityPlayer player, float partialTicks) {
        if (pos == null || face == null) {
            return;
        }
        VertexBuffer vb = Tessellator.getInstance().getBuffer();
        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        VolumeUtil.iterateCone(player.world, pos, face, 6, true, (world, start, p, visible) -> {
            int colour = visible ? COLOUR_VISIBLE : COLOUR_NOT_VISIBLE;
            DebugRenderHelper.renderSmallCuboid(vb, p, colour);
        });
        Tessellator.getInstance().draw();
    }
}
