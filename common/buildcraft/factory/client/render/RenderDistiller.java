/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.client.render;

import java.util.EnumMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.RenderSystem.DestFactor;
import com.mojang.blaze3d.systems.RenderSystem.SourceFactor;
import net.minecraft.client.render.RenderHelper;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.render.fluid.FluidRenderer;
import buildcraft.lib.client.render.fluid.FluidRenderer.TankSize;
import buildcraft.lib.client.render.fluid.FluidSpriteType;
import buildcraft.lib.fluid.FluidSmoother;
import buildcraft.lib.fluid.FluidSmoother.FluidStackInterp;
import buildcraft.lib.misc.RenderUtil;
import buildcraft.lib.misc.RenderUtil.AutoTessellator;
import buildcraft.lib.misc.VecUtil;

import buildcraft.factory.BCFactoryBlocks;
import buildcraft.factory.BCFactoryModels;
import buildcraft.factory.tile.TileDistiller_BC8;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.render.VertexFormat;

@Environment(EnvType.CLIENT)
public class RenderDistiller extends TileEntitySpecialRenderer<TileDistiller_BC8> {
    private static final Map<Direction, TankRenderSizes> TANK_SIZES = new EnumMap<>(Direction.class);

    static {
        Direction face = Direction.WEST;
        TankSize tankIn = new TankSize(0, 0, 4, 8, 16, 12).shrink(1 / 64.0);
        TankSize tankGasOut = new TankSize(8, 8, 0, 16, 16, 16).shrink(1 / 64.0);
        TankSize tankLiquidOut = new TankSize(8, 0, 0, 16, 8, 16).shrink(1 / 64.0);
        TankRenderSizes sizes = new TankRenderSizes(tankIn, tankGasOut, tankLiquidOut);
        for (int i = 0; i < 4; i++) {
            TANK_SIZES.put(face, sizes);
            face = face.rotateY();
            sizes = sizes.rotateY();
        }
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public void render(TileDistiller_BC8 tile, double x, double y, double z, float partialTicks, int destroyStage,
        float alpha) {
        super.render(tile, x, y, z, partialTicks, destroyStage, alpha);

        BlockState state = tile.getWorld().getBlockState(tile.getPos());
        if (state.getBlock() != BCFactoryBlocks.distiller) {
            return;
        }

        Profiler profiler = MinecraftClient.getInstance().getProfiler();
        profiler.push("bc");
        profiler.push("distiller");

        int combinedLight = tile.getWorld().getCombinedLight(tile.getPos(), 0);
        Direction face = state.get(BlockBCBase_Neptune.PROP_FACING);
        TankRenderSizes sizes = TANK_SIZES.get(face);

        // gl state setup
        RenderHelper.disableStandardItemLighting();
        com.mojang.blaze3d.systems.RenderSystem.setShaderTexture(0, net.minecraft.screen.PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);
        RenderSystem.enableBlend();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);

        // buffer setup
        try (AutoTessellator tess = RenderUtil.getThreadLocalUnusedTessellator()) {
            BufferBuilder bb = tess.tessellator.getBuffer();
            bb.begin(VertexFormat.DrawMode.QUADS, DefaultVertexFormats.BLOCK);
            // TODO(R.Chen): setTranslation removed — use MatrixStack instead: bb.setTranslation(x, y, z);

            profiler.push("model");
            profiler.push("compute");
            if (tile.clientModelData.hasNoNodes()) {
                tile.clientModelData.setNodes(BCFactoryModels.DISTILLER.createTickableNodes());
            }
            tile.setClientModelVariables(partialTicks);
            tile.clientModelData.refresh();
            MutableQuad[] quads = BCFactoryModels.DISTILLER.getCutoutQuads();
            profiler.swap("render");

            MutableQuad copy = new MutableQuad(0, null);
            int lightc = combinedLight;
            int light_block = (lightc >> 4) & 15;
            int light_sky = (lightc >> 20) & 15;
            for (MutableQuad q : quads) {
                copy.copyFrom(q);
                copy.maxLighti(light_block, light_sky);
                copy.multShade();
                copy.render(bb);
            }

            profiler.pop();
            profiler.swap("fluid");

            renderTank(sizes.tankIn, tile.smoothedTankIn, combinedLight, partialTicks, bb);
            renderTank(sizes.tankOutGas, tile.smoothedTankGasOut, combinedLight, partialTicks, bb);
            renderTank(sizes.tankOutLiquid, tile.smoothedTankLiquidOut, combinedLight, partialTicks, bb);

            // buffer finish
            // TODO(R.Chen): setTranslation removed — use MatrixStack instead: bb.setTranslation(0, 0, 0);
            profiler.swap("draw");
            tess.tessellator.draw();
        }

        // gl state finish
        RenderHelper.enableStandardItemLighting();

        profiler.pop();
        profiler.pop();
        profiler.pop();
    }

    public static void renderTank(TankSize size, FluidSmoother tank, int combinedLight, float partialTicks,
        BufferBuilder bb) {
        FluidStackInterp fluid = tank.getFluidForRender(partialTicks);
        if (fluid == null || fluid.amount <= 0) {
            return;
        }
        int blockLight = fluid.fluid.getFluid().getLuminosity(fluid.fluid) & 0xF;
        combinedLight |= blockLight << 4;
        FluidRenderer.vertex.lighti(combinedLight);
        FluidRenderer.renderFluid(FluidSpriteType.STILL, fluid.fluid, fluid.amount, tank.getCapacity(), size.min,
            size.max, bb, null);
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

    static class Size {
        final Vec3d min, max;

        public Size(int sx, int sy, int sz, int ex, int ey, int ez) {
            this(new Vec3d(sx, sy, sz).scale(1 / 16.0), new Vec3d(ex, ey, ez).scale(1 / 16.0));
        }

        public Size(Vec3d min, Vec3d max) {
            this.min = min;
            this.max = max;
        }

        public Size shrink(double by) {
            return new Size(min.add(by, by, by), max.subtract(by, by, by));
        }

        public Size rotateY() {
            Vec3d _min = rotateY(min);
            Vec3d _max = rotateY(max);
            return new Size(VecUtil.min(_min, _max), VecUtil.max(_min, _max));
        }

        private static Vec3d rotateY(Vec3d vec) {
            return new Vec3d(//
                1 - vec.z, //
                vec.y, //
                vec.x//
            );
        }
    }
}
