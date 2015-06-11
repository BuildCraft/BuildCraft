package buildcraft.core.lib.render;

import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

public class RenderMultiTESR extends TileEntitySpecialRenderer {
	private final TileEntitySpecialRenderer[] renderers;

	public RenderMultiTESR(TileEntitySpecialRenderer[] renderers) {
		this.renderers = renderers;
		for (TileEntitySpecialRenderer r : renderers) {
			r.func_147497_a(TileEntityRendererDispatcher.instance);
		}
	}

	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float f) {
		for (TileEntitySpecialRenderer r : renderers) {
			r.renderTileEntityAt(tile, x, y, z, f);
		}
	}
}
