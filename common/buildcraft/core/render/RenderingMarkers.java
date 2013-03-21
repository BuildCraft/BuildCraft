package buildcraft.core.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.IBlockAccess;
import buildcraft.BuildCraftCore;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

public class RenderingMarkers implements ISimpleBlockRenderingHandler {

	public RenderingMarkers() {
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {

		Tessellator tessellator = Tessellator.instance;
		float f = block.getBlockBrightness(world, x, y, z);
		if (Block.lightValue[block.blockID] > 0) {
			f = 1.0F;
		}
		tessellator.setColorOpaque_F(f, f, f);
		renderer.setOverrideBlockTexture(block.getBlockTexture(world, x, y, z, 1));
		renderer.renderBlockTorch(block, x, y, z);
		renderer.clearOverrideBlockTexture();

		return true;
	}

	@Override
	public boolean shouldRender3DInInventory() {
		return false;
	}

	@Override
	public int getRenderId() {
		return BuildCraftCore.markerModel;
	}
}
