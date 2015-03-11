package buildcraft.core.lib.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import buildcraft.BuildCraftCore;
import buildcraft.core.lib.block.BlockBuildCraft;

public class RenderBlockMultipass implements ISimpleBlockRenderingHandler {
	@Override
	public void renderInventoryBlock(Block block, int meta, int modelId, RenderBlocks renderer) {
		BlockBuildCraft bcBlock = (BlockBuildCraft) block;
		int pass = 0;
		while (bcBlock.canRenderInPass(pass)) {
			renderPassInventory(pass, bcBlock, meta, renderer);
			pass++;
		}
	}

	private void renderPassInventory(int pass, BlockBuildCraft block, int meta, RenderBlocks renderer) {
		if (block.getIconGlowLevel() >= 0) {
			Tessellator.instance.setBrightness(block.getIconGlowLevel() << 4);
			RenderUtils.drawBlockItem(renderer, Tessellator.instance, block, meta);
		} else {
			RenderUtils.drawBlockItem(renderer, Tessellator.instance, block, meta);
		}
	}

	private void renderPassWorld(int pass, BlockBuildCraft block, int meta, RenderBlocks renderer, IBlockAccess world, int x, int y, int z) {
		if (block.getIconGlowLevel(world, x, y, z) >= 0) {
			Tessellator.instance.setBrightness(block.getIconGlowLevel(world, x, y, z) << 4);
			Tessellator.instance.setColorOpaque_F(1.0F, 1.0F, 1.0F);
			IIcon icon;

			icon = block.getIcon(world, x, y, z, 0);
			if (icon != null && (renderer.renderAllFaces || block.shouldSideBeRendered(world, x, y - 1, z, 0))) {
				renderer.renderFaceYNeg(block, x, y, z, icon);
			}
			icon = block.getIcon(world, x, y, z, 1);
			if (icon != null && (renderer.renderAllFaces || block.shouldSideBeRendered(world, x, y + 1, z, 1))) {
				renderer.renderFaceYPos(block, x, y, z, icon);
			}
			icon = block.getIcon(world, x, y, z, 2);
			if (icon != null && (renderer.renderAllFaces || block.shouldSideBeRendered(world, x, y, z - 1, 2))) {
				renderer.renderFaceZNeg(block, x, y, z, icon);
			}
			icon = block.getIcon(world, x, y, z, 3);
			if (icon != null && (renderer.renderAllFaces || block.shouldSideBeRendered(world, x, y, z + 1, 3))) {
				renderer.renderFaceZPos(block, x, y, z, icon);
			}
			icon = block.getIcon(world, x, y, z, 4);
			if (icon != null && (renderer.renderAllFaces || block.shouldSideBeRendered(world, x - 1, y, z, 4))) {
				renderer.renderFaceXNeg(block, x, y, z, icon);
			}
			icon = block.getIcon(world, x, y, z, 5);
			if (icon != null && (renderer.renderAllFaces || block.shouldSideBeRendered(world, x + 1, y, z, 5))) {
				renderer.renderFaceXPos(block, x, y, z, icon);
			}
		} else {
			renderer.renderStandardBlockWithColorMultiplier(block, x, y, z, 1.0f, 1.0f, 1.0f);
		}
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		BlockBuildCraft bcBlock = (BlockBuildCraft) block;
		int meta = world.getBlockMetadata(x, y, z);

		if (bcBlock.getCurrentRenderPass() > 0) {
			int pass = bcBlock.getCurrentRenderPass();
			while (bcBlock.canRenderInPass(pass)) {
				renderPassWorld(pass, bcBlock, meta, renderer, world, x, y, z);
				pass++;
			}
		} else {
			if (bcBlock.canRenderInPass(0)) {
				renderPassWorld(0, bcBlock, meta, renderer, world, x, y, z);
			}
		}
		return true;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId) {
		return true;
	}

	@Override
	public int getRenderId() {
		return BuildCraftCore.multipassModel;
	}
}
