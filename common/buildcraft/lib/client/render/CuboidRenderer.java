/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.render;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.client.model.MutableVertex;
import buildcraft.lib.client.render.fluid.FluidRenderer;
import buildcraft.lib.misc.SpriteUtil;

/** Renders something that flows. Most commonly used for fluid rendering, however power and data rendering both use this
 * as well.<br>
 * For fluid rendering look at {@link FluidRenderer} - it deals with fluid types so that you don't have to manage the
 * sprite collection. */
@SideOnly(Side.CLIENT)
public class CuboidRenderer {
    // ##############
    //
    // Public API
    //
    // ##############

    public static void renderCuboid(TextureAtlasSprite sprite, Vec3d min, Vec3d max, Vec3d offset, VertexBuffer bb, boolean[] sideRender) {
        if (sprite == null) {
            sprite = SpriteUtil.missingSprite();
        }
        if (sideRender == null) {
            sideRender = DEFAULT_FACES;
        }

        CuboidRenderContext ctx = new CuboidRenderContext();
        ctx.buffer = bb;

        final double xs = min.xCoord;
        final double ys = min.yCoord;
        final double zs = min.zCoord;

        final double xb = max.xCoord;
        final double yb = max.yCoord;
        final double zb = max.zCoord;

        // if (type == FluidSpriteType.FROZEN) {
        // if (min.xCoord > 1) {
        // xTexDiff = Math.floor(min.xCoord);
        // } else if (min.xCoord < 0) {
        // xTexDiff = Math.floor(min.xCoord);
        // } else {
        // xTexDiff = 0;
        // }
        // if (min.yCoord > 1) {
        // yTexDiff = Math.floor(min.yCoord);
        // } else if (min.yCoord < 0) {
        // yTexDiff = Math.floor(min.yCoord);
        // } else {
        // yTexDiff = 0;
        // }
        // if (min.zCoord > 1) {
        // zTexDiff = Math.floor(min.zCoord);
        // } else if (min.zCoord < 0) {
        // zTexDiff = Math.floor(min.zCoord);
        // } else {
        // zTexDiff = 0;
        // }
        // } else {
        // xTexDiff = 0;
        // yTexDiff = 0;
        // zTexDiff = 0;
        // }

        // vertex.colouri(RenderUtil.swapARGBforABGR(fluid.getFluid().getColor(fluid)));
        ctx.texmap = TexMap.XZ;
        // TODO: Enable/disable inversion for the correct faces
        ctx.invertU = false;
        ctx.invertV = false;
        if (sideRender[EnumFacing.UP.ordinal()]) {
            vertex(ctx, xs, yb, zb);
            vertex(ctx, xb, yb, zb);
            vertex(ctx, xb, yb, zs);
            vertex(ctx, xs, yb, zs);
        }

        if (sideRender[EnumFacing.DOWN.ordinal()]) {
            vertex(ctx, xs, ys, zs);
            vertex(ctx, xb, ys, zs);
            vertex(ctx, xb, ys, zb);
            vertex(ctx, xs, ys, zb);
        }

        ctx.texmap = TexMap.ZY;
        if (sideRender[EnumFacing.WEST.ordinal()]) {
            vertex(ctx, xs, ys, zs);
            vertex(ctx, xs, ys, zb);
            vertex(ctx, xs, yb, zb);
            vertex(ctx, xs, yb, zs);
        }

        if (sideRender[EnumFacing.EAST.ordinal()]) {
            vertex(ctx, xb, yb, zs);
            vertex(ctx, xb, yb, zb);
            vertex(ctx, xb, ys, zb);
            vertex(ctx, xb, ys, zs);
        }

        ctx.texmap = TexMap.XY;
        if (sideRender[EnumFacing.NORTH.ordinal()]) {
            vertex(ctx, xs, yb, zs);
            vertex(ctx, xb, yb, zs);
            vertex(ctx, xb, ys, zs);
            vertex(ctx, xs, ys, zs);
        }

        if (sideRender[EnumFacing.SOUTH.ordinal()]) {
            vertex(ctx, xs, ys, zb);
            vertex(ctx, xb, ys, zb);
            vertex(ctx, xb, yb, zb);
            vertex(ctx, xs, yb, zb);
        }
    }

    public static class CuboidRenderContext {
        public final MutableVertex vertex = new MutableVertex();
        VertexBuffer buffer;
        TextureAtlasSprite sprite;
        TexMap texmap;
        boolean invertU, invertV;
        public double xTexDiff, yTexDiff, zTexDiff;
    }

    // ##############
    //
    // Internal logic
    //
    // ##############

    private static final boolean[] DEFAULT_FACES = { true, true, true, true, true, true };

    /** Helper function to add a vertex. */
    private static void vertex(CuboidRenderContext ctx, double x, double y, double z) {
        ctx.vertex.positiond(x, y, z);
        ctx.texmap.apply(ctx, x - ctx.xTexDiff, y - ctx.yTexDiff, z - ctx.zTexDiff);
        ctx.vertex.render(ctx.buffer);
    }

    /** Used to keep track of what position maps to what texture co-ord.
     * <p>
     * For example XY maps X to U and Y to V, and ignores Z */
    private enum TexMap {
        XY(true, true),
        XZ(true, false),
        ZY(false, true);

        /** If true, then X maps to U. Otherwise Z maps to U. */
        private final boolean ux;
        /** If true, then Y maps to V. Otherwise Z maps to V. */
        private final boolean vy;

        TexMap(boolean ux, boolean vy) {
            this.ux = ux;
            this.vy = vy;
        }

        /** Changes the vertex's texture co-ord to be the same as the position, for that face.
         * 
         * (Uses {@link #ux} and {@link #vy} to determine how they are mapped). */
        void apply(CuboidRenderContext ctx, double x, double y, double z) {
            double realu = ux ? x : z;
            double realv = vy ? y : z;
            if (ctx.invertU) {
                realu = 1 - realu;
            }
            if (ctx.invertV) {
                realv = 1 - realv;
            }
            ctx.vertex.texf(ctx.sprite.getInterpolatedU(realu * 16), ctx.sprite.getInterpolatedV(realv * 16));
        }
    }
}
