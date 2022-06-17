package buildcraft.core.lib.render;

import buildcraft.core.render.BCSimpleBlockRenderingHandler;
import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.IBlockAccess;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

import buildcraft.BuildCraftCore;
import buildcraft.core.lib.block.BlockBuildCraft;

public class RenderBlockComplex extends BCSimpleBlockRenderingHandler {
	private static final int[] Y_ROTATE = {3, 0, 1, 2};

	@Override
	public void renderInventoryBlock(Block block, int meta, int modelId, RenderBlocks renderer) {
		GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
		BlockBuildCraft bcBlock = (BlockBuildCraft) block;
		int pass = 0;
		while (bcBlock.canRenderInPassBC(pass)) {
			renderPassInventory(pass, bcBlock, meta, renderer);
			pass++;
		}
		GL11.glTranslatef(0.5F, 0.5F, 0.5F);
	}

	private void renderPassInventory(int pass, BlockBuildCraft block, int meta, RenderBlocks renderer) {
		if (block.isRotatable()) {
			renderer.uvRotateTop = Y_ROTATE[block.getFrontSide(meta) - 2];
			renderer.uvRotateBottom = Y_ROTATE[block.getFrontSide(meta) - 2];
		}

		RenderUtils.drawBlockItem(renderer, Tessellator.instance, block, meta);

		renderer.uvRotateTop = 0;
		renderer.uvRotateBottom = 0;
	}

	private void renderPassWorld(int pass, BlockBuildCraft block, int meta, RenderBlocks renderer, IBlockAccess world, int x, int y, int z) {
		if (block.isRotatable()) {
			renderer.uvRotateTop = Y_ROTATE[block.getFrontSide(meta) - 2];
			renderer.uvRotateBottom = Y_ROTATE[block.getFrontSide(meta) - 2];
		}

		double pDouble = (pass > 0 ? 1 : 0) / 512.0;
		renderer.setRenderBounds(block.getBlockBoundsMinX() - pDouble,
				block.getBlockBoundsMinY() - pDouble,
				block.getBlockBoundsMinZ() - pDouble,
				block.getBlockBoundsMaxX() + pDouble,
				block.getBlockBoundsMaxY() + pDouble,
				block.getBlockBoundsMaxZ() + pDouble);

		renderer.renderStandardBlock(block, x, y, z);

		renderer.uvRotateTop = 0;
		renderer.uvRotateBottom = 0;
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		BlockBuildCraft bcBlock = (BlockBuildCraft) block;
		int meta = world.getBlockMetadata(x, y, z);

		int pass = bcBlock.getCurrentRenderPass();
		while (bcBlock.canRenderInPassBC(pass)) {
			renderPassWorld(pass, bcBlock, meta, renderer, world, x, y, z);
			pass++;
		}
		return true;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId) {
		return true;
	}

	@Override
	public int getRenderId() {
		return BuildCraftCore.complexBlockModel;
	}
}
