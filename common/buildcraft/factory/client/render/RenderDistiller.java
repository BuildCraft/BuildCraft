/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.client.render;

import buildcraft.factory.BCFactoryBlocks;
import buildcraft.factory.BCFactoryModels;
import buildcraft.factory.tile.TileDistiller_BC8;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.render.fluid.FluidRenderer;
import buildcraft.lib.client.render.fluid.FluidRenderer.TankSize;
import buildcraft.lib.client.render.fluid.FluidSpriteType;
import buildcraft.lib.fluid.FluidSmoother;
import buildcraft.lib.fluid.FluidSmoother.FluidStackInterp;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.EnumFacing;
import org.lwjgl.opengl.GL11;

import java.util.EnumMap;
import java.util.Map;

public class RenderDistiller extends TileEntitySpecialRenderer<TileDistiller_BC8> {
    private static final Map<EnumFacing, TankRenderSizes> TANK_SIZES = new EnumMap<>(EnumFacing.class);

    static {
        EnumFacing face = EnumFacing.WEST;
        TankSize tankIn = new TankSize(0, 0, 4, 8, 16, 12).shrink(1 / 64.0);
        TankSize tankOutGas = new TankSize(8, 8, 0, 16, 16, 16).shrink(1 / 64.0);
        TankSize tankOutLiquid = new TankSize(8, 0, 0, 16, 8, 16).shrink(1 / 64.0);
        TankRenderSizes sizes = new TankRenderSizes(tankIn, tankOutGas, tankOutLiquid);
        for (int i = 0; i < 4; i++) {
            TANK_SIZES.put(face, sizes);
            face = face.rotateY();
            sizes = sizes.rotateY();
        }
    }

    @Override
    public void renderTileEntityAt(TileDistiller_BC8 tile, double x, double y, double z, float partialTicks, int destroyStage) {
        super.renderTileEntityAt(tile, x, y, z, partialTicks, destroyStage);

        IBlockState state = tile.getWorld().getBlockState(tile.getPos());
        if (state.getBlock() != BCFactoryBlocks.distiller) {
            return;
        }

        Profiler profiler = Minecraft.getMinecraft().mcProfiler;
        profiler.startSection("bc");
        profiler.startSection("distiller");

        int combinedLight = tile.getWorld().getCombinedLight(tile.getPos(), 0);
        EnumFacing face = state.getValue(BlockBCBase_Neptune.PROP_FACING);
        TankRenderSizes sizes = TANK_SIZES.get(face);

        // gl state setup
        RenderHelper.disableStandardItemLighting();
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);

        // buffer setup
        VertexBuffer vb = Tessellator.getInstance().getBuffer();
        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        vb.setTranslation(x, y, z);

        profiler.startSection("model");
        profiler.startSection("compute");
        if (tile.clientModelData.hasNoNodes()) {
            tile.clientModelData.setNodes(BCFactoryModels.DISTILLER.createTickableNodes());
        }
        tile.setClientModelVariables(partialTicks);
        tile.clientModelData.refresh();
        MutableQuad[] quads = BCFactoryModels.DISTILLER.getCutoutQuads();
        profiler.endStartSection("render");

        MutableQuad copy = new MutableQuad(0, null);
        int lightc = combinedLight;
        int light_block = (lightc >> 4) & 15;
        int light_sky = (lightc >> 20) & 15;
        for (MutableQuad q : quads) {
            copy.copyFrom(q);
            copy.maxLighti(light_block, light_sky);
            copy.multShade();
            copy.render(vb);
        }

        profiler.endSection();
        profiler.endStartSection("fluid");

        renderTank(sizes.tankIn, tile.smoothedTankIn, combinedLight, partialTicks, vb);
        renderTank(sizes.tankOutGas, tile.smoothedTankOutGas, combinedLight, partialTicks, vb);
        renderTank(sizes.tankOutLiquid, tile.smoothedTankOutLiquid, combinedLight, partialTicks, vb);

        // buffer finish
        vb.setTranslation(0, 0, 0);
        profiler.endStartSection("draw");
        Tessellator.getInstance().draw();

        // gl state finish
        RenderHelper.enableStandardItemLighting();

        profiler.endSection();
        profiler.endSection();
        profiler.endSection();
    }

    public static void renderTank(TankSize size, FluidSmoother tank, int combinedLight, float partialTicks, VertexBuffer vb) {
        FluidStackInterp fluid = tank.getFluidForRender(partialTicks);
        if (fluid == null || fluid.amount <= 0) {
            return;
        }
        int blockLight = fluid.fluid.getFluid().getLuminosity(fluid.fluid) & 0xF;
        combinedLight |= blockLight << 4;
        FluidRenderer.vertex.lighti(combinedLight);
        FluidRenderer.renderFluid(FluidSpriteType.STILL, fluid.fluid, fluid.amount, tank.getCapacity(), size.min, size.max, vb, null);
    }

    static class TankRenderSizes {
        final TankSize tankIn, tankOutGas, tankOutLiquid;

        public TankRenderSizes(TankSize tankIn, TankSize tankOutGas, TankSize tankOutLiquid) {
            this.tankIn = tankIn;
            this.tankOutGas = tankOutGas;
            this.tankOutLiquid = tankOutLiquid;
        }

        public TankRenderSizes rotateY() {
            return new TankRenderSizes(tankIn.rotateY(), tankOutGas.rotateY(), tankOutLiquid.rotateY());
        }
    }
}
