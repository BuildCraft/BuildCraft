package buildcraft.lib.client.render.tile;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.tileentity.TileEntity;

/** Designates a small "part" of the whole renderable machine. In future this could be extracted out to JSON files in
 * terms of arguments etc. */
public interface ITileRenderPart<T extends TileEntity> {
    void render(T tile, VertexBuffer buffer);
}
