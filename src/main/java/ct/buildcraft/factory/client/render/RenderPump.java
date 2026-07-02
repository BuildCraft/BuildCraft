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

import ct.buildcraft.factory.BCFactorySprites;
import ct.buildcraft.factory.tile.TilePump;
import ct.buildcraft.lib.client.render.laser.LaserData_BC8.LaserRow;
import ct.buildcraft.lib.client.render.laser.LaserData_BC8.LaserType;
import ct.buildcraft.lib.client.render.tile.RenderPartCube;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.phys.Vec3;


public class RenderPump implements BlockEntityRenderer<TilePump> {
    private static final int[] COLOUR_POWER = new int[16];
    private static final int COLOUR_STATUS_ON = 0xFF_77_DD_77; // a light green
    private static final int COLOUR_STATUS_OFF = 0xFF_1f_10_1b; // black-ish

    private static final int BLOCK_LIGHT_STATUS_ON = 0xF000F0;
    private static final int BLOCK_LIGHT_STATUS_OFF = 0x0;

    private static final double POWER = 1.5 / 16.0;
    private static final double STATUS = 3.5 / 16.0;
    private static final double Y = 13.5 / 16.0;

    private static final RenderPartCube[] LED_POWER;
    private static final RenderPartCube[] LED_STATUS;

    private static final LaserType TUBE_LASER;

    static {
        for (int i = 0; i < COLOUR_POWER.length; i++) {
            int c = (i * 0x40) / COLOUR_POWER.length;
            int r = (i * 0xE0) / COLOUR_POWER.length + 0x1F;
            int colour = (0xFF << 24) + (r << 16) + (c << 8) + c;
            COLOUR_POWER[i] = colour;
        }

        LED_POWER = new RenderPartCube[4];
        LED_STATUS = new RenderPartCube[4];
        for (int i = 0; i < 4; i++) {
            Direction facing = Direction.from2DDataValue(i);

            final int dX, dZ;
            final double ledX, ledZ;

            if (facing.getAxis() == Axis.X) {
                dX = 0;
                dZ = facing.getAxisDirection().getStep();
                ledZ = 0.5;
                if (facing == Direction.EAST) {
                    ledX = 15.6 / 16.0;
                } else {
                    ledX = 0.4 / 16.0;
                }
            } else {
                dX = -facing.getAxisDirection().getStep();
                dZ = 0;
                ledX = 0.5;
                if (facing == Direction.SOUTH) {
                    ledZ = 15.6 / 16.0;
                } else {
                    ledZ = 0.4 / 16.0;
                }
            }

            LED_POWER[i] = new RenderPartCube();
            LED_POWER[i].center.positiond(ledX + dX * POWER, Y, ledZ + dZ * POWER);

            LED_STATUS[i] = new RenderPartCube();
            LED_STATUS[i].center.positiond(ledX + dX * STATUS, Y, ledZ + dZ * STATUS);
        }

        LaserRow cap = new LaserRow(BCFactorySprites.pump_tube, 0, 8, 8, 16);
        LaserRow middle = new LaserRow(BCFactorySprites.pump_tube, 0, 0, 16, 8);

        LaserRow[] middles = { middle };

        TUBE_LASER = new LaserType(cap, middle, middles, null, cap);
    }
    
    public RenderPump(BlockEntityRendererProvider.Context ctx) {
        for (int i = 0; i < 4; i++) {
            LED_POWER[i].setWhiteTex();
            LED_STATUS[i].setWhiteTex();
        }
    }

    private final RenderTube tubeRenderer = new RenderTube(TUBE_LASER);

    public RenderPump() {}

    
    
    @Override
	public void render(TilePump tile, float partialTicks, PoseStack matrix, MultiBufferSource builder,
			int combinedLight, int overlay) {
    	VertexConsumer buffer = builder.getBuffer(RenderType.cutoutMipped());
    	Pose p = matrix.last();
    	Matrix4f pose = p.pose();
    	Matrix3f normalMatrix = p.normal();

//    	matrix.translate(0, 0, -1);
        float percentFilled = tile.getPercentFilledForRender();
        int powerColour = COLOUR_POWER[(int) (percentFilled * (COLOUR_POWER.length - 1))];

        boolean complete = tile.isComplete();
        int statusColour = complete ? COLOUR_STATUS_OFF : COLOUR_STATUS_ON;
        int statusLight = complete ? BLOCK_LIGHT_STATUS_OFF : BLOCK_LIGHT_STATUS_ON;

        for (int i = 0; i < 4; i++) {
            // Get the light level of a direction

            Direction dir = Direction.from2DDataValue(i);
            BlockPos pos = tile.getBlockPos().offset(dir.getNormal());
            int block = combinedLight >> 4;
            int sky = combinedLight >> 20;

            LED_POWER[i].center.colouri(powerColour);
            LED_STATUS[i].center.colouri(statusColour);

            LED_POWER[i].center.lightf(percentFilled > 0.01 ? 1 : 0, 0);
            LED_STATUS[i].center.lighti(Math.max(statusLight, block), sky);

            LED_POWER[i].render(pose, normalMatrix, buffer);
            LED_STATUS[i].render(pose, normalMatrix, buffer);

            // TODO: fluid rendering
        }

        tubeRenderer.render(tile, partialTicks, matrix, builder, statusLight, overlay);;

		
	}

    @Override
	public boolean shouldRenderOffScreen(TilePump p_112306_) {
		return true;
	}

    @Override
	public int getViewDistance() {
		return 256;
	}

    @Override
	public boolean shouldRender(TilePump tile, Vec3 cameraPos) {
		return true;
	}

    public static void init() {
        //force the static block to run before texture stitching
    }
}
