/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftTransport;
import buildcraft.core.CoreConstants;
import buildcraft.core.utils.MatrixTranformations;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeRenderState;
import buildcraft.transport.TransportConstants;

public final class FacadeRenderHelper {

	private static final float zFightOffset = 1F / 4096F;
	private static final float[][] zeroStateFacade = new float[3][2];
	private static final float[][] zeroStateSupport = new float[3][2];
	private static final float[] xOffsets = new float[6];
	private static final float[] yOffsets = new float[6];
	private static final float[] zOffsets = new float[6];

	/**
	 * Deactivate constructor
	 */
	private FacadeRenderHelper() {
	}

	static {

		// X START - END
		zeroStateFacade[0][0] = 0.0F;
		zeroStateFacade[0][1] = 1.0F;
		// Y START - END
		zeroStateFacade[1][0] = 0.0F;
		zeroStateFacade[1][1] = TransportConstants.FACADE_THICKNESS;
		// Z START - END
		zeroStateFacade[2][0] = 0.0F;
		zeroStateFacade[2][1] = 1.0F;

		// X START - END
		zeroStateSupport[0][0] = CoreConstants.PIPE_MIN_POS;
		zeroStateSupport[0][1] = CoreConstants.PIPE_MAX_POS;
		// Y START - END
		zeroStateSupport[1][0] = TransportConstants.FACADE_THICKNESS;
		zeroStateSupport[1][1] = CoreConstants.PIPE_MIN_POS;
		// Z START - END
		zeroStateSupport[2][0] = CoreConstants.PIPE_MIN_POS;
		zeroStateSupport[2][1] = CoreConstants.PIPE_MAX_POS;

		xOffsets[0] = zFightOffset;
		xOffsets[1] = zFightOffset;
		xOffsets[2] = 0;
		xOffsets[3] = 0;
		xOffsets[4] = 0;
		xOffsets[5] = 0;

		yOffsets[0] = 0;
		yOffsets[1] = 0;
		yOffsets[2] = zFightOffset;
		yOffsets[3] = zFightOffset;
		yOffsets[4] = 0;
		yOffsets[5] = 0;

		zOffsets[0] = zFightOffset;
		zOffsets[1] = zFightOffset;
		zOffsets[2] = 0;
		zOffsets[3] = 0;
		zOffsets[4] = 0;
		zOffsets[5] = 0;
	}

	private static void setRenderBounds(RenderBlocks renderblocks, float[][] rotated, ForgeDirection side) {
		renderblocks.setRenderBounds(
				rotated[0][0] + xOffsets[side.ordinal()],
				rotated[1][0] + yOffsets[side.ordinal()],
				rotated[2][0] + zOffsets[side.ordinal()],
				rotated[0][1] - xOffsets[side.ordinal()],
				rotated[1][1] - yOffsets[side.ordinal()],
				rotated[2][1] - zOffsets[side.ordinal()]);
	}

	public static void pipeFacadeRenderer(RenderBlocks renderblocks, BlockGenericPipe block, PipeRenderState state, int x, int y, int z) {
		state.textureArray = new IIcon[6];

		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			Block renderBlock = state.facadeMatrix.getFacadeBlock(direction);

			if (renderBlock != null) {
				// If the facade is meant to render in the current pass
				if (renderBlock.canRenderInPass(PipeRendererWorld.renderPass)) {
					int renderMeta = state.facadeMatrix.getFacadeMetaId(direction);

					for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
						state.textureArray[side.ordinal()] = renderBlock.getIcon(side.ordinal(), renderMeta);
						if (side == direction || side == direction.getOpposite()) {
							block.setRenderSide(side, true);
						} else {
							block.setRenderSide(side, state.facadeMatrix.getFacadeBlock(side) == null);
						}
					}

					try {
						BlockGenericPipe.facadeRenderColor = Item.getItemFromBlock(state.facadeMatrix.getFacadeBlock(direction)).getColorFromItemStack(new ItemStack(renderBlock, 1, renderMeta), 0);
					} catch (Throwable error) {
					}

					if (renderBlock.getRenderType() == 31) {
						if ((renderMeta & 12) == 4) {
							renderblocks.uvRotateEast = 1;
							renderblocks.uvRotateWest = 1;
							renderblocks.uvRotateTop = 1;
							renderblocks.uvRotateBottom = 1;
						} else if ((renderMeta & 12) == 8) {
							renderblocks.uvRotateSouth = 1;
							renderblocks.uvRotateNorth = 1;
						}
					}

					// Hollow facade
					if (state.pipeConnectionMatrix.isConnected(direction)) {
						float[][] rotated = MatrixTranformations.deepClone(zeroStateFacade);
						rotated[0][0] = CoreConstants.PIPE_MIN_POS - zFightOffset * 4;
						rotated[0][1] = CoreConstants.PIPE_MAX_POS + zFightOffset * 4;
						rotated[2][0] = 0.0F;
						rotated[2][1] = CoreConstants.PIPE_MIN_POS - zFightOffset * 2;
						MatrixTranformations.transform(rotated, direction);
						setRenderBounds(renderblocks, rotated, direction);
						renderblocks.renderStandardBlock(block, x, y, z);

						rotated = MatrixTranformations.deepClone(zeroStateFacade);
						rotated[0][0] = CoreConstants.PIPE_MIN_POS - zFightOffset * 4;
						rotated[0][1] = CoreConstants.PIPE_MAX_POS + zFightOffset * 4;
						rotated[2][0] = CoreConstants.PIPE_MAX_POS + zFightOffset * 2;
						MatrixTranformations.transform(rotated, direction);
						setRenderBounds(renderblocks, rotated, direction);
						renderblocks.renderStandardBlock(block, x, y, z);

						rotated = MatrixTranformations.deepClone(zeroStateFacade);
						rotated[0][0] = 0.0F;
						rotated[0][1] = CoreConstants.PIPE_MIN_POS - zFightOffset * 2;
						MatrixTranformations.transform(rotated, direction);
						setRenderBounds(renderblocks, rotated, direction);
						renderblocks.renderStandardBlock(block, x, y, z);

						rotated = MatrixTranformations.deepClone(zeroStateFacade);
						rotated[0][0] = CoreConstants.PIPE_MAX_POS + zFightOffset * 2;
						rotated[0][1] = 1F;
						MatrixTranformations.transform(rotated, direction);
						setRenderBounds(renderblocks, rotated, direction);
						renderblocks.renderStandardBlock(block, x, y, z);
					} else { // Solid facade
						float[][] rotated = MatrixTranformations.deepClone(zeroStateFacade);
						MatrixTranformations.transform(rotated, direction);
						setRenderBounds(renderblocks, rotated, direction);
						renderblocks.renderStandardBlock(block, x, y, z);
					}

					if (renderBlock.getRenderType() == 31) {
						renderblocks.uvRotateSouth = 0;
						renderblocks.uvRotateEast = 0;
						renderblocks.uvRotateWest = 0;
						renderblocks.uvRotateNorth = 0;
						renderblocks.uvRotateTop = 0;
						renderblocks.uvRotateBottom = 0;
					}
				}
			}

			BlockGenericPipe.facadeRenderColor = -1;
		}

		state.textureArray = null;
		block.setRenderAllSides();

		state.currentTexture = BuildCraftTransport.instance.pipeIconProvider.getIcon(PipeIconProvider.TYPE.PipeStructureCobblestone.ordinal()); // Structure Pipe

		// Always render connectors in pass 0
		if (PipeRendererWorld.renderPass == 0) {
			for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
				if (state.facadeMatrix.getFacadeBlock(direction) != null && !state.pipeConnectionMatrix.isConnected(direction)) {
					float[][] rotated = MatrixTranformations.deepClone(zeroStateSupport);
					MatrixTranformations.transform(rotated, direction);

					renderblocks.setRenderBounds(rotated[0][0], rotated[1][0], rotated[2][0], rotated[0][1], rotated[1][1], rotated[2][1]);
					renderblocks.renderStandardBlock(block, x, y, z);
				}
			}
		}
	}
}
