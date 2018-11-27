/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.client.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.fluids.FluidStack;

import buildcraft.lib.client.render.fluid.FluidRenderer;
import buildcraft.lib.client.render.fluid.FluidSpriteType;
import buildcraft.lib.fluid.FluidSmoother.FluidStackInterp;
import buildcraft.lib.misc.RenderUtil;
import buildcraft.lib.misc.RenderUtil.AutoTessellator;

import buildcraft.factory.tile.TileTank;

public class RenderTank extends TileEntitySpecialRenderer<TileTank> {
    private static final Vec3d MIN = new Vec3d(0.13, 0.01, 0.13);
    private static final Vec3d MAX = new Vec3d(0.86, 0.99, 0.86);
    private static final Vec3d MIN_CONNECTED = new Vec3d(0.13, 0, 0.13);
    private static final Vec3d MAX_CONNECTED = new Vec3d(0.86, 1 - 1e-5, 0.86);

    public RenderTank() {}

    @Override
    public void render(TileTank tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        FluidStackInterp forRender = tile.getFluidForRender(partialTicks);
        if (forRender == null) {
            return;
        }
        Minecraft.getMinecraft().mcProfiler.startSection("bc");
        Minecraft.getMinecraft().mcProfiler.startSection("tank");

        // gl state setup
        RenderHelper.disableStandardItemLighting();
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);

        // buffer setup
        try (AutoTessellator tess = RenderUtil.getThreadLocalUnusedTessellator()) {
            BufferBuilder bb = tess.tessellator.getBuffer();
            bb.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
            bb.setTranslation(x, y, z);

            boolean[] sideRender = { true, true, true, true, true, true };
            boolean connectedUp = isFullyConnected(tile, EnumFacing.UP, partialTicks);
            boolean connectedDown = isFullyConnected(tile, EnumFacing.DOWN, partialTicks);
            sideRender[EnumFacing.DOWN.ordinal()] = !connectedDown;
            sideRender[EnumFacing.UP.ordinal()] = !connectedUp;

            Vec3d min = connectedDown ? MIN_CONNECTED : MIN;
            Vec3d max = connectedUp ? MAX_CONNECTED : MAX;
            FluidStack fluid = forRender.fluid;
            int blocklight = fluid.getFluid().getLuminosity(fluid);
            int combinedLight = tile.getWorld().getCombinedLight(tile.getPos(), blocklight);

            FluidRenderer.vertex.lighti(combinedLight);

            FluidRenderer.renderFluid(FluidSpriteType.STILL, fluid, forRender.amount, tile.tank.getCapacity(), min, max,
                bb, sideRender);

            // buffer finish
            bb.setTranslation(0, 0, 0);
            tess.tessellator.draw();
        }

        // gl state finish
        RenderHelper.enableStandardItemLighting();

        Minecraft.getMinecraft().mcProfiler.endSection();
        Minecraft.getMinecraft().mcProfiler.endSection();
    }

    private static boolean isFullyConnected(TileTank thisTank, EnumFacing face, float partialTicks) {
        BlockPos pos = thisTank.getPos().offset(face);
        TileEntity oTile = thisTank.getWorld().getTileEntity(pos);
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
            if (fluid.getFluid().isGaseous(fluid)) {
                face = face.getOpposite();
            }
            return forRender.amount >= oTank.tank.getCapacity() || face == EnumFacing.UP;
        } else {
            return false;
        }
    }
}
