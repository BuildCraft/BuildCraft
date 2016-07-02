package buildcraft.lib.client.render.tile;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.tileentity.TileEntity;

public interface ITileRenderPart<T extends TileEntity> {
    void render(T tile, VertexBuffer buffer);
}
