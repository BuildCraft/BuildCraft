/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.render.ITextureStates;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.pluggable.IFacadePluggable;
import buildcraft.core.CoreConstants;
import buildcraft.core.lib.render.FakeBlock;
import buildcraft.core.lib.render.TextureStateManager;
import buildcraft.core.lib.utils.MatrixTranformations;
import buildcraft.transport.PipeIconProvider;
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

	public static void pipeFacadeRenderer(RenderBlocks renderblocks, ITextureStates blockStateMachine, IPipeTile tile, int renderPass, int x, int y, int z, ForgeDirection direction, IFacadePluggable pluggable) {
		ITextureStates textureManager = blockStateMachine;
		IIcon[] textures = ((TextureStateManager) textureManager.getTextureState()).popArray();

		Block renderBlock = pluggable.getCurrentBlock();

		if (renderBlock != null && tile != null) {
			IBlockAccess facadeBlockAccess = new FacadeBlockAccess(tile.getWorld(), direction);

			// If the facade is meant to render in the current pass
			if (renderBlock.canRenderInPass(renderPass)) {
				int renderMeta = pluggable.getCurrentMetadata();

				for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
					textures[side.ordinal()] = renderBlock.getIcon(
							facadeBlockAccess, tile.x(), tile.y(), tile.z(), side.ordinal()
					);
					if (textures[side.ordinal()] == null) {
						textures[side.ordinal()] = renderBlock.getIcon(side.ordinal(), renderMeta);
					}
					if (side == direction || side == direction.getOpposite()) {
						blockStateMachine.setRenderSide(side, true);
					} else {
						if (!(tile.getPipePluggable(side) instanceof IFacadePluggable)) {
							blockStateMachine.setRenderSide(side, true);
						} else {
							IFacadePluggable pluggable2 = (IFacadePluggable) tile.getPipePluggable(side);
							blockStateMachine.setRenderSide(side, pluggable2.getCurrentBlock() == null);
						}
					}
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

				((FakeBlock) blockStateMachine.getBlock()).setColor(renderBlock.getRenderColor(renderMeta));
				// Hollow facade
				if (pluggable.isHollow()) {
					renderblocks.field_152631_f = true;
					float[][] rotated = MatrixTranformations.deepClone(zeroStateFacade);
					rotated[0][0] = CoreConstants.PIPE_MIN_POS - zFightOffset * 4;
					rotated[0][1] = CoreConstants.PIPE_MAX_POS + zFightOffset * 4;
					rotated[2][0] = 0.0F;
					rotated[2][1] = CoreConstants.PIPE_MIN_POS - zFightOffset * 2;
					MatrixTranformations.transform(rotated, direction);
					setRenderBounds(renderblocks, rotated, direction);
					renderblocks.renderStandardBlockWithColorMultiplier(blockStateMachine.getBlock(), x, y, z, 1.0f, 1.0f, 1.0f);

					rotated = MatrixTranformations.deepClone(zeroStateFacade);
					rotated[0][0] = CoreConstants.PIPE_MIN_POS - zFightOffset * 4;
					rotated[0][1] = CoreConstants.PIPE_MAX_POS + zFightOffset * 4;
					rotated[2][0] = CoreConstants.PIPE_MAX_POS + zFightOffset * 2;
					MatrixTranformations.transform(rotated, direction);
					setRenderBounds(renderblocks, rotated, direction);
					renderblocks.renderStandardBlockWithColorMultiplier(blockStateMachine.getBlock(), x, y, z, 1.0f, 1.0f, 1.0f);

					rotated = MatrixTranformations.deepClone(zeroStateFacade);
					rotated[0][0] = 0.0F;
					rotated[0][1] = CoreConstants.PIPE_MIN_POS - zFightOffset * 2;
					MatrixTranformations.transform(rotated, direction);
					setRenderBounds(renderblocks, rotated, direction);
					renderblocks.renderStandardBlockWithColorMultiplier(blockStateMachine.getBlock(), x, y, z, 1.0f, 1.0f, 1.0f);

					rotated = MatrixTranformations.deepClone(zeroStateFacade);
					rotated[0][0] = CoreConstants.PIPE_MAX_POS + zFightOffset * 2;
					rotated[0][1] = 1F;
					MatrixTranformations.transform(rotated, direction);
					setRenderBounds(renderblocks, rotated, direction);
					renderblocks.renderStandardBlockWithColorMultiplier(blockStateMachine.getBlock(), x, y, z, 1.0f, 1.0f, 1.0f);
					renderblocks.field_152631_f = false;
				} else { // Solid facade
					float[][] rotated = MatrixTranformations.deepClone(zeroStateFacade);
					MatrixTranformations.transform(rotated, direction);
					setRenderBounds(renderblocks, rotated, direction);
					renderblocks.renderStandardBlock(blockStateMachine.getBlock(), x, y, z);
				}
				((FakeBlock) blockStateMachine.getBlock()).setColor(0xFFFFFF);

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

		((FakeBlock) blockStateMachine.getBlock()).setColor(0xFFFFFF);

		((TextureStateManager) textureManager.getTextureState()).pushArray();
		blockStateMachine.setRenderAllSides();

		textureManager.getTextureState().set(BuildCraftTransport.instance.pipeIconProvider.getIcon(PipeIconProvider.TYPE.PipeStructureCobblestone.ordinal())); // Structure Pipe

		// Always render connectors in pass 0
		if (renderPass == 0 && !pluggable.isHollow() && renderBlock != null && renderBlock.getMaterial().isOpaque()) {
			float[][] rotated = MatrixTranformations.deepClone(zeroStateSupport);
			MatrixTranformations.transform(rotated, direction);

			renderblocks.setRenderBounds(rotated[0][0], rotated[1][0], rotated[2][0], rotated[0][1], rotated[1][1], rotated[2][1]);
			renderblocks.renderStandardBlock(blockStateMachine.getBlock(), x, y, z);
		}
	}
}
