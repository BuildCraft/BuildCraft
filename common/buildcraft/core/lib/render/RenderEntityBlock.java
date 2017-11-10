/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.render;

import java.util.Arrays;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import buildcraft.core.lib.EntityBlock;

public final class RenderEntityBlock extends Render {

	public static RenderEntityBlock INSTANCE = new RenderEntityBlock();
	protected RenderBlocks renderBlocks;

	private RenderEntityBlock() {
		renderBlocks = field_147909_c;
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public static class RenderInfo {

		public double minX = 0.0F;
		public double minY = 0.0F;
		public double minZ = 0.0F;
		public double maxX = 1.0F;
		public double maxY = 1.0F;
		public double maxZ = 1.0F;
		public Block baseBlock = Blocks.sand;
		public IIcon texture = null;
		public IIcon[] textureArray = null;
		public boolean[] renderSide = new boolean[]{true, true, true, true, true, true};
		public int light = -1;
		public int brightness = -1;

		public RenderInfo() {
		}

		public RenderInfo(Block template, IIcon[] texture) {
			this();
			this.baseBlock = template;
			this.textureArray = texture;
		}

		public RenderInfo(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
			this();
			setBounds(minX, minY, minZ, maxX, maxY, maxZ);
		}

		public void setSkyBlockLight(World world, int x, int y, int z, int light) {
			this.brightness = world.getSkyBlockTypeBrightness(EnumSkyBlock.Sky, x, y, z) << 16 | light;
		}

		public float getBlockBrightness(IBlockAccess iblockaccess, int i, int j, int k) {
			return baseBlock.getMixedBrightnessForBlock(iblockaccess, i, j, k);
		}

		public final void setBounds(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
			this.minX = minX;
			this.minY = minY;
			this.minZ = minZ;
			this.maxX = maxX;
			this.maxY = maxY;
			this.maxZ = maxZ;
		}

		public final void setRenderSingleSide(int side) {
			Arrays.fill(renderSide, false);
			renderSide[side] = true;
		}

		public final void setRenderAllSides() {
			Arrays.fill(renderSide, true);
		}

		public void rotate() {
			double temp = minX;
			minX = minZ;
			minZ = temp;

			temp = maxX;
			maxX = maxZ;
			maxZ = temp;
		}

		public void reverseX() {
			double temp = minX;
			minX = 1 - maxX;
			maxX = 1 - temp;
		}

		public void reverseZ() {
			double temp = minZ;
			minZ = 1 - maxZ;
			maxZ = 1 - temp;
		}

		public IIcon getBlockTextureFromSide(int i) {
			if (texture != null) {
				return texture;
			}

			int index = i;

			if (textureArray == null || textureArray.length == 0) {
				return baseBlock.getBlockTextureFromSide(index);
			} else {
				if (index >= textureArray.length) {
					index = 0;
				}

				return textureArray[index];
			}
		}
	}

	@Override
	public void doRender(Entity entity, double x, double y, double z, float f, float f1) {
		doRenderBlock((EntityBlock) entity, x, y, z);
	}

	public void doRenderBlock(EntityBlock entity, double x, double y, double z) {
		if (entity.isDead) {
			return;
		}

		shadowSize = entity.shadowSize;
		RenderInfo util = new RenderInfo();
		util.textureArray = entity.texture;
		bindTexture(TextureMap.locationBlocksTexture);

		int iMax = (int) Math.ceil(entity.iSize) - 1;
		int jMax = (int) Math.ceil(entity.jSize) - 1;
		int kMax = (int) Math.ceil(entity.kSize) - 1;

		GL11.glTranslatef((float) x, (float) y, (float) z);

		for (int iBase = 0; iBase <= iMax; ++iBase) {
			for (int jBase = 0; jBase <= jMax; ++jBase) {
				for (int kBase = 0; kBase <= kMax; ++kBase) {
					util.renderSide[0] = jBase == 0;
					util.renderSide[1] = jBase == jMax;
					util.renderSide[2] = kBase == 0;
					util.renderSide[3] = kBase == kMax;
					util.renderSide[4] = iBase == 0;
					util.renderSide[5] = iBase == iMax;

					if (util.renderSide[0] || util.renderSide[1] || util.renderSide[2]
							|| util.renderSide[3] || util.renderSide[4] || util.renderSide[5]) {
						util.minX = 0;
						util.minY = 0;
						util.minZ = 0;

						double remainX = entity.iSize - iBase;
						double remainY = entity.jSize - jBase;
						double remainZ = entity.kSize - kBase;

						util.maxX = remainX > 1.0 ? 1.0 : remainX;
						util.maxY = remainY > 1.0 ? 1.0 : remainY;
						util.maxZ = remainZ > 1.0 ? 1.0 : remainZ;

						GL11.glPushMatrix();
						GL11.glRotatef(entity.rotationX, 1, 0, 0);
						GL11.glRotatef(entity.rotationY, 0, 1, 0);
						GL11.glRotatef(entity.rotationZ, 0, 0, 1);
						GL11.glTranslatef(iBase, jBase, kBase);

						renderBlock(util);
						GL11.glPopMatrix();
					}
				}
			}
		}

		GL11.glTranslatef((float) -x, (float) -y, (float) -z);
	}

	public void renderBlock(RenderInfo info) {
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();

		renderBlocks.setRenderBounds(info.minX, info.minY, info.minZ, info.maxX, info.maxY, info.maxZ);

		if (info.light != -1) {
			tessellator.setBrightness(info.light << 20 | info.light << 4);
		} else if (info.brightness != -1) {
			tessellator.setBrightness(info.brightness << 4);
		}

		if (info.renderSide[0]) {
			tessellator.setNormal(0, -1, 0);
			renderBlocks.renderFaceYNeg(info.baseBlock, 0, 0, 0, info.getBlockTextureFromSide(0));
		}
		if (info.renderSide[1]) {
			tessellator.setNormal(0, 1, 0);
			renderBlocks.renderFaceYPos(info.baseBlock, 0, 0, 0, info.getBlockTextureFromSide(1));
		}
		if (info.renderSide[2]) {
			tessellator.setNormal(0, 0, -1);
			renderBlocks.renderFaceZNeg(info.baseBlock, 0, 0, 0, info.getBlockTextureFromSide(2));
		}
		if (info.renderSide[3]) {
			tessellator.setNormal(0, 0, 1);
			renderBlocks.renderFaceZPos(info.baseBlock, 0, 0, 0, info.getBlockTextureFromSide(3));
		}
		if (info.renderSide[4]) {
			tessellator.setNormal(-1, 0, 0);
			renderBlocks.renderFaceXNeg(info.baseBlock, 0, 0, 0, info.getBlockTextureFromSide(4));
		}
		if (info.renderSide[5]) {
			tessellator.setNormal(1, 0, 0);
			renderBlocks.renderFaceXPos(info.baseBlock, 0, 0, 0, info.getBlockTextureFromSide(5));
		}
		tessellator.draw();
	}
}