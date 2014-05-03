/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.render;

import java.util.HashMap;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

import buildcraft.BuildCraftCore;
import buildcraft.core.CoreConstants;
import buildcraft.core.IInventoryRenderer;
import buildcraft.core.utils.Utils;

public class RenderingEntityBlocks implements ISimpleBlockRenderingHandler {

	public static HashMap<EntityRenderIndex, IInventoryRenderer> blockByEntityRenders = new HashMap<EntityRenderIndex, IInventoryRenderer>();
	private static final ResourceLocation BLOCK_TEXTURE = TextureMap.locationBlocksTexture;

	public static class EntityRenderIndex {
		Block block;
		int damage;

		public EntityRenderIndex(Block block, int damage) {
			this.block = block;
			this.damage = damage;
		}

		@Override
		public int hashCode() {
			return block.hashCode() + damage;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof EntityRenderIndex)) {
				return false;
			}

			EntityRenderIndex i = (EntityRenderIndex) o;

			return i.block == block && i.damage == damage;
		}
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {

		if (block.getRenderType() == BuildCraftCore.blockByEntityModel) {

			EntityRenderIndex index = new EntityRenderIndex(block, metadata);
			if (blockByEntityRenders.containsKey(index)) {
				blockByEntityRenders.get(index).inventoryRender(-0.5, -0.5, -0.5, 0, 0);
			}

		} else if (block.getRenderType() == BuildCraftCore.legacyPipeModel) {
			Tessellator tessellator = Tessellator.instance;

			block.setBlockBounds(CoreConstants.PIPE_MIN_POS, 0.0F, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MAX_POS, 1.0F, CoreConstants.PIPE_MAX_POS);
			renderer.setRenderBoundsFromBlock(block);
			block.setBlockBoundsForItemRender();
			GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
			tessellator.startDrawingQuads();
			tessellator.setNormal(0.0F, -1F, 0.0F);
			renderer.renderFaceYNeg(block, 0.0D, 0.0D, 0.0D, block.getIcon(0, metadata));
			tessellator.draw();
			tessellator.startDrawingQuads();
			tessellator.setNormal(0.0F, 1.0F, 0.0F);
			renderer.renderFaceYPos(block, 0.0D, 0.0D, 0.0D, block.getIcon(1, metadata));
			tessellator.draw();
			tessellator.startDrawingQuads();
			tessellator.setNormal(0.0F, 0.0F, -1F);
			renderer.renderFaceZNeg(block, 0.0D, 0.0D, 0.0D, block.getIcon(2, metadata));
			tessellator.draw();
			tessellator.startDrawingQuads();
			tessellator.setNormal(0.0F, 0.0F, 1.0F);
			renderer.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, block.getIcon(3, metadata));
			tessellator.draw();
			tessellator.startDrawingQuads();
			tessellator.setNormal(-1F, 0.0F, 0.0F);
			renderer.renderFaceXNeg(block, 0.0D, 0.0D, 0.0D, block.getIcon(4, metadata));
			tessellator.draw();
			tessellator.startDrawingQuads();
			tessellator.setNormal(1.0F, 0.0F, 0.0F);
			renderer.renderFaceXPos(block, 0.0D, 0.0D, 0.0D, block.getIcon(5, metadata));
			tessellator.draw();
			GL11.glTranslatef(0.5F, 0.5F, 0.5F);
			block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
		}
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {

		if (block.getRenderType() == BuildCraftCore.blockByEntityModel) {
			// renderblocks.renderStandardBlock(block, i, j, k);
		} else if (block.getRenderType() == BuildCraftCore.legacyPipeModel) {
			Minecraft.getMinecraft().renderEngine.bindTexture(BLOCK_TEXTURE);
			legacyPipeRender(renderer, world, x, y, z, block, modelId);

		}

		return true;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId) {
		return true;
	}

	@Override
	public int getRenderId() {
		return BuildCraftCore.blockByEntityModel;
	}

	/* LEGACY PIPE RENDERING and quarry frames! */
	private void legacyPipeRender(RenderBlocks renderblocks, IBlockAccess iblockaccess, int i, int j, int k, Block block, int l) {
		float minSize = CoreConstants.PIPE_MIN_POS;
		float maxSize = CoreConstants.PIPE_MAX_POS;

		block.setBlockBounds(minSize, minSize, minSize, maxSize, maxSize, maxSize);
		renderblocks.setRenderBoundsFromBlock(block);
		renderblocks.renderStandardBlock(block, i, j, k);

		if (Utils.checkLegacyPipesConnections(iblockaccess, i, j, k, i - 1, j, k)) {
			block.setBlockBounds(0.0F, minSize, minSize, minSize, maxSize, maxSize);
			renderblocks.setRenderBoundsFromBlock(block);
			renderblocks.renderStandardBlock(block, i, j, k);
		}

		if (Utils.checkLegacyPipesConnections(iblockaccess, i, j, k, i + 1, j, k)) {
			block.setBlockBounds(maxSize, minSize, minSize, 1.0F, maxSize, maxSize);
			renderblocks.setRenderBoundsFromBlock(block);
			renderblocks.renderStandardBlock(block, i, j, k);
		}

		if (Utils.checkLegacyPipesConnections(iblockaccess, i, j, k, i, j - 1, k)) {
			block.setBlockBounds(minSize, 0.0F, minSize, maxSize, minSize, maxSize);
			renderblocks.setRenderBoundsFromBlock(block);
			renderblocks.renderStandardBlock(block, i, j, k);
		}

		if (Utils.checkLegacyPipesConnections(iblockaccess, i, j, k, i, j + 1, k)) {
			block.setBlockBounds(minSize, maxSize, minSize, maxSize, 1.0F, maxSize);
			renderblocks.setRenderBoundsFromBlock(block);
			renderblocks.renderStandardBlock(block, i, j, k);
		}

		if (Utils.checkLegacyPipesConnections(iblockaccess, i, j, k, i, j, k - 1)) {
			block.setBlockBounds(minSize, minSize, 0.0F, maxSize, maxSize, minSize);
			renderblocks.setRenderBoundsFromBlock(block);
			renderblocks.renderStandardBlock(block, i, j, k);
		}

		if (Utils.checkLegacyPipesConnections(iblockaccess, i, j, k, i, j, k + 1)) {
			block.setBlockBounds(minSize, minSize, maxSize, maxSize, maxSize, 1.0F);
			renderblocks.setRenderBoundsFromBlock(block);
			renderblocks.renderStandardBlock(block, i, j, k);
		}

		block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
	}


}
