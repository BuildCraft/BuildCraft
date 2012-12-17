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
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;

import org.lwjgl.opengl.GL11;

import buildcraft.core.DefaultProps;
import buildcraft.core.EntityBlock;

public class RenderEntityBlock extends Render {

	public static class BlockInterface {

		public double minX;
		public double minY;
		public double minZ;
		public double maxX;
		public double maxY;
		public double maxZ;

		public Block baseBlock = Block.sand;

		public int texture = -1;

		public int getBlockTextureFromSide(int i) {
			if (texture == -1)
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
					GL11.glTranslatef((float) i + 0.5F, (float) j + 0.5F, (float) k + 0.5F);
					GL11.glRotatef(entity.rotationX, 1, 0, 0);
					GL11.glRotatef(entity.rotationY, 0, 1, 0);
					GL11.glRotatef(entity.rotationZ, 0, 0, 1);
					GL11.glTranslatef(iBase, jBase, kBase);

					ForgeHooksClient.bindTexture(DefaultProps.TEXTURE_BLOCKS, 0);

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

		renderBottomFace(block, -0.5D, -0.5D, -0.5D, block.getBlockTextureFromSide(0));

		if (doLight) {
			f5 = block.getBlockBrightness(blockAccess, i, j, k);
			if (f5 < f4) {
				f5 = f4;
			}
			tessellator.setColorOpaque_F(f1 * f5, f1 * f5, f1 * f5);
		}

		renderTopFace(block, -0.5D, -0.5D, -0.5D, block.getBlockTextureFromSide(1));

		if (doLight) {
			f5 = block.getBlockBrightness(blockAccess, i, j, k);
			if (f5 < f4) {
				f5 = f4;
			}
			tessellator.setColorOpaque_F(f2 * f5, f2 * f5, f2 * f5);
		}

		renderEastFace(block, -0.5D, -0.5D, -0.5D, block.getBlockTextureFromSide(2));

		if (doLight) {
			f5 = block.getBlockBrightness(blockAccess, i, j, k);
			if (f5 < f4) {
				f5 = f4;
			}
			tessellator.setColorOpaque_F(f2 * f5, f2 * f5, f2 * f5);
		}

		renderWestFace(block, -0.5D, -0.5D, -0.5D, block.getBlockTextureFromSide(3));

		if (doLight) {
			f5 = block.getBlockBrightness(blockAccess, i, j, k);
			if (f5 < f4) {
				f5 = f4;
			}
			tessellator.setColorOpaque_F(f3 * f5, f3 * f5, f3 * f5);
		}

		renderNorthFace(block, -0.5D, -0.5D, -0.5D, block.getBlockTextureFromSide(4));

		if (doLight) {
			f5 = block.getBlockBrightness(blockAccess, i, j, k);
			if (f5 < f4) {
				f5 = f4;
			}
			tessellator.setColorOpaque_F(f3 * f5, f3 * f5, f3 * f5);
		}

		renderSouthFace(block, -0.5D, -0.5D, -0.5D, block.getBlockTextureFromSide(5));

		if (doTessellating) {
			tessellator.draw();
		}
	}

	public static void renderBottomFace(BlockInterface block, double d, double d1, double d2, int i) {
		Tessellator tessellator = Tessellator.instance;

		int j = (i & 0xf) << 4;
		int k = i & 0xf0;
		double d3 = (j + block.minX * 16D) / 256D;
		double d4 = ((j + block.maxX * 16D) - 0.01D) / 256D;
		double d5 = (k + block.minZ * 16D) / 256D;
		double d6 = ((k + block.maxZ * 16D) - 0.01D) / 256D;
		if (block.minX < 0.0D || block.maxX > 1.0D) {
			d3 = (j + 0.0F) / 256F;
			d4 = (j + 15.99F) / 256F;
		}
		if (block.minZ < 0.0D || block.maxZ > 1.0D) {
			d5 = (k + 0.0F) / 256F;
			d6 = (k + 15.99F) / 256F;
		}
		double d7 = d + block.minX;
		double d8 = d + block.maxX;
		double d9 = d1 + block.minY;
		double d10 = d2 + block.minZ;
		double d11 = d2 + block.maxZ;

		tessellator.addVertexWithUV(d7, d9, d11, d3, d6);
		tessellator.addVertexWithUV(d7, d9, d10, d3, d5);
		tessellator.addVertexWithUV(d8, d9, d10, d4, d5);
		tessellator.addVertexWithUV(d8, d9, d11, d4, d6);
	}

	public static void renderTopFace(BlockInterface block, double d, double d1, double d2, int i) {
		Tessellator tessellator = Tessellator.instance;

		int j = (i & 0xf) << 4;
		int k = i & 0xf0;
		double d3 = (j + block.minX * 16D) / 256D;
		double d4 = ((j + block.maxX * 16D) - 0.01D) / 256D;
		double d5 = (k + block.minZ * 16D) / 256D;
		double d6 = ((k + block.maxZ * 16D) - 0.01D) / 256D;
		if (block.minX < 0.0D || block.maxX > 1.0D) {
			d3 = (j + 0.0F) / 256F;
			d4 = (j + 15.99F) / 256F;
		}
		if (block.minZ < 0.0D || block.maxZ > 1.0D) {
			d5 = (k + 0.0F) / 256F;
			d6 = (k + 15.99F) / 256F;
		}
		double d7 = d + block.minX;
		double d8 = d + block.maxX;
		double d9 = d1 + block.maxY;
		double d10 = d2 + block.minZ;
		double d11 = d2 + block.maxZ;

		tessellator.addVertexWithUV(d8, d9, d11, d4, d6);
		tessellator.addVertexWithUV(d8, d9, d10, d4, d5);
		tessellator.addVertexWithUV(d7, d9, d10, d3, d5);
		tessellator.addVertexWithUV(d7, d9, d11, d3, d6);
	}

	public static void renderEastFace(BlockInterface block, double d, double d1, double d2, int i) {
		Tessellator tessellator = Tessellator.instance;

		int j = (i & 0xf) << 4;
		int k = i & 0xf0;
		double d3 = (j + block.minX * 16D) / 256D;
		double d4 = ((j + block.maxX * 16D) - 0.01D) / 256D;
		double d5 = (k + block.minY * 16D) / 256D;
		double d6 = ((k + block.maxY * 16D) - 0.01D) / 256D;

		if (block.minX < 0.0D || block.maxX > 1.0D) {
			d3 = (j + 0.0F) / 256F;
			d4 = (j + 15.99F) / 256F;
		}
		if (block.minY < 0.0D || block.maxY > 1.0D) {
			d5 = (k + 0.0F) / 256F;
			d6 = (k + 15.99F) / 256F;
		}
		double d8 = d + block.minX;
		double d9 = d + block.maxX;
		double d10 = d1 + block.minY;
		double d11 = d1 + block.maxY;
		double d12 = d2 + block.minZ;

		tessellator.addVertexWithUV(d8, d11, d12, d4, d5);
		tessellator.addVertexWithUV(d9, d11, d12, d3, d5);
		tessellator.addVertexWithUV(d9, d10, d12, d3, d6);
		tessellator.addVertexWithUV(d8, d10, d12, d4, d6);
	}

	public static void renderWestFace(BlockInterface block, double d, double d1, double d2, int i) {
		Tessellator tessellator = Tessellator.instance;

		int j = (i & 0xf) << 4;
		int k = i & 0xf0;
		double d3 = (j + block.minX * 16D) / 256D;
		double d4 = ((j + block.maxX * 16D) - 0.01D) / 256D;
		double d5 = (k + block.minY * 16D) / 256D;
		double d6 = ((k + block.maxY * 16D) - 0.01D) / 256D;

		if (block.minX < 0.0D || block.maxX > 1.0D) {
			d3 = (j + 0.0F) / 256F;
			d4 = (j + 15.99F) / 256F;
		}
		if (block.minY < 0.0D || block.maxY > 1.0D) {
			d5 = (k + 0.0F) / 256F;
			d6 = (k + 15.99F) / 256F;
		}
		double d8 = d + block.minX;
		double d9 = d + block.maxX;
		double d10 = d1 + block.minY;
		double d11 = d1 + block.maxY;
		double d12 = d2 + block.maxZ;

		tessellator.addVertexWithUV(d8, d11, d12, d3, d5);
		tessellator.addVertexWithUV(d8, d10, d12, d3, d6);
		tessellator.addVertexWithUV(d9, d10, d12, d4, d6);
		tessellator.addVertexWithUV(d9, d11, d12, d4, d5);
	}

	public static void renderNorthFace(BlockInterface block, double d, double d1, double d2, int i) {
		Tessellator tessellator = Tessellator.instance;

		int j = (i & 0xf) << 4;
		int k = i & 0xf0;
		double d3 = (j + block.minZ * 16D) / 256D;
		double d4 = ((j + block.maxZ * 16D) - 0.01D) / 256D;
		double d5 = (k + block.minY * 16D) / 256D;
		double d6 = ((k + block.maxY * 16D) - 0.01D) / 256D;

		if (block.minZ < 0.0D || block.maxZ > 1.0D) {
			d3 = (j + 0.0F) / 256F;
			d4 = (j + 15.99F) / 256F;
		}
		if (block.minY < 0.0D || block.maxY > 1.0D) {
			d5 = (k + 0.0F) / 256F;
			d6 = (k + 15.99F) / 256F;
		}
		double d8 = d + block.minX;
		double d9 = d1 + block.minY;
		double d10 = d1 + block.maxY;
		double d11 = d2 + block.minZ;
		double d12 = d2 + block.maxZ;

		tessellator.addVertexWithUV(d8, d10, d12, d4, d5);
		tessellator.addVertexWithUV(d8, d10, d11, d3, d5);
		tessellator.addVertexWithUV(d8, d9, d11, d3, d6);
		tessellator.addVertexWithUV(d8, d9, d12, d4, d6);
	}

	public static void renderSouthFace(BlockInterface block, double d, double d1, double d2, int i) {
		Tessellator tessellator = Tessellator.instance;

		int j = (i & 0xf) << 4;
		int k = i & 0xf0;
		double d3 = (j + block.minZ * 16D) / 256D;
		double d4 = ((j + block.maxZ * 16D) - 0.01D) / 256D;
		double d5 = (k + block.minY * 16D) / 256D;
		double d6 = ((k + block.maxY * 16D) - 0.01D) / 256D;

		if (block.minZ < 0.0D || block.maxZ > 1.0D) {
			d3 = (j + 0.0F) / 256F;
			d4 = (j + 15.99F) / 256F;
		}
		if (block.minY < 0.0D || block.maxY > 1.0D) {
			d5 = (k + 0.0F) / 256F;
			d6 = (k + 15.99F) / 256F;
		}
		double d8 = d + block.maxX;
		double d9 = d1 + block.minY;
		double d10 = d1 + block.maxY;
		double d11 = d2 + block.minZ;
		double d12 = d2 + block.maxZ;

		tessellator.addVertexWithUV(d8, d9, d12, d3, d6);
		tessellator.addVertexWithUV(d8, d9, d11, d4, d6);
		tessellator.addVertexWithUV(d8, d10, d11, d4, d5);
		tessellator.addVertexWithUV(d8, d10, d12, d3, d5);
	}

}
