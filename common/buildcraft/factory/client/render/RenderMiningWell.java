/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.client.render;

import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.factory.BCFactoryBlocks;
import buildcraft.factory.tile.TileMiningWell;
import buildcraft.lib.client.render.laser.LaserData_BC8.LaserRow;
import buildcraft.lib.client.render.laser.LaserData_BC8.LaserType;
import buildcraft.lib.client.render.tile.RenderPartCube;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderMiningWell implements BlockEntityRenderer<TileMiningWell> {
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

        SpriteHolder spriteTubeMiddle = SpriteHolderRegistry.getHolder("buildcraftfactory:block/mining_well/tube");
        LaserRow cap = new LaserRow(spriteTubeMiddle, 0, 8, 8, 16);
        LaserRow middle = new LaserRow(spriteTubeMiddle, 0, 0, 16, 8);

        LaserRow[] middles = { middle };

        TUBE_LASER = new LaserType(cap, middle, middles, null, cap);
    }

    private static boolean whiteTextureFlag = false;

    // public static void textureStitchPost()
    public static void initWhiteTex() {
        whiteTextureFlag = true;
        LED_POWER.setWhiteTex();
        LED_STATUS.setWhiteTex();
    }

    private final RenderTube tubeRenderer = new RenderTube(null, TUBE_LASER);

    public RenderMiningWell(BlockEntityRendererProvider.Context context) {
    }

    @Override
//    public void renderTileEntityFast(@Nonnull TileMiningWell tile, double x, double y, double z, float partialTicks, int destroyStage, float partial, @Nonnull BufferBuilder buffer)
    public void render(TileMiningWell tile, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        // Calen: get the white texture
        if (!whiteTextureFlag) {
            initWhiteTex();
        }

        VertexConsumer buffer = bufferSource.getBuffer(RenderType.solid());

        Minecraft.getInstance().getProfiler().push("bc");
        Minecraft.getInstance().getProfiler().push("miner");

//        buffer.setTranslation(x, y, z);
        Direction facing = Direction.NORTH;
        BlockState state = tile.getLevel().getBlockState(tile.getBlockPos());
        if (state.getBlock() == BCFactoryBlocks.miningWell.get()) {
            facing = state.getValue(BuildCraftProperties.BLOCK_FACING);
        }

        final int dX, dZ;
        final double ledX, ledZ;

        if (facing.getAxis() == Axis.X) {
            dX = 0;
//            dZ = facing.getAxisDirection().getOffset();
            dZ = facing.getAxisDirection().getStep();
            ledZ = 0.5;
            if (facing == Direction.EAST) {
                ledX = 15.8 / 16.0;
            } else {
                ledX = 0.2 / 16.0;
            }
        } else {
//            dX = -facing.getAxisDirection().getOffset();
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
        LED_POWER.center.lightf(percentFilled > 0.01 ? 1 : 0, 0);

        LED_POWER.render(poseStack, buffer);

        LED_STATUS.center.positiond(ledX + dX * STATUS, Y, ledZ + dZ * STATUS);
        boolean complete = tile.isComplete();
        LED_STATUS.center.colouri(complete ? COLOUR_STATUS_OFF : COLOUR_STATUS_ON);
        LED_STATUS.center.lighti((byte) (complete ? BLOCK_LIGHT_STATUS_OFF : BLOCK_LIGHT_STATUS_ON), (byte) 0);

        LED_STATUS.render(poseStack, buffer);

//        tubeRenderer.renderTileEntityFast(tile, x, y, z, partialTicks, destroyStage, partial, buffer);
        tubeRenderer.render(tile, partialTicks, poseStack, bufferSource, combinedLight, combinedOverlay);

        Minecraft.getInstance().getProfiler().pop();
        Minecraft.getInstance().getProfiler().pop();
    }

    @Override
//    public boolean isGlobalRenderer(TileMiningWell tile)
    public boolean shouldRenderOffScreen(TileMiningWell p_112306_) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 512;
    }

    public static void init() {
    }
}
