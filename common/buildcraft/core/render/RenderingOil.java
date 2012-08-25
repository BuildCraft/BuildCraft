package buildcraft.core.render;

import buildcraft.BuildCraftCore;
import net.minecraft.src.Block;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.RenderBlocks;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

public class RenderingOil implements ISimpleBlockRenderingHandler {

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID,
			RenderBlocks renderer) {}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
			Block block, int modelId, RenderBlocks renderer) {
		
		if(block.getRenderType() != BuildCraftCore.oilModel)
			return true;
		
		renderer.renderBlockFluids(block, x, y, z);
		return true;
	}

	@Override public boolean shouldRender3DInInventory() { return false; }
	@Override public int getRenderId() { return BuildCraftCore.oilModel; }

}
