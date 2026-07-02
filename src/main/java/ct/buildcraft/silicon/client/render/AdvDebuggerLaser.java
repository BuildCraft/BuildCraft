/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.silicon.client.render;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;

import ct.buildcraft.api.properties.BuildCraftProperties;
import ct.buildcraft.lib.client.render.DetachedRenderer;
import ct.buildcraft.lib.debug.DebugRenderHelper;
import ct.buildcraft.lib.misc.VolumeUtil;
import ct.buildcraft.silicon.BCSiliconBlocks;
import ct.buildcraft.silicon.tile.TileLaser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class AdvDebuggerLaser implements DetachedRenderer.IDetachedRenderer {
    private static final int COLOUR_VISIBLE = 0xFF_99_FF_99;
    private static final int COLOUR_NOT_VISIBLE = 0xFF_11_11_99;

    private final BlockPos pos;
    private final Direction face;

    public AdvDebuggerLaser(TileLaser tile) {
        pos = tile.getBlockPos();
        BlockState state = tile.getLevel().getBlockState(pos);
        face = state.getBlock() == BCSiliconBlocks.LASER_BLOCK.get()
            ? state.getValue(BuildCraftProperties.BLOCK_FACING_6)
            : null;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(PoseStack pose, Matrix4f matrix, Player player, float partialTicks) {
        if (pos == null || face == null) {
            return;
        }
        BufferBuilder bb = Tesselator.getInstance().getBuilder();
        bb.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
        VolumeUtil.iterateCone(player.level, pos, face, 6, true, (world, start, p, visible) -> {
            int colour = visible ? COLOUR_VISIBLE : COLOUR_NOT_VISIBLE;
            DebugRenderHelper.renderSmallCuboid(pose, matrix, bb, p, colour);
        });
        Tesselator.getInstance().end();
    }
}
