/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.factory.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;

import ct.buildcraft.factory.BCFactoryBlocks;
import ct.buildcraft.factory.BCFactorySprites;
import ct.buildcraft.factory.tile.TileMiningWell;
import ct.buildcraft.lib.client.render.laser.LaserData_BC8.LaserRow;
import ct.buildcraft.lib.client.render.laser.LaserData_BC8.LaserType;
import ct.buildcraft.lib.client.render.tile.RenderPartCube;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

public class RenderMiningWell implements BlockEntityRenderer<TileMiningWell> {
    private static final int[] COLOUR_POWER = new int[16];
    private static final int COLOUR_STATUS_ON = 0xDD_77_FF_77; // a light green
    private static final int COLOUR_STATUS_OFF = 0xFF_1f_10_1b; // black-ish

    private static final int BLOCK_LIGHT_STATUS_ON = 0xF000F0;
    private static final int BLOCK_LIGHT_STATUS_OFF = 0x000000;

    private static final double POWER = 2.5 / 16.0;
    private static final double STATUS = 4.5 / 16.0;
    private static final double Y = 5.5 / 16.0;

    private static final RenderPartCube LED_POWER, LED_STATUS;
    private static final LaserType TUBE_LASER;

    static {
        for (int i = 0; i < COLOUR_POWER.length; i++) {
            int c = (i * 0x40) / COLOUR_POWER.length;
            int r = (i * 0xE0) / COLOUR_POWER.length + 0x1F;
            int colour = (0xFF << 24) + (r << 16) + (c << 8) + c;//argb
            COLOUR_POWER[i] = colour;
        }
        LED_POWER = new RenderPartCube();
        LED_STATUS = new RenderPartCube();

        LaserRow cap = new LaserRow(BCFactorySprites.mining_tube, 0, 8, 8, 16);
        LaserRow middle = new LaserRow(BCFactorySprites.mining_tube, 0, 0, 16, 8);

        LaserRow[] middles = { middle };

        TUBE_LASER = new LaserType(cap, middle, middles, null, cap);
    }
    
    public RenderMiningWell(BlockEntityRendererProvider.Context ctx) {
        LED_POWER.setWhiteTex();
        LED_STATUS.setWhiteTex();
    }

    private final RenderTube tubeRenderer = new RenderTube(TUBE_LASER);

    public RenderMiningWell() {}
    
    @Override
	public void render(TileMiningWell tile, float partialTicks, PoseStack matrix, MultiBufferSource builder,
			int combineLight, int overlay) {
        Direction facing = Direction.NORTH;
        BlockState state = tile.getLevel().getBlockState(tile.getBlockPos());
        if (state.getBlock() == BCFactoryBlocks.MINING_WELL_BLOCK.get()) {
            facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        }
        matrix.pushPose();
        VertexConsumer buffer = builder.getBuffer(RenderType.cutout());
        Pose p = matrix.last();
        Matrix3f normalMatrix = p.normal();
        Matrix4f pose = p.pose();

        final int dX, dZ;
        final double ledX, ledZ;

        if (facing.getAxis() == Axis.X) {
            dX = 0;
            dZ = facing.getAxisDirection().getStep();
            ledZ = 0.5;
            if (facing == Direction.EAST) {
                ledX = 15.8 / 16.0;
            } else {
                ledX = 0.2 / 16.0;
            }
        } else {
            dX = -facing.getAxisDirection().getStep();
            dZ = 0;
            ledX = 0.5;
            if (facing == Direction.SOUTH) {
                ledZ = 15.8 / 16.0;
            } else {
                ledZ = 0.2 / 16.0;
            }
        }

        LED_POWER.center.positiond(ledX + dX * POWER, Y, ledZ + dZ * POWER);
        float percentFilled = tile.getPercentFilledForRender();
        int colourIndex = (int) (percentFilled * (COLOUR_POWER.length - 1));
        LED_POWER.center.colouri(COLOUR_POWER[colourIndex]);
//        BCLog.logger.debug("" + COLOUR_POWER[colourIndex]);
        LED_POWER.center.lightf(percentFilled > 0.01 ? 1 : 0, 0);
        LED_POWER.center.overlay(overlay);

        LED_POWER.render(pose, normalMatrix, buffer);

        LED_STATUS.center.positiond(ledX + dX * STATUS, Y, ledZ + dZ * STATUS);
        boolean complete = tile.isComplete();
        LED_STATUS.center.colouri(complete ? COLOUR_STATUS_OFF : COLOUR_STATUS_ON);
        LED_STATUS.center.lighti(complete ? BLOCK_LIGHT_STATUS_OFF : BLOCK_LIGHT_STATUS_ON);
        LED_POWER.center.overlay(overlay);
        
        LED_STATUS.render(pose, normalMatrix, buffer);

        matrix.popPose();
        tubeRenderer.render(tile, partialTicks, matrix, builder, combineLight, overlay);
        }


    @Override
	public boolean shouldRenderOffScreen(TileMiningWell p_112306_) {
		return true;
	}
    
    @Override
	public boolean shouldRender(TileMiningWell p_173568_, Vec3 p_173569_) {
		return true;
	}

	public static void init() {}
}
