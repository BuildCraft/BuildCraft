/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.core.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import buildcraft.core.EntityBlock;

public class RenderEntityBlock extends Render {

    private static RenderBlocks renderBlocks = new RenderBlocks();
    static {
    }
	public static class BlockInterface {

		public double minX;
		public double minY;
		public double minZ;
		public double maxX;
		public double maxY;
		public double maxZ;

		public Block baseBlock = Block.sand;

		public Icon texture = null;

		public Icon getBlockTextureFromSide(int i) {
			if (texture == null)
				return baseBlock.getBlockTextureFromSide(i);
			else
				return texture;
		}

		public float getBlockBrightness(IBlockAccess iblockaccess, int i, int j, int k) {
			return baseBlock.getBlockBrightness(iblockaccess, i, j, k);
		}
	}

	public RenderEntityBlock() {
	}

	@Override
	public void doRender(Entity entity, double i, double j, double k, float f, float f1) {
		doRenderBlock((EntityBlock) entity, i, j, k);
	}

	public void doRenderBlock(EntityBlock entity, double i, double j, double k) {
		if (entity.isDead)
			return;

		shadowSize = entity.shadowSize;
		World world = entity.worldObj;
		BlockInterface util = new BlockInterface();
		util.texture = entity.texture;
		loadTexture("/terrain.png");

		for (int iBase = 0; iBase < entity.iSize; ++iBase) {
			for (int jBase = 0; jBase < entity.jSize; ++jBase) {
				for (int kBase = 0; kBase < entity.kSize; ++kBase) {

					util.minX = 0;
					util.minY = 0;
					util.minZ = 0;

					double remainX = entity.iSize - iBase;
					double remainY = entity.jSize - jBase;
					double remainZ = entity.kSize - kBase;

					util.maxX = (remainX > 1.0 ? 1.0 : remainX);
					util.maxY = (remainY > 1.0 ? 1.0 : remainY);
					util.maxZ = (remainZ > 1.0 ? 1.0 : remainZ);

					GL11.glPushMatrix();
					GL11.glTranslatef((float) i, (float) j, (float) k);
					GL11.glRotatef(entity.rotationX, 1, 0, 0);
					GL11.glRotatef(entity.rotationY, 0, 1, 0);
					GL11.glRotatef(entity.rotationZ, 0, 0, 1);
					GL11.glTranslatef(iBase, jBase, kBase);

					int lightX, lightY, lightZ;

					lightX = (int) (Math.floor(entity.posX) + iBase);
					lightY = (int) (Math.floor(entity.posY) + jBase);
					lightZ = (int) (Math.floor(entity.posZ) + kBase);

					GL11.glDisable(2896 /* GL_LIGHTING */);
					renderBlock(util, world, lightX, lightY, lightZ, false, true);
					GL11.glEnable(2896 /* GL_LIGHTING */);
					GL11.glPopMatrix();

				}
			}
		}
	}

	public static void renderBlock(BlockInterface block, IBlockAccess blockAccess, int i, int j, int k, boolean doLight, boolean doTessellating) {
		float f = 0.5F;
		float f1 = 1.0F;
		float f2 = 0.8F;
		float f3 = 0.6F;

        renderBlocks.renderMaxX = block.maxX;
        renderBlocks.renderMinX = block.minX;
        renderBlocks.renderMaxY = block.maxY;
        renderBlocks.renderMinY = block.minY;
        renderBlocks.renderMaxZ = block.maxZ;
        renderBlocks.renderMinZ = block.minZ;
        renderBlocks.enableAO = false;


		Tessellator tessellator = Tessellator.instance;

		if (doTessellating) {
			tessellator.startDrawingQuads();
		}

		float f4 = 0, f5 = 0;

		if (doLight) {
			f4 = block.getBlockBrightness(blockAccess, i, j, k);
			f5 = block.getBlockBrightness(blockAccess, i, j, k);
			if (f5 < f4) {
				f5 = f4;
			}
			tessellator.setColorOpaque_F(f * f5, f * f5, f * f5);
		}

		renderBlocks.renderFaceYNeg(null, 0, 0, 0, block.getBlockTextureFromSide(0));

		if (doLight) {
			f5 = block.getBlockBrightness(blockAccess, i, j, k);
			if (f5 < f4) {
				f5 = f4;
			}
			tessellator.setColorOpaque_F(f1 * f5, f1 * f5, f1 * f5);
		}

		renderBlocks.renderFaceYPos(null, 0, 0, 0, block.getBlockTextureFromSide(1));

		if (doLight) {
			f5 = block.getBlockBrightness(blockAccess, i, j, k);
			if (f5 < f4) {
				f5 = f4;
			}
			tessellator.setColorOpaque_F(f2 * f5, f2 * f5, f2 * f5);
		}

		renderBlocks.renderFaceZNeg(null, 0, 0, 0, block.getBlockTextureFromSide(2));

		if (doLight) {
			f5 = block.getBlockBrightness(blockAccess, i, j, k);
			if (f5 < f4) {
				f5 = f4;
			}
			tessellator.setColorOpaque_F(f2 * f5, f2 * f5, f2 * f5);
		}

		renderBlocks.renderFaceZPos(null, 0, 0, 0, block.getBlockTextureFromSide(3));

		if (doLight) {
			f5 = block.getBlockBrightness(blockAccess, i, j, k);
			if (f5 < f4) {
				f5 = f4;
			}
			tessellator.setColorOpaque_F(f3 * f5, f3 * f5, f3 * f5);
		}

		renderBlocks.renderFaceXNeg(null, 0, 0, 0, block.getBlockTextureFromSide(4));

		if (doLight) {
			f5 = block.getBlockBrightness(blockAccess, i, j, k);
			if (f5 < f4) {
				f5 = f4;
			}
			tessellator.setColorOpaque_F(f3 * f5, f3 * f5, f3 * f5);
		}

		renderBlocks.renderFaceXPos(null, 0, 0, 0, block.getBlockTextureFromSide(5));

		if (doTessellating) {
			tessellator.draw();
		}
	}
}
