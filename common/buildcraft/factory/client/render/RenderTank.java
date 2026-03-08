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
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

@OnlyIn(Dist.CLIENT)
public class RenderTank extends TileEntityRenderer<TileTank> {
    private static final Vector3d MIN = new Vector3d(0.13, 0.01, 0.13);
    private static final Vector3d MAX = new Vector3d(0.86, 0.99, 0.86);
    private static final Vector3d MIN_CONNECTED = new Vector3d(0.13, 0, 0.13);
    private static final Vector3d MAX_CONNECTED = new Vector3d(0.86, 1 - 1e-5, 0.86);

    public RenderTank(TileEntityRendererDispatcher context) {
        super(context);
    }

    @Override
//    public void render(TileTank tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
    public void render(TileTank tile, float partialTicks, MatrixStack poseStack, IRenderTypeBuffer bufferSource, int combinedLight, int combinedOverlay) {
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
        IVertexBuilder bb = bufferSource.getBuffer(Atlases.translucentCullBlockSheet());
//        poseStack.translate(x, y, z);

        boolean[] sideRender = { true, true, true, true, true, true };
        boolean connectedUp = isFullyConnected(tile, Direction.UP, partialTicks);
        boolean connectedDown = isFullyConnected(tile, Direction.DOWN, partialTicks);
        sideRender[Direction.DOWN.ordinal()] = !connectedDown;
        sideRender[Direction.UP.ordinal()] = !connectedUp;

        Vector3d min = connectedDown ? MIN_CONNECTED : MIN;
        Vector3d max = connectedUp ? MAX_CONNECTED : MAX;
        FluidStack fluid = forRender.fluid;
        int blocklight = fluid.getRawFluid().getAttributes().getLuminosity(fluid);
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
        TileEntity oTile = thisTank.getLevel().getBlockEntity(pos);
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
                    || !fluid.isFluidEqual(thisTank.getFluidForRender(partialTicks).fluid))
            {
                return false;
            }
            if (fluid.getRawFluid().getAttributes().isGaseous(fluid)) {
                face = face.getOpposite();
            }
            return forRender.amount >= oTank.tank.getCapacity() || face == Direction.UP;
        } else {
            return false;
        }
    }
}
