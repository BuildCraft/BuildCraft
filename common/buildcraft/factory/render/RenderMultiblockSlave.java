package buildcraft.factory.render;

import buildcraft.factory.BlockRefineryComponent;
import buildcraft.factory.TileMultiblockMaster;
import buildcraft.factory.TileMultiblockSlave;
import buildcraft.factory.TileMultiblockValve;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;

public class RenderMultiblockSlave implements ISimpleBlockRenderingHandler {

	public static int renderID;

	static {
		renderID = RenderingRegistry.getNextAvailableRenderId();
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
		Tessellator tessellator = Tessellator.instance;
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, -1.0F, 0.0F);
		renderer.renderFaceYNeg(block, 0.0D, 0.0D, 0.0D, block.getIcon(0, metadata));
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 1.0F, 0.0F);
		renderer.renderFaceYPos(block, 0.0D, 0.0D, 0.0D, block.getIcon(1, metadata));
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 0.0F, 1.0F);
		renderer.renderFaceXNeg(block, 0.0D, 0.0D, 0.0D, block.getIcon(2, metadata));
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 0.0F, -1.0F);
		renderer.renderFaceXPos(block, 0.0D, 0.0D, 0.0D, block.getIcon(3, metadata));
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(1.0F, 0.0F, 0.0F);
		renderer.renderFaceZNeg(block, 0.0D, 0.0D, 0.0D, block.getIcon(4, metadata));
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(-1.0F, 0.0F, 0.0F);
		renderer.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, block.getIcon(5, metadata));
		tessellator.draw();
		GL11.glTranslatef(0.5F, 0.5F, 0.5F);
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		TileEntity tile = world.getTileEntity(x, y, z);

		if (tile == null || (tile instanceof TileMultiblockMaster && !((TileMultiblockMaster) tile).formed) || (tile instanceof TileMultiblockSlave && !((TileMultiblockSlave) tile).formed)) {
			renderer.renderStandardBlock(block, x, y, z);
			return true;
		}

		// Special case for valves
		int meta = world.getBlockMetadata(x, y, z);
		if (tile != null && tile instanceof TileMultiblockValve && ((TileMultiblockSlave) tile).formed) {
			renderer.setOverrideBlockTexture(BlockRefineryComponent.icons[0][1]);
			if (meta == BlockRefineryComponent.VALVE_STEEL) {
				renderer.setRenderBounds(-0.001, -0.001, -0.001, 1.001, 1.001, 1.001);
			} else if (meta == BlockRefineryComponent.VALVE_IRON) {
				renderer.setRenderBounds(0.06, 0.06, 0.06, 1F - 0.06, 1F - 0.06, 1F - 0.06);
			}
			renderer.renderStandardBlock(block, x, y, z);
			renderer.setRenderBoundsFromBlock(block);
			renderer.clearOverrideBlockTexture();
		}

		return false;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId) {
		return true;
	}

	@Override
	public int getRenderId() {
		return renderID;
	}

}
