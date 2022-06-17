/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon.render;

import buildcraft.core.render.BCSimpleBlockRenderingHandler;
import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.IBlockAccess;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.core.lib.render.RenderUtils;
import buildcraft.silicon.SiliconProxy;

public class RenderLaserBlock extends BCSimpleBlockRenderingHandler {
	@Override
	public int getRenderId() {
		return SiliconProxy.laserBlockModel;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId) {
		return true;
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess iblockaccess, int x, int y, int z, Block block, int l, RenderBlocks renderblocks) {
		int meta = iblockaccess.getBlockMetadata(x, y, z);

		if (meta == ForgeDirection.EAST.ordinal()) {
			renderblocks.uvRotateEast = 2;
			renderblocks.uvRotateWest = 1;
			renderblocks.uvRotateTop = 1;
			renderblocks.uvRotateBottom = 2;

			renderblocks.setRenderBounds(0.0F, 0.0F, 0.0F, 4F / 16F, 1, 1);
			renderblocks.renderStandardBlock(block, x, y, z);

			renderblocks.setRenderBounds(4F / 16F, 5F / 16F, 5F / 16F, 13F / 16F, 11F / 16F, 11F / 16F);
			renderblocks.renderStandardBlock(block, x, y, z);
		} else if (meta == ForgeDirection.WEST.ordinal()) {
			renderblocks.uvRotateEast = 1;
			renderblocks.uvRotateWest = 2;
			renderblocks.uvRotateTop = 2;
			renderblocks.uvRotateBottom = 1;

			renderblocks.setRenderBounds(1F - 4F / 16F, 0.0F, 0.0F, 1, 1, 1);
			renderblocks.renderStandardBlock(block, x, y, z);

			renderblocks.setRenderBounds(1F - 13F / 16F, 5F / 16F, 5F / 16F, 1F - 4F / 16F, 11F / 16F, 11F / 16F);
			renderblocks.renderStandardBlock(block, x, y, z);
		} else if (meta == ForgeDirection.NORTH.ordinal()) {
			renderblocks.uvRotateSouth = 1;
			renderblocks.uvRotateNorth = 2;

			renderblocks.setRenderBounds(0.0F, 0.0F, 1F - 4F / 16F, 1, 1, 1);
			renderblocks.renderStandardBlock(block, x, y, z);

			renderblocks.setRenderBounds(5F / 16F, 5F / 16F, 1F - 13F / 16F, 11F / 16F, 11F / 16F, 1F - 4F / 16F);
			renderblocks.renderStandardBlock(block, x, y, z);
		} else if (meta == ForgeDirection.SOUTH.ordinal()) {
			renderblocks.uvRotateSouth = 2;
			renderblocks.uvRotateNorth = 1;
			renderblocks.uvRotateTop = 3;
			renderblocks.uvRotateBottom = 3;

			renderblocks.setRenderBounds(0.0F, 0.0F, 0.0F, 1, 1, 4F / 16F);
			renderblocks.renderStandardBlock(block, x, y, z);

			renderblocks.setRenderBounds(5F / 16F, 5F / 16F, 4F / 16F, 11F / 16F, 11F / 16F, 13F / 16F);
			renderblocks.renderStandardBlock(block, x, y, z);
		} else if (meta == ForgeDirection.DOWN.ordinal()) {
			renderblocks.uvRotateEast = 3;
			renderblocks.uvRotateWest = 3;
			renderblocks.uvRotateSouth = 3;
			renderblocks.uvRotateNorth = 3;

			renderblocks.setRenderBounds(0.0F, 1.0F - 4F / 16F, 0.0F, 1.0F, 1.0F, 1.0F);
			renderblocks.renderStandardBlock(block, x, y, z);

			renderblocks.setRenderBounds(5F / 16F, 1F - 13F / 16F, 5F / 16F, 11F / 16F, 1F - 4F / 16F, 11F / 16F);
			renderblocks.renderStandardBlock(block, x, y, z);
		} else if (meta == ForgeDirection.UP.ordinal()) {
			renderblocks.setRenderBounds(0.0F, 0.0F, 0.0F, 1, 0.25, 1);
			renderblocks.renderStandardBlock(block, x, y, z);

			renderblocks.setRenderBounds(5F / 16F, 4F / 16F, 5F / 16F, 11F / 16F, 13F / 16F, 11F / 16F);
			renderblocks.renderStandardBlock(block, x, y, z);
		}

		renderblocks.uvRotateEast = 0;
		renderblocks.uvRotateWest = 0;
		renderblocks.uvRotateSouth = 0;
		renderblocks.uvRotateNorth = 0;
		renderblocks.uvRotateTop = 0;
		renderblocks.uvRotateBottom = 0;

		return true;
	}

	@Override
	public void renderInventoryBlock(Block block, int i, int j, RenderBlocks renderblocks) {
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);

		renderblocks.setRenderBounds(0.0F, 0.0F, 0.0F, 1, 4F / 16F, 1);
		RenderUtils.drawBlockItem(renderblocks, Tessellator.instance, block, 1);

		renderblocks.setRenderBounds(5F / 16F, 4F / 16F, 5F / 16F, 11F / 16F, 13F / 16F, 11F / 16F);
		RenderUtils.drawBlockItem(renderblocks, Tessellator.instance, block, 1);

		GL11.glTranslatef(0.5F, 0.5F, 0.5F);
	}
}
