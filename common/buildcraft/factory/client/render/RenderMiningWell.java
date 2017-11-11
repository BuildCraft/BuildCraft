/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.client.render;

import javax.annotation.Nonnull;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;

import net.minecraftforge.client.model.animation.FastTESR;

import buildcraft.api.properties.BuildCraftProperties;

import buildcraft.lib.client.render.laser.LaserData_BC8.LaserRow;
import buildcraft.lib.client.render.laser.LaserData_BC8.LaserType;
import buildcraft.lib.client.render.tile.RenderPartCube;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;

import buildcraft.factory.BCFactoryBlocks;
import buildcraft.factory.tile.TileMiningWell;

public class RenderMiningWell extends FastTESR<TileMiningWell> {
    private static final int[] COLOUR_POWER = new int[16];
    private static final int COLOUR_STATUS_ON = 0xFF_77_DD_77; // a light green
    private static final int COLOUR_STATUS_OFF = 0xFF_1f_10_1b; // black-ish

    private static final int BLOCK_LIGHT_STATUS_ON = 0xF;
    private static final int BLOCK_LIGHT_STATUS_OFF = 0x0;

    private static final double POWER = 2.5 / 16.0;
    private static final double STATUS = 4.5 / 16.0;
    private static final double Y = 5.5 / 16.0;

    private static final RenderPartCube LED_POWER, LED_STATUS;
    private static final LaserType TUBE_LASER;

    static {
        for (int i = 0; i < COLOUR_POWER.length; i++) {
            int c = (i * 0x40) / COLOUR_POWER.length;
            int r = (i * 0xE0) / COLOUR_POWER.length + 0x1F;
            int colour = (0xFF << 24) + (c << 16) + (c << 8) + r;
            COLOUR_POWER[i] = colour;
        }
        LED_POWER = new RenderPartCube();
        LED_STATUS = new RenderPartCube();

        SpriteHolder spriteTubeMiddle = SpriteHolderRegistry.getHolder("buildcraftfactory:blocks/mining_well/tube");
        LaserRow cap = new LaserRow(spriteTubeMiddle, 0, 8, 8, 16);
        LaserRow middle = new LaserRow(spriteTubeMiddle, 0, 0, 16, 8);

        LaserRow[] middles = { middle };

        TUBE_LASER = new LaserType(cap, middle, middles, null, cap);
    }

    public static void textureStitchPost() {
        LED_POWER.setWhiteTex();
        LED_STATUS.setWhiteTex();
    }

    private final RenderTube tubeRenderer = new RenderTube(TUBE_LASER);

    public RenderMiningWell() {}

    @Override
    public void renderTileEntityFast(@Nonnull TileMiningWell tile, double x, double y, double z, float partialTicks, int destroyStage, float partial, @Nonnull BufferBuilder buffer) {
        Minecraft.getMinecraft().mcProfiler.startSection("bc");
        Minecraft.getMinecraft().mcProfiler.startSection("miner");

        buffer.setTranslation(x, y, z);
        EnumFacing facing = EnumFacing.NORTH;
        IBlockState state = tile.getWorld().getBlockState(tile.getPos());
        if (state.getBlock() == BCFactoryBlocks.miningWell) {
            facing = state.getValue(BuildCraftProperties.BLOCK_FACING);
        }

        final int dX, dZ;
        final double ledX, ledZ;

        if (facing.getAxis() == Axis.X) {
            dX = 0;
            dZ = facing.getAxisDirection().getOffset();
            ledZ = 0.5;
            if (facing == EnumFacing.EAST) {
                ledX = 15.8 / 16.0;
            } else {
                ledX = 0.2 / 16.0;
            }
        } else {
            dX = -facing.getAxisDirection().getOffset();
            dZ = 0;
            ledX = 0.5;
            if (facing == EnumFacing.SOUTH) {
                ledZ = 15.8 / 16.0;
            } else {
                ledZ = 0.2 / 16.0;
            }
        }

        LED_POWER.center.positiond(ledX + dX * POWER, Y, ledZ + dZ * POWER);
        float percentFilled = tile.getPercentFilledForRender();
        int colourIndex = (int) (percentFilled * (COLOUR_POWER.length - 1));
        LED_POWER.center.colouri(COLOUR_POWER[colourIndex]);
        LED_POWER.center.lightf(percentFilled > 0.01 ? 1 : 0, 0);

        LED_POWER.render(buffer);

        LED_STATUS.center.positiond(ledX + dX * STATUS, Y, ledZ + dZ * STATUS);
        boolean complete = tile.isComplete();
        LED_STATUS.center.colouri(complete ? COLOUR_STATUS_OFF : COLOUR_STATUS_ON);
        LED_STATUS.center.lighti(complete ? BLOCK_LIGHT_STATUS_OFF : BLOCK_LIGHT_STATUS_ON, 0);

        LED_STATUS.render(buffer);

        tubeRenderer.renderTileEntityFast(tile, x, y, z, partialTicks, destroyStage, partial, buffer);

        Minecraft.getMinecraft().mcProfiler.endSection();
        Minecraft.getMinecraft().mcProfiler.endSection();
    }

    @Override
    public boolean isGlobalRenderer(TileMiningWell tile) {
        return true;
    }

    public static void init() {}
}
