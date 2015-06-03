package buildcraft.core.lib.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.client.registry.ISimpleBlockRenderingHandler;

import buildcraft.BuildCraftCore;
import buildcraft.core.lib.block.BlockBuildCraft;

public class RenderBlockComplex implements ISimpleBlockRenderingHandler {
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

		if (block.getIconGlowLevel() >= 0) {
			Tessellator.instance.setBrightness(block.getIconGlowLevel() << 4);
			RenderUtils.drawBlockItem(renderer, Tessellator.instance, block, meta);
		} else {
			RenderUtils.drawBlockItem(renderer, Tessellator.instance, block, meta);
		}

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

		if (block.getIconGlowLevel(world, x, y, z) >= 0) {
			Tessellator.instance.setBrightness(block.getIconGlowLevel(world, x, y, z) << 4);
			Tessellator.instance.setColorOpaque_F(1.0F, 1.0F, 1.0F);
			IIcon icon;

			icon = block.getIcon(world, x, y, z, 0);
			if (icon != BuildCraftCore.transparentTexture && (renderer.renderAllFaces || block.shouldSideBeRendered(world, x, y - 1, z, 0))) {
				renderer.renderFaceYNeg(block, x, y, z, icon);
			}
			icon = block.getIcon(world, x, y, z, 1);
			if (icon != BuildCraftCore.transparentTexture && (renderer.renderAllFaces || block.shouldSideBeRendered(world, x, y + 1, z, 1))) {
				renderer.renderFaceYPos(block, x, y, z, icon);
			}
			icon = block.getIcon(world, x, y, z, 2);
			if (icon != BuildCraftCore.transparentTexture && (renderer.renderAllFaces || block.shouldSideBeRendered(world, x, y, z - 1, 2))) {
				renderer.renderFaceZNeg(block, x, y, z, icon);
			}
			icon = block.getIcon(world, x, y, z, 3);
			if (icon != BuildCraftCore.transparentTexture && (renderer.renderAllFaces || block.shouldSideBeRendered(world, x, y, z + 1, 3))) {
				renderer.renderFaceZPos(block, x, y, z, icon);
			}
			icon = block.getIcon(world, x, y, z, 4);
			if (icon != BuildCraftCore.transparentTexture && (renderer.renderAllFaces || block.shouldSideBeRendered(world, x - 1, y, z, 4))) {
				renderer.renderFaceXNeg(block, x, y, z, icon);
			}
			icon = block.getIcon(world, x, y, z, 5);
			if (icon != BuildCraftCore.transparentTexture && (renderer.renderAllFaces || block.shouldSideBeRendered(world, x + 1, y, z, 5))) {
				renderer.renderFaceXPos(block, x, y, z, icon);
			}
		} else {
			renderer.renderStandardBlock(block, x, y, z);
		}

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
