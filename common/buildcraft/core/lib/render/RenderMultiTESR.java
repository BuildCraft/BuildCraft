package buildcraft.core.lib.render;

import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

public class RenderMultiTESR<T extends TileEntity> extends TileEntitySpecialRenderer<T> {
    private final TileEntitySpecialRenderer<T>[] renderers;

    public RenderMultiTESR(TileEntitySpecialRenderer<T>... renderers) {
        this.renderers = renderers;
        for (TileEntitySpecialRenderer<T> r : renderers) {
            r.setRendererDispatcher(TileEntityRendererDispatcher.instance);
        }
    }

    @Override
    public void renderTileEntityAt(T tile, double x, double y, double z, float f, int arg) {
        for (TileEntitySpecialRenderer<T> r : renderers) {
            r.renderTileEntityAt(tile, x, y, z, f, arg);
        }
    }
}
