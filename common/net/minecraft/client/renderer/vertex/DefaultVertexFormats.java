// STUB(R.Chen): Minecraft 1.12 DefaultVertexFormats → Fabric 1.20.1 VertexFormats.
package net.minecraft.client.renderer.vertex;

import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;

public class DefaultVertexFormats {
    public static final VertexFormat BLOCK = VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL;
    public static final VertexFormat ITEM = VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL;
    public static final VertexFormat POSITION = VertexFormats.POSITION;
    public static final VertexFormat POSITION_COLOR = VertexFormats.POSITION_COLOR;
    public static final VertexFormat POSITION_TEX = VertexFormats.POSITION_TEXTURE;
    public static final VertexFormat POSITION_TEX_COLOR = VertexFormats.POSITION_TEXTURE_COLOR;
    public static final VertexFormat POSITION_TEX_NORMAL = VertexFormats.POSITION_TEXTURE_LIGHT_NORMAL;
    public static final VertexFormat POSITION_COLOR_TEX = VertexFormats.POSITION_COLOR_TEXTURE;
}
