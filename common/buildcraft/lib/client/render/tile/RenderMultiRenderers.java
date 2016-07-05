package buildcraft.lib.client.render.tile;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.model.animation.FastTESR;

public class RenderMultiRenderers extends FastTESR<TileEntity> {
    public FastTESR[] renderers;

    public RenderMultiRenderers(FastTESR... renderers) {
        this.renderers = renderers;
    }

    @Override
    public void renderTileEntityFast(TileEntity tile, double x, double y, double z, float partialTicks, int destroyStage, VertexBuffer VertexBuffer) {
        for(FastTESR renderer : renderers) {
            renderer.renderTileEntityFast(tile, x, y, z, partialTicks, destroyStage, VertexBuffer);
        }
    }
}
