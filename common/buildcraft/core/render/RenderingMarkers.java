/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

import buildcraft.BuildCraftCore;

public class RenderingMarkers extends BCSimpleBlockRenderingHandler {

	/* PATH MARKER RENDERING */
	public static final double[][][] frontX = new double[6][3][4];
	public static final double[][][] frontZ = new double[6][3][4];
	public static final double[][][] frontY = new double[6][3][4];
	public static final int[] metaToOld = new int[6];

	public RenderingMarkers() {
		initializeMarkerMatrix();
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		Tessellator tessellator = Tessellator.instance;
		float f = block.getMixedBrightnessForBlock(world, x, y, z);

		if (block.getLightValue() > 0) {
			f = 1.0F;
		}

		tessellator.setColorOpaque_F(f, f, f);
		renderMarkerWithMeta(world, block, x, y, z, world.getBlockMetadata(x, y, z));

		return true;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId) {
		return false;
	}

	@Override
	public int getRenderId() {
		return BuildCraftCore.markerModel;
	}

	public static double[][] safeClone(double[][] d) {
		double[][] ret = new double[d.length][d[0].length];

		for (int i = 0; i < d.length; ++i) {
			System.arraycopy(d[i], 0, ret[i], 0, d[0].length);
		}

		return ret;
	}

	public static void initializeMarkerMatrix() {
		metaToOld[0] = 0;
		metaToOld[1] = 5;
		metaToOld[2] = 4;
		metaToOld[3] = 3;
		metaToOld[4] = 2;
		metaToOld[5] = 1;

		double[][] frontXBase = {{-0.0625, -0.0625, -0.0625, -0.0625}, {1, 0, 0, 1}, {-0.5, -0.5, 0.5, 0.5}};

		frontX[3] = safeClone(frontXBase);
		rotateFace(frontX[3]);
		rotateFace(frontX[3]);
		rotateFace(frontX[3]);

		frontX[4] = safeClone(frontXBase);
		rotateFace(frontX[4]);

		frontX[5] = safeClone(frontXBase);

		frontX[0] = safeClone(frontXBase);
		rotateFace(frontX[0]);
		rotateFace(frontX[0]);

		double[][] frontZBase = {{-0.5, -0.5, 0.5, 0.5}, {1, 0, 0, 1}, {0.0625, 0.0625, 0.0625, 0.0625}};

		frontZ[5] = safeClone(frontZBase);

		frontZ[1] = safeClone(frontZBase);
		rotateFace(frontZ[1]);
		rotateFace(frontZ[1]);
		rotateFace(frontZ[1]);

		frontZ[2] = safeClone(frontZBase);
		rotateFace(frontZ[2]);

		frontZ[0] = safeClone(frontZBase);
		rotateFace(frontZ[0]);
		rotateFace(frontZ[0]);

		double[][] frontYBase = {{-0.5, -0.5, 0.5, 0.5}, {-0.0625, -0.0625, -0.0625, -0.0625}, {0.5, -0.5, -0.5, 0.5}};

		frontY[4] = safeClone(frontYBase);
		rotateFace(frontY[4]);
		rotateFace(frontY[4]);

		frontY[3] = safeClone(frontYBase);

		frontY[2] = safeClone(frontYBase);
		rotateFace(frontY[2]);

		frontY[1] = safeClone(frontYBase);
		rotateFace(frontY[1]);
		rotateFace(frontY[1]);
		rotateFace(frontY[1]);

	}

	public void renderMarkerWithMeta(IBlockAccess iblockaccess, Block block, double xi, double yi, double zi, int meta) {
		Tessellator tessellator = Tessellator.instance;
		int metadata = meta;

		if (metadata > 5 || metadata < 0) {
			metadata = 1;
		}

		double x = xi;
		double y = yi;
		double z = zi;

		int xCoord = (int) x;
		int yCoord = (int) y;
		int zCoord = (int) z;

		IIcon i = block.getIcon(iblockaccess, xCoord, yCoord, zCoord, 1);

		int m = metaToOld[metadata];
		x += 0.5D;
		z += 0.5D;

		double minU = i.getInterpolatedU(7);
		double minV = i.getInterpolatedV(7);
		double maxU = i.getInterpolatedU(9);
		double maxV = i.getInterpolatedV(9);

		tessellator.setBrightness(block.getMixedBrightnessForBlock(iblockaccess, xCoord, yCoord, zCoord));

		double s = 1F / 16F;

		if (m == 5) {
			tessellator.addVertexWithUV(x - s, y + 0.5 + s, z - s, minU, minV);
			tessellator.addVertexWithUV(x - s, y + 0.5 + s, z + s, minU, maxV);
			tessellator.addVertexWithUV(x + s, y + 0.5 + s, z + s, maxU, maxV);
			tessellator.addVertexWithUV(x + s, y + 0.5 + s, z - s, maxU, minV);
		} else if (m == 0) {
			tessellator.addVertexWithUV(x + s, y + 0.5 - s, z - s, maxU, minV);
			tessellator.addVertexWithUV(x + s, y + 0.5 - s, z + s, maxU, maxV);
			tessellator.addVertexWithUV(x - s, y + 0.5 - s, z + s, minU, maxV);
			tessellator.addVertexWithUV(x - s, y + 0.5 - s, z - s, minU, minV);
		} else if (m == 2) {
			tessellator.addVertexWithUV(x - s, y + 0.5 - s, z - s, minU, minV);
			tessellator.addVertexWithUV(x - s, y + 0.5 - s, z + s, minU, maxV);
			tessellator.addVertexWithUV(x - s, y + 0.5 + s, z + s, maxU, maxV);
			tessellator.addVertexWithUV(x - s, y + 0.5 + s, z - s, maxU, minV);
		} else if (m == 1) {
			tessellator.addVertexWithUV(x + s, y + 0.5 + s, z - s, maxU, minV);
			tessellator.addVertexWithUV(x + s, y + 0.5 + s, z + s, maxU, maxV);
			tessellator.addVertexWithUV(x + s, y + 0.5 - s, z + s, minU, maxV);
			tessellator.addVertexWithUV(x + s, y + 0.5 - s, z - s, minU, minV);
		} else if (m == 3) {
			tessellator.addVertexWithUV(x - s, y + 0.5 - s, z + s, minU, minV);
			tessellator.addVertexWithUV(x + s, y + 0.5 - s, z + s, minU, maxV);
			tessellator.addVertexWithUV(x + s, y + 0.5 + s, z + s, maxU, maxV);
			tessellator.addVertexWithUV(x - s, y + 0.5 + s, z + s, maxU, minV);
		} else if (m == 4) {
			tessellator.addVertexWithUV(x - s, y + 0.5 + s, z - s, maxU, minV);
			tessellator.addVertexWithUV(x + s, y + 0.5 + s, z - s, maxU, maxV);
			tessellator.addVertexWithUV(x + s, y + 0.5 - s, z - s, minU, maxV);
			tessellator.addVertexWithUV(x - s, y + 0.5 - s, z - s, minU, minV);
		}

		i = block.getIcon(iblockaccess, xCoord, yCoord, zCoord, 0);

		minU = i.getMinU();
		maxU = i.getMaxU();
		minV = i.getMinV();
		maxV = i.getMaxV();

		if (m == 5 || m == 4 || m == 3 || m == 0) {
			tessellator.addVertexWithUV(x + frontX[m][0][0], y + frontX[m][1][0], z + frontX[m][2][0], minU, minV);
			tessellator.addVertexWithUV(x + frontX[m][0][1], y + frontX[m][1][1], z + frontX[m][2][1], minU, maxV);
			tessellator.addVertexWithUV(x + frontX[m][0][2], y + frontX[m][1][2], z + frontX[m][2][2], maxU, maxV);
			tessellator.addVertexWithUV(x + frontX[m][0][3], y + frontX[m][1][3], z + frontX[m][2][3], maxU, minV);

			tessellator.addVertexWithUV(x - frontX[m][0][3], y + frontX[m][1][3], z + frontX[m][2][3], maxU, minV);
			tessellator.addVertexWithUV(x - frontX[m][0][2], y + frontX[m][1][2], z + frontX[m][2][2], maxU, maxV);
			tessellator.addVertexWithUV(x - frontX[m][0][1], y + frontX[m][1][1], z + frontX[m][2][1], minU, maxV);
			tessellator.addVertexWithUV(x - frontX[m][0][0], y + frontX[m][1][0], z + frontX[m][2][0], minU, minV);
		}

		if (m == 5 || m == 2 || m == 1 || m == 0) {
			tessellator.addVertexWithUV(x + frontZ[m][0][0], y + frontZ[m][1][0], z + frontZ[m][2][0], minU, minV);
			tessellator.addVertexWithUV(x + frontZ[m][0][1], y + frontZ[m][1][1], z + frontZ[m][2][1], minU, maxV);
			tessellator.addVertexWithUV(x + frontZ[m][0][2], y + frontZ[m][1][2], z + frontZ[m][2][2], maxU, maxV);
			tessellator.addVertexWithUV(x + frontZ[m][0][3], y + frontZ[m][1][3], z + frontZ[m][2][3], maxU, minV);

			tessellator.addVertexWithUV(x + frontZ[m][0][3], y + frontZ[m][1][3], z - frontZ[m][2][3], maxU, minV);
			tessellator.addVertexWithUV(x + frontZ[m][0][2], y + frontZ[m][1][2], z - frontZ[m][2][2], maxU, maxV);
			tessellator.addVertexWithUV(x + frontZ[m][0][1], y + frontZ[m][1][1], z - frontZ[m][2][1], minU, maxV);
			tessellator.addVertexWithUV(x + frontZ[m][0][0], y + frontZ[m][1][0], z - frontZ[m][2][0], minU, minV);
		}

		if (m == 4 || m == 3 || m == 2 || m == 1) {
			tessellator.addVertexWithUV(x + frontY[m][0][0], y + 0.5 + frontY[m][1][0], z + frontY[m][2][0], minU, minV);
			tessellator.addVertexWithUV(x + frontY[m][0][1], y + 0.5 + frontY[m][1][1], z + frontY[m][2][1], minU, maxV);
			tessellator.addVertexWithUV(x + frontY[m][0][2], y + 0.5 + frontY[m][1][2], z + frontY[m][2][2], maxU, maxV);
			tessellator.addVertexWithUV(x + frontY[m][0][3], y + 0.5 + frontY[m][1][3], z + frontY[m][2][3], maxU, minV);

			tessellator.addVertexWithUV(x + frontY[m][0][3], y + 0.5 - frontY[m][1][3], z + frontY[m][2][3], maxU, minV);
			tessellator.addVertexWithUV(x + frontY[m][0][2], y + 0.5 - frontY[m][1][2], z + frontY[m][2][2], maxU, maxV);
			tessellator.addVertexWithUV(x + frontY[m][0][1], y + 0.5 - frontY[m][1][1], z + frontY[m][2][1], minU, maxV);
			tessellator.addVertexWithUV(x + frontY[m][0][0], y + 0.5 - frontY[m][1][0], z + frontY[m][2][0], minU, minV);
		}
	}

	private static void rotateFace(double[][] face) {
		for (int j = 0; j < 3; ++j) {
			double tmp = face[j][0];
			face[j][0] = face[j][1];
			face[j][1] = face[j][2];
			face[j][2] = face[j][3];
			face[j][3] = tmp;
		}
	}
}
