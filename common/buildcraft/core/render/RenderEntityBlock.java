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

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import buildcraft.core.EntityBlock;

//TODO (1.8): Rewrite
public final class RenderEntityBlock extends Render {

	public static RenderEntityBlock INSTANCE = new RenderEntityBlock();
	//protected RenderBlocks renderBlocks;

	private RenderEntityBlock() {
		super(Minecraft.getMinecraft().getRenderManager());
		//renderBlocks = field_147909_c;
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
		public IBlockState blockState = Blocks.sand.getDefaultState();
		public ResourceLocation resource;
		public TextureAtlasSprite texture = null;
		//public IIcon[] textureArray = null;
		public boolean[] renderSide = new boolean[6];
		public float light = -1f;
		public int brightness = -1;

		public RenderInfo() {
			setRenderAllSides();
		}

		public RenderInfo(IBlockState state /*,*IIcon[] texture*/) {
			this();
			this.blockState = state;
			//this.textureArray = texture;
		}

		public RenderInfo(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
			this();
			setBounds(minX, minY, minZ, maxX, maxY, maxZ);
		}

		public float getBlockBrightness(IBlockAccess iblockaccess, BlockPos pos) {
			return blockState.getBlock().getMixedBrightnessForBlock(iblockaccess, pos);
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

		/*public IIcon getBlockTextureFromSide(int i) {
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
		}*/
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
		if(entity.blockState != null)
			util.blockState = entity.blockState;
		util.resource = entity.resource;
		if (entity.texture != null)
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

					util.maxX = remainX > 1.0 ? 1.0 : remainX;
					util.maxY = remainY > 1.0 ? 1.0 : remainY;
					util.maxZ = remainZ > 1.0 ? 1.0 : remainZ;
					//GlStateManager.enableTexture2D();
					GlStateManager.enableRescaleNormal();
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
					
					renderBlock(util, world, 0, 0, 0, new BlockPos(lightX, lightY, lightZ), false, true);
					GL11.glEnable(2896 /* GL_LIGHTING */);
					GL11.glPopMatrix();
					GlStateManager.disableRescaleNormal();

				}
			}
		}
	}

	public void renderBlock(RenderInfo info, IBlockAccess blockAccess, int x, int y, int z, boolean doLight, boolean doTessellating) {
		renderBlock(info, blockAccess, x, y, z, new BlockPos(x, y, z), doLight, doTessellating);
	}

	public void renderBlock(RenderInfo info, IBlockAccess blockAccess, double x, double y, double z, BlockPos lightPos, boolean doLight, boolean doTessellating) {

		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer renderer = tessellator.getWorldRenderer();
		BlockRendererDispatcher renderBlocks = Minecraft.getMinecraft().getBlockRendererDispatcher();
		BlockPos pos = new BlockPos(x, y, z);
		renderer.startDrawingQuads();
		renderer.setVertexFormat(DefaultVertexFormats.BLOCK);
		renderer.setTranslation((double)(-pos.getX()), (double)(-pos.getY()), (double)(-pos.getZ()));

        GlStateManager.scale(info.maxX - info.minX, info.maxY - info.minY, info.maxZ - info.minZ);
		renderBlocks.renderBlock(info.blockState, pos, blockAccess, renderer);
		tessellator.draw();
	}
}
