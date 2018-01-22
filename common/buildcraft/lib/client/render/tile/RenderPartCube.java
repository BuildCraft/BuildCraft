/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.render.tile;

import javax.vecmath.Point3f;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.client.model.MutableVertex;

/** A variable sized element (like LED) that can render somewhere in a TESR. Passing a resolver instance will let you
 * modify the location, colour, lightmap, and size of the single element. This does not allow for different textures. */
@SideOnly(Side.CLIENT)
public class RenderPartCube {
    /** The centre of this element. */
    public final MutableVertex center = new MutableVertex();
    public double sizeX = 1 / 16.0, sizeY = 1 / 16.0, sizeZ = 1 / 16.0;

    /** Constructs a simple cube configured for a LED. */
    public RenderPartCube() {
        this(1 / 16.0, 1 / 16.0, 1 / 16.0);
    }

    public RenderPartCube(double x, double y, double z) {
        center.positiond(x, y, z);
    }

    public void setWhiteTex() {
        TextureAtlasSprite sprite = ModelLoader.White.INSTANCE;
        // Reset the vertex so that edits don't spill out to other tiles.
        center.texf(sprite.getInterpolatedU(8), sprite.getInterpolatedV(8));
    }

    /** Renders an element, without changing the vertex. However this does ignore the "normal" and "texture" components
     * of the vertex. */
    public void render(VertexBuffer bb) {
        Point3f pos = center.positionvf();
        double x = pos.x;
        double y = pos.y;
        double z = pos.z;

        double rX = sizeX / 2;
        double rY = sizeY / 2;
        double rZ = sizeZ / 2;

        vertex(bb, center, x - rX, y + rY, z + rZ);
        vertex(bb, center, x + rX, y + rY, z + rZ);
        vertex(bb, center, x + rX, y + rY, z - rZ);
        vertex(bb, center, x - rX, y + rY, z - rZ);

        vertex(bb, center, x - rX, y - rY, z - rZ);
        vertex(bb, center, x + rX, y - rY, z - rZ);
        vertex(bb, center, x + rX, y - rY, z + rZ);
        vertex(bb, center, x - rX, y - rY, z + rZ);

        vertex(bb, center, x - rX, y - rY, z + rZ);
        vertex(bb, center, x - rX, y + rY, z + rZ);
        vertex(bb, center, x - rX, y + rY, z - rZ);
        vertex(bb, center, x - rX, y - rY, z - rZ);

        vertex(bb, center, x + rX, y - rY, z - rZ);
        vertex(bb, center, x + rX, y + rY, z - rZ);
        vertex(bb, center, x + rX, y + rY, z + rZ);
        vertex(bb, center, x + rX, y - rY, z + rZ);

        vertex(bb, center, x - rX, y - rY, z - rZ);
        vertex(bb, center, x - rX, y + rY, z - rZ);
        vertex(bb, center, x + rX, y + rY, z - rZ);
        vertex(bb, center, x + rX, y - rY, z - rZ);

        vertex(bb, center, x + rX, y - rY, z + rZ);
        vertex(bb, center, x + rX, y + rY, z + rZ);
        vertex(bb, center, x - rX, y + rY, z + rZ);
        vertex(bb, center, x - rX, y - rY, z + rZ);
    }

    private static void vertex(VertexBuffer vb, MutableVertex center, double x, double y, double z) {
        // Using DefaultVertexFormats.BLOCK
        // -- POSITION_3F // pos
        // -- COLOR_4UB // colour
        // -- TEX_2F // texture
        // -- TEX_2S // lightmap
        vb.pos(x, y, z);
        center.renderColour(vb);
        center.renderTex(vb);
        center.renderLightMap(vb);
        vb.endVertex();
    }
}
