/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.render.laser;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;

public class LaserCompiledBuffer {
    private static final int DOUBLE_STRIDE = 5;
    private static final int INT_STRIDE = 2;
    private final int vertices;
    private final double[] da;
    private final int[] ia;

    public LaserCompiledBuffer(int vertices, double[] da, int[] ia) {
        this.vertices = vertices;
        this.da = da;
        this.ia = ia;
    }

    /** Assumes the buffer uses {@link DefaultVertexFormats#BLOCK} */
    public void render(VertexBuffer buffer) {
        for (int i = 0; i < vertices; i++) {
            // POSITION_3F
            buffer.pos(da[DOUBLE_STRIDE * i + 0], da[DOUBLE_STRIDE * i + 1], da[DOUBLE_STRIDE * i + 2]);

            // COLOR_4UB
            int c = ia[INT_STRIDE * i + 0];
            buffer.color(c & 0xFF, (c >> 8) & 0xFF, (c >> 16) & 0xFF, (c >> 24) & 0xFF);

            // TEX_2F
            buffer.tex(da[DOUBLE_STRIDE * i + 3], da[DOUBLE_STRIDE * i + 4]);

            // TEX_2S
            int lmap = ia[INT_STRIDE * i + 1];
            buffer.lightmap((lmap >> 16) & 0xFFFF, lmap & 0xFFFF);

            buffer.endVertex();
        }
    }

    public static class Builder implements ILaserRenderer {
        private final boolean useNormalColour;
        private final TDoubleArrayList doubleData = new TDoubleArrayList();
        private final TIntArrayList intData = new TIntArrayList();
        private int vertices = 0;

        public Builder(boolean useNormalColour) {
            this.useNormalColour = useNormalColour;
        }

        @Override
        public void vertex(double x, double y, double z, double u, double v, int lmap, float nx, float ny, float nz, float diffuse) {
            // POSITION_3F
            doubleData.add(x);
            doubleData.add(y);
            doubleData.add(z);

            // COLOR_4UB
            if (useNormalColour) {
                int c = (int) (diffuse * 0xFF);
                intData.add(c | c << 8 | c << 16 | 0xFF << 24);
            } else {
                intData.add(0xFF_FF_FF_FF);
            }

            // TEX_2F
            doubleData.add(u);
            doubleData.add(v);

            // TEX_2S
            intData.add(lmap);

            vertices++;
        }

        public LaserCompiledBuffer build() {
            return new LaserCompiledBuffer(vertices, doubleData.toArray(), intData.toArray());
        }
    }
}
