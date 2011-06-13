package net.minecraft.src;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix2f;

import net.minecraft.src.buildcraft.api.EntityPassiveItem;
import net.minecraft.src.buildcraft.api.IBlockPipe;
import net.minecraft.src.buildcraft.api.IPipeConnection;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.core.EntityBlock;
import net.minecraft.src.buildcraft.core.ICustomTextureBlock;
import net.minecraft.src.buildcraft.core.RenderEntityBlock;
import net.minecraft.src.buildcraft.core.Utils;

public class mod_BuildCraftCore extends BaseModMp {

	BuildCraftCore proxy = new BuildCraftCore();

	public static HashMap<Block, Render> blockByEntityRenders = new HashMap<Block, Render>();

	public static void initialize() {
		BuildCraftCore.initialize();
	}

	public void ModsLoaded() {
		mod_BuildCraftCore.initialize();
		BuildCraftCore.initializeModel(this);
	}

	@Override
	public String Version() {
		return version();
	}

	public static String version() {
		return "1.6.6.3";
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void AddRenderer(Map map) {
		map.put(EntityPassiveItem.class, new RenderPassiveItem());
		map.put(EntityBlock.class, new RenderEntityBlock());
	}

	public boolean RenderWorldBlock(RenderBlocks renderblocks,
			IBlockAccess iblockaccess, int i, int j, int k, Block block, int l) {
		if (block instanceof ICustomTextureBlock) {
			Tessellator tessellator = Tessellator.instance;
			tessellator.draw();
			tessellator.startDrawingQuads();
			tessellator.setNormal(0.0F, -1F, 0.0F);

			GL11.glBindTexture(3553 /* GL_TEXTURE_2D */, ModLoader
					.getMinecraftInstance().renderEngine
					.getTexture(((ICustomTextureBlock) (block))
							.getTextureFile()));
		}

		if (block.getRenderType() == BuildCraftCore.blockByEntityModel) {
			renderblocks.renderStandardBlock(block, i, j, k);

			return true;
		} else if (block.getRenderType() == BuildCraftCore.markerModel) {
			Tessellator tessellator = Tessellator.instance;
			float f = block.getBlockBrightness(iblockaccess, i, j, k);
			if (Block.lightValue[block.blockID] > 0) {
				f = 1.0F;
			}
			tessellator.setColorOpaque_F(f, f, f);
			renderMarkerWithMeta(block, i, j, k,
					iblockaccess.getBlockMetadata(i, j, k));
		} else if (block.getRenderType() == BuildCraftCore.customTextureModel) {
			renderblocks.renderStandardBlock(block, i, j, k);
		} else if (block.getRenderType() == BuildCraftCore.pipeModel) {
			float minSize = Utils.pipeMinSize;
			float maxSize = Utils.pipeMaxSize;
			int initialTexture = block.blockIndexInTexture;

			block.setBlockBounds(minSize, minSize, minSize, maxSize, maxSize,
					maxSize);
			renderblocks.renderStandardBlock(block, i, j, k);

			int metadata = iblockaccess.getBlockMetadata(i, j, k);

			IPipeConnection connect = (IPipeConnection) block;

			if (connect.isPipeConnected(iblockaccess, i - 1, j, k)) {
				block.blockIndexInTexture = ((IBlockPipe) block)
						.getTextureForConnection(Orientations.XNeg, metadata);
				block.setBlockBounds(0.0F, minSize, minSize, minSize, maxSize,
						maxSize);
				renderblocks.renderStandardBlock(block, i, j, k);
				block.blockIndexInTexture = initialTexture;
			}

			if (connect.isPipeConnected(iblockaccess, i + 1, j, k)) {
				block.blockIndexInTexture = ((IBlockPipe) block)
						.getTextureForConnection(Orientations.XPos, metadata);
				block.setBlockBounds(maxSize, minSize, minSize, 1.0F, maxSize,
						maxSize);
				renderblocks.renderStandardBlock(block, i, j, k);
				block.blockIndexInTexture = initialTexture;
			}

			if (connect.isPipeConnected(iblockaccess, i, j - 1, k)) {
				block.blockIndexInTexture = ((IBlockPipe) block)
						.getTextureForConnection(Orientations.YNeg, metadata);
				block.setBlockBounds(minSize, 0.0F, minSize, maxSize, minSize,
						maxSize);
				renderblocks.renderStandardBlock(block, i, j, k);
				block.blockIndexInTexture = initialTexture;
			}

			if (connect.isPipeConnected(iblockaccess, i, j + 1, k)) {
				block.blockIndexInTexture = ((IBlockPipe) block)
						.getTextureForConnection(Orientations.YPos, metadata);
				block.setBlockBounds(minSize, maxSize, minSize, maxSize, 1.0F,
						maxSize);
				renderblocks.renderStandardBlock(block, i, j, k);
				block.blockIndexInTexture = initialTexture;
			}

			if (connect.isPipeConnected(iblockaccess, i, j, k - 1)) {
				block.blockIndexInTexture = ((IBlockPipe) block)
						.getTextureForConnection(Orientations.ZNeg, metadata);
				block.setBlockBounds(minSize, minSize, 0.0F, maxSize, maxSize,
						minSize);
				renderblocks.renderStandardBlock(block, i, j, k);
				block.blockIndexInTexture = initialTexture;
			}

			if (connect.isPipeConnected(iblockaccess, i, j, k + 1)) {
				block.blockIndexInTexture = ((IBlockPipe) block)
						.getTextureForConnection(Orientations.ZPos, metadata);
				block.setBlockBounds(minSize, minSize, maxSize, maxSize,
						maxSize, 1.0F);
				renderblocks.renderStandardBlock(block, i, j, k);
				block.blockIndexInTexture = initialTexture;
			}

			block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
		}

		if (block instanceof ICustomTextureBlock) {
			Tessellator tessellator = Tessellator.instance;
			tessellator.draw();
			tessellator.startDrawingQuads();

			GL11.glBindTexture(3553 /* GL_TEXTURE_2D */, ModLoader
					.getMinecraftInstance().renderEngine
					.getTexture("/terrain.png"));

			return true;
		}

		return false;
	}

	RenderItem itemRenderer = new RenderItem();

	public void RenderInvBlock(RenderBlocks renderblocks, Block block, int i,
			int j) {
		if (block instanceof ICustomTextureBlock) {
			GL11.glBindTexture(3553 /* GL_TEXTURE_2D */, ModLoader
					.getMinecraftInstance().renderEngine
					.getTexture(((ICustomTextureBlock) (block))
							.getTextureFile()));
		}

		if (block.getRenderType() == BuildCraftCore.blockByEntityModel
				&& blockByEntityRenders.containsKey(block)) {
			// ??? GET THE ENTITY FROM THE TILE
			blockByEntityRenders.get(block).doRender(null, -0.5, -0.5, -0.5, 0,
					0);
		} else if (block.getRenderType() == BuildCraftCore.markerModel) {
			// Do nothing here...
		} else if (block.getRenderType() == BuildCraftCore.customTextureModel) {
			Tessellator tessellator = Tessellator.instance;
			block.setBlockBoundsForItemRender();
			GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
			tessellator.startDrawingQuads();
			tessellator.setNormal(0.0F, -1F, 0.0F);
			renderblocks.renderBottomFace(block, 0.0D, 0.0D, 0.0D,
					block.getBlockTextureFromSideAndMetadata(0, i));
			tessellator.draw();
			tessellator.startDrawingQuads();
			tessellator.setNormal(0.0F, 1.0F, 0.0F);
			renderblocks.renderTopFace(block, 0.0D, 0.0D, 0.0D,
					block.getBlockTextureFromSideAndMetadata(1, i));
			tessellator.draw();
			tessellator.startDrawingQuads();
			tessellator.setNormal(0.0F, 0.0F, -1F);
			renderblocks.renderEastFace(block, 0.0D, 0.0D, 0.0D,
					block.getBlockTextureFromSideAndMetadata(2, i));
			tessellator.draw();
			tessellator.startDrawingQuads();
			tessellator.setNormal(0.0F, 0.0F, 1.0F);
			renderblocks.renderWestFace(block, 0.0D, 0.0D, 0.0D,
					block.getBlockTextureFromSideAndMetadata(3, i));
			tessellator.draw();
			tessellator.startDrawingQuads();
			tessellator.setNormal(-1F, 0.0F, 0.0F);
			renderblocks.renderNorthFace(block, 0.0D, 0.0D, 0.0D,
					block.getBlockTextureFromSideAndMetadata(4, i));
			tessellator.draw();
			tessellator.startDrawingQuads();
			tessellator.setNormal(1.0F, 0.0F, 0.0F);
			renderblocks.renderSouthFace(block, 0.0D, 0.0D, 0.0D,
					block.getBlockTextureFromSideAndMetadata(5, i));
			tessellator.draw();
			GL11.glTranslatef(0.5F, 0.5F, 0.5F);
		} else if (block.getRenderType() == BuildCraftCore.pipeModel) {
			Tessellator tessellator = Tessellator.instance;

			block.setBlockBounds(Utils.pipeMinSize, 0.0F, Utils.pipeMinSize,
					Utils.pipeMaxSize, 1.0F, Utils.pipeMaxSize);
			block.setBlockBoundsForItemRender();
			GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
			tessellator.startDrawingQuads();
			tessellator.setNormal(0.0F, -1F, 0.0F);
			renderblocks.renderBottomFace(block, 0.0D, 0.0D, 0.0D,
					block.getBlockTextureFromSideAndMetadata(0, i));
			tessellator.draw();
			tessellator.startDrawingQuads();
			tessellator.setNormal(0.0F, 1.0F, 0.0F);
			renderblocks.renderTopFace(block, 0.0D, 0.0D, 0.0D,
					block.getBlockTextureFromSideAndMetadata(1, i));
			tessellator.draw();
			tessellator.startDrawingQuads();
			tessellator.setNormal(0.0F, 0.0F, -1F);
			renderblocks.renderEastFace(block, 0.0D, 0.0D, 0.0D,
					block.getBlockTextureFromSideAndMetadata(2, i));
			tessellator.draw();
			tessellator.startDrawingQuads();
			tessellator.setNormal(0.0F, 0.0F, 1.0F);
			renderblocks.renderWestFace(block, 0.0D, 0.0D, 0.0D,
					block.getBlockTextureFromSideAndMetadata(3, i));
			tessellator.draw();
			tessellator.startDrawingQuads();
			tessellator.setNormal(-1F, 0.0F, 0.0F);
			renderblocks.renderNorthFace(block, 0.0D, 0.0D, 0.0D,
					block.getBlockTextureFromSideAndMetadata(4, i));
			tessellator.draw();
			tessellator.startDrawingQuads();
			tessellator.setNormal(1.0F, 0.0F, 0.0F);
			renderblocks.renderSouthFace(block, 0.0D, 0.0D, 0.0D,
					block.getBlockTextureFromSideAndMetadata(5, i));
			tessellator.draw();
			GL11.glTranslatef(0.5F, 0.5F, 0.5F);
			block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
		}

		if (block instanceof ICustomTextureBlock) {
			GL11.glBindTexture(3553 /* GL_TEXTURE_2D */, ModLoader
					.getMinecraftInstance().renderEngine
					.getTexture("/terrain.png"));
		}
	}

	public void renderMarkerWithMeta(Block block, double x, double y, double z,
			int meta) {
		Tessellator tessellator = Tessellator.instance;
		int i = block.getBlockTextureFromSide(0);

		int j = (i & 0xf) << 4;
		int k = i & 0xf0;
		float f = (float) j / 256F;
		float f1 = ((float) j + 15.99F) / 256F;
		float f2 = (float) k / 256F;
		float f3 = ((float) k + 15.99F) / 256F;
		double d5 = (double) f + 0.02734375D;
		double d6 = (double) f2 + 0.0234375D;
		double d7 = (double) f + 0.03515625D;
		double d8 = (double) f2 + 0.03125D;
		x += 0.5D;
		z += 0.5D;
		double d9 = x - 0.5D;
		double d10 = x + 0.5D;
		double d11 = z - 0.5D;
		double d12 = z + 0.5D;
		double d13 = 0.0625D;
		double d14 = 0.625D;
		tessellator.addVertexWithUV(x - d13, y + d14, z - d13, d5, d6);
		tessellator.addVertexWithUV(x - d13, y + d14, z + d13, d5, d8);
		tessellator.addVertexWithUV(x + d13, y + d14, z + d13, d7, d8);
		tessellator.addVertexWithUV(x + d13, y + d14, z - d13, d7, d6);
		
		double frontX [][] = {{-0.1, -0.1, -0.1, -0.1},
		            	      {1, 0, 0, 1},       
		                      {-0.5, -0.5, 0.5, 0.5}};				
		
		if (meta == 5) {
			
		} else if (meta == 3) {
			rotateFace (frontX);
			rotateFace (frontX);
			rotateFace (frontX);
		} else if (meta == 4) {
			rotateFace (frontX);
		}
		
		if (meta == 5 || meta == 4 || meta == 3) {			
			tessellator.addVertexWithUV(x + frontX [0][0], y + frontX [1][0], z + frontX [2][0], f, f2);
			tessellator.addVertexWithUV(x + frontX [0][0], y + frontX [1][1], z + frontX [2][1], f, f3);
			tessellator.addVertexWithUV(x + frontX [0][0], y + frontX [1][2], z + frontX [2][2], f1, f3);
			tessellator.addVertexWithUV(x + frontX [0][0], y + frontX [1][3], z + frontX [2][3], f1, f2);
		}
	}
	
	private void rotateFace (double [][] face) {		
		for (int j = 0; j < 3; ++j) {
			double tmp = face [j][0];
			face [j][0] = face [j][1];
			face [j][1] = face [j][2];
			face [j][2] = face [j][3];
			face [j][3] = tmp;
		}
	}

}
