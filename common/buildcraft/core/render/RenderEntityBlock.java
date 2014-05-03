/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.render;

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
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import buildcraft.core.EntityBlock;

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

		public double minX;
		public double minY;
		public double minZ;
		public double maxX;
		public double maxY;
		public double maxZ;
		public Block baseBlock = Blocks.sand;
		public IIcon texture = null;
		public IIcon[] textureArray = null;
		public boolean[] renderSide = new boolean[6];
		public float light = -1f;
		public int brightness = -1;

		public RenderInfo() {
			setRenderAllSides();
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
	public void doRender(Entity entity, double i, double j, double k, float f, float f1) {
		doRenderBlock((EntityBlock) entity, i, j, k);
	}

	public void doRenderBlock(EntityBlock entity, double i, double j, double k) {
		if (entity.isDead) {
			return;
		}

		shadowSize = entity.shadowSize;
		World world = entity.worldObj;
		RenderInfo util = new RenderInfo();
		util.texture = entity.texture;
		bindTexture(TextureMap.locationBlocksTexture);

		for (int iBase = 0; iBase < entity.iSize; ++iBase) {
			for (int jBase = 0; jBase < entity.jSize; ++jBase) {
				for (int kBase = 0; kBase < entity.kSize; ++kBase) {

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
					renderBlock(util, world, 0, 0, 0, lightX, lightY, lightZ, false, true);
					GL11.glEnable(2896 /* GL_LIGHTING */);
					GL11.glPopMatrix();

				}
			}
		}
	}

	public void renderBlock(RenderInfo info, IBlockAccess blockAccess, int x, int y, int z, boolean doLight, boolean doTessellating) {
		renderBlock(info, blockAccess, x, y, z, x, y, z, doLight, doTessellating);
	}

	public void renderBlock(RenderInfo info, IBlockAccess blockAccess, double x, double y, double z, int lightX, int lightY, int lightZ, boolean doLight, boolean doTessellating) {
		float lightBottom = 0.5F;
		float lightTop = 1.0F;
		float lightEastWest = 0.8F;
		float lightNorthSouth = 0.6F;

		Tessellator tessellator = Tessellator.instance;

		boolean realDoLight = doLight;

		if (blockAccess == null) {
			realDoLight = false;
		}

		// TODO: needs to cancel the test because the variable is now private... May need to
		// duplicate the tessellator code.
		//if (doTessellating && !tessellator.isDrawing)
			tessellator.startDrawingQuads();

		float light = 0;
		if (realDoLight) {
			if (info.light < 0) {
				light = info.baseBlock.getMixedBrightnessForBlock(blockAccess, lightX, lightY, lightZ);
				light = light + ((1.0f - light) * 0.4f);
			} else {
				light = info.light;
			}
			int brightness = 0;
			if (info.brightness < 0) {
				brightness = info.baseBlock.getMixedBrightnessForBlock(blockAccess, lightX, lightY, lightZ);
			} else {
				brightness = info.brightness;
			}
			tessellator.setBrightness(brightness);
			tessellator.setColorOpaque_F(lightBottom * light, lightBottom * light, lightBottom * light);
		} else {
//			tessellator.setColorOpaque_F(1.0F, 1.0F, 1.0F);
			if (info.brightness >= 0) {
				tessellator.setBrightness(info.brightness);
			}
		}

		renderBlocks.setRenderBounds(info.minX, info.minY, info.minZ, info.maxX, info.maxY, info.maxZ);

		if (info.renderSide[0]) {
			renderBlocks.renderFaceYNeg(info.baseBlock, x, y, z, info.getBlockTextureFromSide(0));
		}

		if (realDoLight) {
			tessellator.setColorOpaque_F(lightTop * light, lightTop * light, lightTop * light);
		}

		if (info.renderSide[1]) {
			renderBlocks.renderFaceYPos(info.baseBlock, x, y, z, info.getBlockTextureFromSide(1));
		}

		if (realDoLight) {
			tessellator.setColorOpaque_F(lightEastWest * light, lightEastWest * light, lightEastWest * light);
		}

		if (info.renderSide[2]) {
			renderBlocks.renderFaceZNeg(info.baseBlock, x, y, z, info.getBlockTextureFromSide(2));
		}

		if (realDoLight) {
			tessellator.setColorOpaque_F(lightEastWest * light, lightEastWest * light, lightEastWest * light);
		}

		if (info.renderSide[3]) {
			renderBlocks.renderFaceZPos(info.baseBlock, x, y, z, info.getBlockTextureFromSide(3));
		}

		if (realDoLight) {
			tessellator.setColorOpaque_F(lightNorthSouth * light, lightNorthSouth * light, lightNorthSouth * light);
		}

		if (info.renderSide[4]) {
			renderBlocks.renderFaceXNeg(info.baseBlock, x, y, z, info.getBlockTextureFromSide(4));
		}

		if (realDoLight) {
			tessellator.setColorOpaque_F(lightNorthSouth * light, lightNorthSouth * light, lightNorthSouth * light);
		}

		if (info.renderSide[5]) {
			renderBlocks.renderFaceXPos(info.baseBlock, x, y, z, info.getBlockTextureFromSide(5));
		}

		// TODO: needs to cancel the test because the variable is now private... May need to
		// duplicate the tessellator code.
		//if (doTessellating && tessellator.isDrawing)
		tessellator.draw();
	}
}
