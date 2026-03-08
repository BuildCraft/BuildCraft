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
import buildcraft.lib.misc.RenderUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderMiningWell extends TileEntityRenderer<TileMiningWell> {
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
            int c = ((i * 0x40) / COLOUR_POWER.length) & 0xFF;
            int r = (((i * 0xB0) / COLOUR_POWER.length) & 0xFF) + 0x4F;
            int colour = (0xFF << 24) | (c << 16) | (c << 8) | r;
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

    private static boolean whiteTextureFlag = false;

    // public static void textureStitchPost()
    public static void initWhiteTex() {
        whiteTextureFlag = true;
        LED_POWER.setWhiteTex();
        LED_STATUS.setWhiteTex();
    }

    private final RenderTube tubeRenderer = new RenderTube(null, TUBE_LASER);

    public RenderMiningWell(TileEntityRendererDispatcher context) {
        super(context);
    }

    @Override
//    public void renderTileEntityFast(@Nonnull TileMiningWell tile, double x, double y, double z, float partialTicks, int destroyStage, float partial, @Nonnull BufferBuilder buffer)
    public void render(TileMiningWell tile, float partialTicks, MatrixStack poseStack, IRenderTypeBuffer bufferSource, int combinedLight, int combinedOverlay) {
        // Calen: get the white texture
        if (!whiteTextureFlag) {
            initWhiteTex();
        }

        IVertexBuilder buffer = bufferSource.getBuffer(Atlases.translucentCullBlockSheet());

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

        // int combinedLight = tile.getWorld().getCombinedLight(tile.getPos().offset(facing), 0);
        combinedLight = RenderUtil.getCombinedLight(tile.getLevel(), tile.getBlockPos().relative(facing));
        LED_POWER.center.lighti(combinedLight);
        LED_STATUS.center.lighti(combinedLight);

        LED_POWER.center.positiond(ledX + dX * POWER, Y, ledZ + dZ * POWER);
        float percentFilled = tile.getPercentFilledForRender();
        int colourIndex = (int) (percentFilled * (COLOUR_POWER.length - 1));
        LED_POWER.center.colouri(COLOUR_POWER[colourIndex]);
        LED_POWER.center.maxLighti((byte) (percentFilled > 0.01 ? BLOCK_LIGHT_STATUS_ON : BLOCK_LIGHT_STATUS_OFF), (byte) 0);

        LED_POWER.render(poseStack, buffer);

        LED_STATUS.center.positiond(ledX + dX * STATUS, Y, ledZ + dZ * STATUS);
        boolean complete = tile.isComplete();
        LED_STATUS.center.colouri(complete ? COLOUR_STATUS_OFF : COLOUR_STATUS_ON);
        LED_STATUS.center.maxLighti((byte) (complete ? BLOCK_LIGHT_STATUS_OFF : BLOCK_LIGHT_STATUS_ON), (byte) 0);

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

    public static void init() {
    }
}
