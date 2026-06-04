// STUB(R.Chen): Minecraft 1.12 VertexFormat import shim — type alias for net.minecraft.client.render.VertexFormat.
package net.minecraft.client.renderer.vertex;

// Import the real class so this package resolves without error.
// Files using this import should be updated to net.minecraft.client.render.VertexFormat directly.
public class VertexFormat {
    public static final net.minecraft.client.render.VertexFormat BLOCK = net.minecraft.client.render.VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL;
    public static final net.minecraft.client.render.VertexFormat POSITION = net.minecraft.client.render.VertexFormats.POSITION;
    public static final net.minecraft.client.render.VertexFormat POSITION_COLOR = net.minecraft.client.render.VertexFormats.POSITION_COLOR;

    // Hide constructor
    private VertexFormat() {}
}
