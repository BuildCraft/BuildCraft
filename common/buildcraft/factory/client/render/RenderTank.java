/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.client.render;

import buildcraft.factory.tile.TileTank;
import buildcraft.lib.client.render.fluid.FluidRenderer;
import buildcraft.lib.client.render.fluid.FluidSpriteType;
import buildcraft.lib.fluid.FluidSmoother.FluidStackInterp;
import buildcraft.lib.misc.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

@OnlyIn(Dist.CLIENT)
public class RenderTank implements BlockEntityRenderer<TileTank> {
    private static final Vec3 MIN = new Vec3(0.13, 0.01, 0.13);
    private static final Vec3 MAX = new Vec3(0.86, 0.99, 0.86);
    private static final Vec3 MIN_CONNECTED = new Vec3(0.13, 0, 0.13);
    private static final Vec3 MAX_CONNECTED = new Vec3(0.86, 1 - 1e-5, 0.86);

    public RenderTank(BlockEntityRendererProvider.Context context) {
    }

    @Override
//    public void render(TileTank tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
    public void render(TileTank tile, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        FluidStackInterp forRender = tile.getFluidForRender(partialTicks);
        if (forRender == null) {
            return;
        }
        Minecraft.getInstance().getProfiler().push("bc");
        Minecraft.getInstance().getProfiler().push("tank");

//        // gl state setup
//        RenderHelper.disableStandardItemLighting();
//        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
//        GlStateManager.enableBlend();
//        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);

//        // buffer setup
//        try (AutoTessellator tess = RenderUtil.getThreadLocalUnusedTessellator()) {
//            BufferBuilder bb = tess.tessellator.getBuffer();
//            bb.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
//            bb.setTranslation(x, y, z);
        VertexConsumer bb = bufferSource.getBuffer(FluidRenderer.COMMON_FLUID_RENDER_TYPE_TRANSLUCENT);
//        poseStack.translate(x, y, z);

        boolean[] sideRender = { true, true, true, true, true, true };
        boolean connectedUp = isFullyConnected(tile, Direction.UP, partialTicks);
        boolean connectedDown = isFullyConnected(tile, Direction.DOWN, partialTicks);
        sideRender[Direction.DOWN.ordinal()] = !connectedDown;
        sideRender[Direction.UP.ordinal()] = !connectedUp;

        Vec3 min = connectedDown ? MIN_CONNECTED : MIN;
        Vec3 max = connectedUp ? MAX_CONNECTED : MAX;
        FluidStack fluid = forRender.fluid;
        int blocklight = fluid.getRawFluid().getFluidType().getLightLevel(fluid);
//        int combinedLight = tile.getWorld().getCombinedLight(tile.getPos(), blocklight);
        combinedLight = RenderUtil.combineWithFluidLight(combinedLight, (byte) blocklight);

        FluidRenderer.vertex.lighti(combinedLight);
        FluidRenderer.vertex.overlay(combinedOverlay);

        FluidRenderer.renderFluid(
                FluidSpriteType.STILL,
                fluid,
                forRender.amount,
                tile.tank.getCapacity(),
                min,
                max,
                poseStack.last(),
                bb,
                sideRender
        );

//        // buffer finish
//        bb.setTranslation(0, 0, 0);
//        tess.tessellator.draw();
//    }

//        // gl state finish
//        RenderHelper.enableStandardItemLighting();

        Minecraft.getInstance().getProfiler().pop();
        Minecraft.getInstance().getProfiler().pop();
    }

    private static boolean isFullyConnected(TileTank thisTank, Direction face, float partialTicks) {
        BlockPos pos = thisTank.getBlockPos().relative(face);
        BlockEntity oTile = thisTank.getLevel().getBlockEntity(pos);
        if (oTile instanceof TileTank) {
            TileTank oTank = (TileTank) oTile;
            if (!TileTank.canTanksConnect(thisTank, oTank, face)) {
                return false;
            }
            FluidStackInterp forRender = oTank.getFluidForRender(partialTicks);
            if (forRender == null) {
                return false;
            }
            FluidStack fluid = forRender.fluid;
            if (fluid == null || forRender.amount <= 0) {
                return false;
            } else if (thisTank.getFluidForRender(partialTicks) == null
                    || !fluid.isFluidEqual(thisTank.getFluidForRender(partialTicks).fluid)) {
                return false;
            }
            if (fluid.getRawFluid().getFluidType().isLighterThanAir()) {
                face = face.getOpposite();
            }
            return forRender.amount >= oTank.tank.getCapacity() || face == Direction.UP;
        } else {
            return false;
        }
    }
}
