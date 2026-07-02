/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.client.render.laser;


import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;

import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


@OnlyIn(Dist.CLIENT)
public class LaserCompiledBuffer {
    private final int vertices;
    private final float[] data;

    public LaserCompiledBuffer(int vertices, float[] data) {
        this.vertices = vertices;
		this.data = data;
    }

    /** Assumes the buffer uses {@link DefaultVertexFormats#BLOCK} */
    public void render(Matrix4f pose, Matrix3f normal, VertexConsumer buffer) {
    	int counter = 0;
//    	BCLog.logger.debug(""+vertices);
        for (int i = 0; i < vertices; i++) {
            // POSITION_3F
            buffer.vertex(pose, data[counter++], data[counter++], data[counter++]);

            // COLOR_4UB
            int c = (int)data[counter++];
            buffer.color(c & 0xFF, (c >> 8) & 0xFF, (c >> 16) & 0xFF, (c >> 24) & 0xFF);

            // TEX_2F
            buffer.uv(data[counter++]+0.00f, data[counter++]+0.000f);

            // TEX_2S
            int lmap = (int)data[counter++];
            buffer.uv2(lmap);
            buffer.normal(normal, data[counter++], data[counter++], data[counter++]);
            buffer.overlayCoords(OverlayTexture.NO_OVERLAY);

            buffer.endVertex();
        }
    }

    public static class Builder implements ILaserRenderer {
        private final boolean useNormalColour;
        private final FloatList floatData = new FloatArrayList();//pos + color + uv + normal
        private int vertices = 0;

        public Builder(boolean useNormalColour) {
            this.useNormalColour = useNormalColour;
        }

        @Override
        public void vertex(float x, float y, float z, float u, float v, int lmap, float nx, float ny, float nz, float diffuse) {
            // POSITION_3F
        	floatData.add(x);
        	floatData.add(y);
        	floatData.add(z);

            // COLOR_4UB
            if (useNormalColour) {
                int c = (int) (diffuse * 0xFF);
                floatData.add(c | c << 8 | c << 16 | 0xFF << 24);
            } else {
            	floatData.add(0xFF_FF_FF_FF);
            }

            // TEX_2F
            floatData.add(u);
            floatData.add(v);

            // TEX_2S
            floatData.add(lmap);
            
            // Normal
            floatData.add(nx);
            floatData.add(ny);
            floatData.add(nz);

            vertices++;
        }

        public LaserCompiledBuffer build() {
            return new LaserCompiledBuffer(vertices, floatData.toFloatArray());
        }
    }
}
