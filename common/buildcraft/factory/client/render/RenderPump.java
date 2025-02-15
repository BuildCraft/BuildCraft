/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.client.render;

import buildcraft.factory.tile.TilePump;
import buildcraft.lib.client.render.laser.LaserData_BC8.LaserRow;
import buildcraft.lib.client.render.laser.LaserData_BC8.LaserType;
import buildcraft.lib.client.render.tile.RenderPartCube;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderPump implements BlockEntityRenderer<TilePump> {
    private static final int[] COLOUR_POWER = new int[16];
    private static final int COLOUR_STATUS_ON = 0xFF_77_DD_77; // a light green
    private static final int COLOUR_STATUS_OFF = 0xFF_1f_10_1b; // black-ish

    private static final int BLOCK_LIGHT_STATUS_ON = 0xF;
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
            int colour = (0xFF << 24) + (c << 16) + (c << 8) + r;
            COLOUR_POWER[i] = colour;
        }

        LED_POWER = new RenderPartCube[4];
        LED_STATUS = new RenderPartCube[4];

        for (int i = 0; i < 4; i++) {
//            Direction facing = Direction.getHorizontal(i);
            Direction facing = Direction.from2DDataValue(i);

            final int dX, dZ;
            final double ledX, ledZ;

            if (facing.getAxis() == Axis.X) {
                dX = 0;
//                dZ = facing.getAxisDirection().getOffset();
                dZ = facing.getAxisDirection().getStep();
                ledZ = 0.5;
                if (facing == Direction.EAST) {
                    ledX = 15.6 / 16.0;
                } else {
                    ledX = 0.4 / 16.0;
                }
            } else {
//                dX = -facing.getAxisDirection().getOffset();
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
            LED_POWER[i].center.overlay(OverlayTexture.NO_OVERLAY); // Calen add
            LED_POWER[i].center.normalf(1, 1, 1); // Calen add

            LED_STATUS[i] = new RenderPartCube();
            LED_STATUS[i].center.positiond(ledX + dX * STATUS, Y, ledZ + dZ * STATUS);
            LED_STATUS[i].center.overlay(OverlayTexture.NO_OVERLAY); // Calen add
            LED_STATUS[i].center.normalf(1, 1, 1); // Calen add
        }

        SpriteHolder spriteTubeMiddle = SpriteHolderRegistry.getHolder("buildcraftfactory:block/pump/tube");
        LaserRow cap = new LaserRow(spriteTubeMiddle, 0, 8, 8, 16);
        LaserRow middle = new LaserRow(spriteTubeMiddle, 0, 0, 16, 8);

        LaserRow[] middles = { middle };

        TUBE_LASER = new LaserType(cap, middle, middles, null, cap);
    }

    private static boolean whiteTextureFlag = false;

    // public static void textureStitchPost()
    public static void initWhiteTex() {
        whiteTextureFlag = true;
        for (int i = 0; i < 4; i++) {
            LED_POWER[i].setWhiteTex();
            LED_STATUS[i].setWhiteTex();
        }
    }

    private final RenderTube tubeRenderer = new RenderTube(null, TUBE_LASER);

    public RenderPump(BlockEntityRendererProvider.Context context) {
    }

    @Override
//    public void renderTileEntityFast(@Nonnull TilePump tile, double x, double y, double z, float partialTicks, int destroyStage, float partial, @Nonnull BufferBuilder buffer)
    public void render(TilePump tile, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        // Calen: get the white texture
        if (!whiteTextureFlag) {
            initWhiteTex();
        }

        VertexConsumer buffer = bufferSource.getBuffer(Sheets.solidBlockSheet());

//        Minecraft.getMinecraft().mcProfiler.startSection("bc");
        Minecraft.getInstance().getProfiler().push("bc");
//        Minecraft.getMinecraft().mcProfiler.startSection("pump");
        Minecraft.getInstance().getProfiler().push("pump");

//        buffer.setTranslation(x, y, z);

        float percentFilled = tile.getPercentFilledForRender();
        int powerColour = COLOUR_POWER[(int) (percentFilled * (COLOUR_POWER.length - 1))];

        boolean complete = tile.isComplete();
        int statusColour = complete ? COLOUR_STATUS_OFF : COLOUR_STATUS_ON;
        int statusLight = complete ? BLOCK_LIGHT_STATUS_OFF : BLOCK_LIGHT_STATUS_ON;
        for (int i = 0; i < 4; i++) {
            // Get the light level of a direction
//            Direction dir = Direction.getHorizontal(i);
            Direction dir = Direction.from2DDataValue(i);
//            BlockPos pos = tile.getPos().offset(dir);
            BlockPos pos = tile.getBlockPos().relative(dir);
//            int block = tile.getWorld().getLightFor(EnumSkyBlock.BLOCK, pos);
            byte block = (byte) tile.getLevel().getLightEmission(pos);
//            int sky = tile.getWorld().getLightFor(EnumSkyBlock.SKY, pos);
            byte sky = (byte) tile.getLevel().getLightEngine().getRawBrightness(pos, 0);

            LED_POWER[i].center.colouri(powerColour);
            LED_STATUS[i].center.colouri(statusColour);

            LED_POWER[i].center.lighti(block, sky);
            LED_STATUS[i].center.lighti((byte) Math.max(statusLight, block), sky);

            LED_POWER[i].render(poseStack, buffer);
            LED_STATUS[i].render(poseStack, buffer);

            // TODO: fluid rendering
        }

        tubeRenderer.render(tile, partialTicks, poseStack, bufferSource, combinedLight, combinedOverlay);

//        Minecraft.getMinecraft().mcProfiler.endSection();
        Minecraft.getInstance().getProfiler().pop();
//        Minecraft.getMinecraft().mcProfiler.endSection();
        Minecraft.getInstance().getProfiler().pop();
    }

    @Override
//    public boolean isGlobalRenderer(TilePump tile)
    public boolean shouldRenderOffScreen(TilePump tile) {
        return true;
    }

    public static void init() {
        //force the static block to run before texture stitching
    }
}
