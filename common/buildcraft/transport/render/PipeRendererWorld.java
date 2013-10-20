package buildcraft.transport.render;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.core.CoreConstants;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.IPipeRenderState;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeRenderState;
import buildcraft.transport.TransportConstants;
import buildcraft.transport.TransportProxy;
import buildcraft.core.utils.MatrixTranformations;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.ForgeDirection;

public class PipeRendererWorld implements ISimpleBlockRenderingHandler {

	private void renderAllFaceExeptAxe(RenderBlocks renderblocks, BlockGenericPipe block, Icon icon, int x, int y, int z, char axe) {
		float minX = (float) renderblocks.renderMinX;
		float minY = (float) renderblocks.renderMinY;
		float minZ = (float) renderblocks.renderMinZ;
		float maxX = (float) renderblocks.renderMaxX;
		float maxY = (float) renderblocks.renderMaxY;
		float maxZ = (float) renderblocks.renderMaxZ;
		if (axe != 'x') {
			renderTwoWayXFace(renderblocks, block, icon, x, y, z, minY, minZ, maxY, maxZ, minX);
			renderTwoWayXFace(renderblocks, block, icon, x, y, z, minY, minZ, maxY, maxZ, maxX);
		}
		if (axe != 'y') {
			renderTwoWayYFace(renderblocks, block, icon, x, y, z, minX, minZ, maxX, maxZ, minY);
			renderTwoWayYFace(renderblocks, block, icon, x, y, z, minX, minZ, maxX, maxZ, maxY);
		}
		if (axe != 'z') {
			renderTwoWayZFace(renderblocks, block, icon, x, y, z, minX, minY, maxX, maxY, minZ);
			renderTwoWayZFace(renderblocks, block, icon, x, y, z, minX, minY, maxX, maxY, maxZ);
		}
	}

	private void renderTwoWayXFace(RenderBlocks renderblocks, BlockGenericPipe block, Icon icon, int xCoord, int yCoord, int zCoord, float minY, float minZ, float maxY, float maxZ, float x) {
		renderblocks.setRenderBounds(x, minY, minZ, x, maxY, maxZ);
		block.setRenderAxis('x');
		renderblocks.renderStandardBlock(block, xCoord, yCoord, zCoord);
		block.setRenderAxis('a');
	}

	private void renderTwoWayYFace(RenderBlocks renderblocks, BlockGenericPipe block, Icon icon, int xCoord, int yCoord, int zCoord, float minX, float minZ, float maxX, float maxZ, float y) {
		renderblocks.setRenderBounds(minX, y, minZ, maxX, y, maxZ);
		block.setRenderAxis('y');
		renderblocks.renderStandardBlock(block, xCoord, yCoord, zCoord);
		block.setRenderAxis('a');
	}

	private void renderTwoWayZFace(RenderBlocks renderblocks, BlockGenericPipe block, Icon icon, int xCoord, int yCoord, int zCoord, float minX, float minY, float maxX, float maxY, float z) {
		renderblocks.setRenderBounds(minX, minY, z, maxX, maxY, z);
		block.setRenderAxis('z');
		renderblocks.renderStandardBlock(block, xCoord, yCoord, zCoord);
		block.setRenderAxis('a');
	}

	public void renderPipe(RenderBlocks renderblocks, IBlockAccess iblockaccess, BlockGenericPipe block, IPipeRenderState renderState, int x, int y, int z) {

		float minSize = CoreConstants.PIPE_MIN_POS;
		float maxSize = CoreConstants.PIPE_MAX_POS;

		PipeRenderState state = renderState.getRenderState();
		IIconProvider icons = renderState.getPipeIcons();
		if (icons == null)
			return;

		boolean west = false;
		boolean east = false;
		boolean down = false;
		boolean up = false;
		boolean north = false;
		boolean south = false;

		if (state.pipeConnectionMatrix.isConnected(ForgeDirection.WEST)) {
			state.currentTexture = icons.getIcon(state.textureMatrix.getTextureIndex(ForgeDirection.WEST));
			renderblocks.setRenderBounds(0.0F, minSize, minSize, minSize, maxSize, maxSize);
			renderAllFaceExeptAxe(renderblocks, block, state.currentTexture, x, y, z, 'x');
			west = true;
		}

		if (state.pipeConnectionMatrix.isConnected(ForgeDirection.EAST)) {
			state.currentTexture = icons.getIcon(state.textureMatrix.getTextureIndex(ForgeDirection.EAST));
			renderblocks.setRenderBounds(maxSize, minSize, minSize, 1.0F, maxSize, maxSize);
			renderAllFaceExeptAxe(renderblocks, block, state.currentTexture, x, y, z, 'x');
			east = true;
		}

		if (state.pipeConnectionMatrix.isConnected(ForgeDirection.DOWN)) {
			state.currentTexture = icons.getIcon(state.textureMatrix.getTextureIndex(ForgeDirection.DOWN));
			renderblocks.setRenderBounds(minSize, 0.0F, minSize, maxSize, minSize, maxSize);
			renderAllFaceExeptAxe(renderblocks, block, state.currentTexture, x, y, z, 'y');
			down = true;
		}

		if (state.pipeConnectionMatrix.isConnected(ForgeDirection.UP)) {
			state.currentTexture = icons.getIcon(state.textureMatrix.getTextureIndex(ForgeDirection.UP));
			renderblocks.setRenderBounds(minSize, maxSize, minSize, maxSize, 1.0F, maxSize);
			renderAllFaceExeptAxe(renderblocks, block, state.currentTexture, x, y, z, 'y');
			up = true;
		}

		if (state.pipeConnectionMatrix.isConnected(ForgeDirection.NORTH)) {
			state.currentTexture = icons.getIcon(state.textureMatrix.getTextureIndex(ForgeDirection.NORTH));
			renderblocks.setRenderBounds(minSize, minSize, 0.0F, maxSize, maxSize, minSize);
			renderAllFaceExeptAxe(renderblocks, block, state.currentTexture, x, y, z, 'z');
			north = true;
		}

		if (state.pipeConnectionMatrix.isConnected(ForgeDirection.SOUTH)) {
			state.currentTexture = icons.getIcon(state.textureMatrix.getTextureIndex(ForgeDirection.SOUTH));
			renderblocks.setRenderBounds(minSize, minSize, maxSize, maxSize, maxSize, 1.0F);
			renderAllFaceExeptAxe(renderblocks, block, state.currentTexture, x, y, z, 'z');
			south = true;
		}

		state.currentTexture = icons.getIcon(state.textureMatrix.getTextureIndex(ForgeDirection.UNKNOWN));
		renderblocks.setRenderBounds(minSize, minSize, minSize, maxSize, maxSize, maxSize);
		if (!west)
			renderTwoWayXFace(renderblocks, block, state.currentTexture, x, y, z, minSize, minSize, maxSize, maxSize, minSize);
		if (!east)
			renderTwoWayXFace(renderblocks, block, state.currentTexture, x, y, z, minSize, minSize, maxSize, maxSize, maxSize);
		if (!down)
			renderTwoWayYFace(renderblocks, block, state.currentTexture, x, y, z, minSize, minSize, maxSize, maxSize, minSize);
		if (!up)
			renderTwoWayYFace(renderblocks, block, state.currentTexture, x, y, z, minSize, minSize, maxSize, maxSize, maxSize);
		if (!north)
			renderTwoWayZFace(renderblocks, block, state.currentTexture, x, y, z, minSize, minSize, maxSize, maxSize, minSize);
		if (!south)
			renderTwoWayZFace(renderblocks, block, state.currentTexture, x, y, z, minSize, minSize, maxSize, maxSize, maxSize);

		renderblocks.setRenderBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);

		pipeFacadeRenderer(renderblocks, block, state, x, y, z);
		pipePlugRenderer(renderblocks, block, state, x, y, z);

	}

	private void pipeFacadeRenderer(RenderBlocks renderblocks, BlockGenericPipe block, PipeRenderState state, int x, int y, int z) {

		float zFightOffset = 1F / 4096F;

		float[][] zeroState = new float[3][2];
		// X START - END
		zeroState[0][0] = 0.0F;
		zeroState[0][1] = 1.0F;
		// Y START - END
		zeroState[1][0] = 0.0F;
		zeroState[1][1] = TransportConstants.FACADE_THICKNESS;
		// Z START - END
		zeroState[2][0] = 0.0F;
		zeroState[2][1] = 1.0F;

		float[] xOffsets = new float[6];
		xOffsets[0] = zFightOffset;
		xOffsets[1] = zFightOffset;
		xOffsets[2] = 0;
		xOffsets[3] = 0;
		xOffsets[4] = 0;
		xOffsets[5] = 0;

		float[] yOffsets = new float[6];
		yOffsets[0] = 0;
		yOffsets[1] = 0;
		yOffsets[2] = zFightOffset;
		yOffsets[3] = zFightOffset;
		yOffsets[4] = 0;
		yOffsets[5] = 0;

		float[] zOffsets = new float[6];
		zOffsets[0] = zFightOffset;
		zOffsets[1] = zFightOffset;
		zOffsets[2] = 0;
		zOffsets[3] = 0;
		zOffsets[4] = 0;
		zOffsets[5] = 0;

		state.textureArray = new Icon[6];

		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			int facadeId = state.facadeMatrix.getFacadeBlockId(direction);
			if (facadeId != 0) {
				Block renderBlock = Block.blocksList[facadeId];
				int renderMeta = state.facadeMatrix.getFacadeMetaId(direction);

				for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
					state.textureArray[side.ordinal()] = renderBlock.getIcon(side.ordinal(), renderMeta);
					if (side == direction || side == direction.getOpposite())
						block.setRenderSide(side, true);
					else
						block.setRenderSide(side, state.facadeMatrix.getFacadeBlockId(side) == 0);
				}

				try {
					BlockGenericPipe.facadeRenderColor = Item.itemsList[state.facadeMatrix.getFacadeBlockId(direction)].getColorFromItemStack(new ItemStack(facadeId, 1, renderMeta), 0);
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
					float[][] rotated = MatrixTranformations.deepClone(zeroState);
					rotated[0][0] = CoreConstants.PIPE_MIN_POS - zFightOffset;
					rotated[0][1] = CoreConstants.PIPE_MAX_POS + zFightOffset;
					rotated[2][0] = 0.0F;
					rotated[2][1] = CoreConstants.PIPE_MIN_POS - zFightOffset;
					MatrixTranformations.transform(rotated, direction);
					renderblocks.setRenderBounds(rotated[0][0], rotated[1][0], rotated[2][0], rotated[0][1], rotated[1][1], rotated[2][1]);
					renderblocks.renderStandardBlock(block, x, y, z);

					rotated = MatrixTranformations.deepClone(zeroState);
					rotated[0][0] = CoreConstants.PIPE_MIN_POS - zFightOffset;
					rotated[0][1] = CoreConstants.PIPE_MAX_POS + zFightOffset;
					rotated[2][0] = CoreConstants.PIPE_MAX_POS + zFightOffset;
					MatrixTranformations.transform(rotated, direction);
					renderblocks.setRenderBounds(rotated[0][0], rotated[1][0], rotated[2][0], rotated[0][1], rotated[1][1], rotated[2][1]);
					renderblocks.renderStandardBlock(block, x, y, z);

					rotated = MatrixTranformations.deepClone(zeroState);
					rotated[0][0] = 0.0F;
					rotated[0][1] = CoreConstants.PIPE_MIN_POS - zFightOffset;
					MatrixTranformations.transform(rotated, direction);
					renderblocks.setRenderBounds(rotated[0][0], rotated[1][0], rotated[2][0], rotated[0][1], rotated[1][1], rotated[2][1]);
					renderblocks.renderStandardBlock(block, x, y, z);

					rotated = MatrixTranformations.deepClone(zeroState);
					rotated[0][0] = CoreConstants.PIPE_MAX_POS + zFightOffset;
					rotated[0][1] = 1F;
					MatrixTranformations.transform(rotated, direction);
					renderblocks.setRenderBounds(rotated[0][0], rotated[1][0], rotated[2][0], rotated[0][1], rotated[1][1], rotated[2][1]);
					renderblocks.renderStandardBlock(block, x, y, z);
				} else { // Solid facade
					float[][] rotated = MatrixTranformations.deepClone(zeroState);
					MatrixTranformations.transform(rotated, direction);

					renderblocks.setRenderBounds(
							rotated[0][0] + xOffsets[direction.ordinal()],
							rotated[1][0] + yOffsets[direction.ordinal()],
							rotated[2][0] + zOffsets[direction.ordinal()],
							rotated[0][1] + xOffsets[direction.ordinal()],
							rotated[1][1] + yOffsets[direction.ordinal()],
							rotated[2][1] + zOffsets[direction.ordinal()]);
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

			BlockGenericPipe.facadeRenderColor = -1;
		}

		state.textureArray = null;
		block.setRenderAllSides();

		// X START - END
		zeroState[0][0] = CoreConstants.PIPE_MIN_POS;
		zeroState[0][1] = CoreConstants.PIPE_MAX_POS;
		// Y START - END
		zeroState[1][0] = TransportConstants.FACADE_THICKNESS;
		zeroState[1][1] = CoreConstants.PIPE_MIN_POS;
		// Z START - END
		zeroState[2][0] = CoreConstants.PIPE_MIN_POS;
		zeroState[2][1] = CoreConstants.PIPE_MAX_POS;

		state.currentTexture = BuildCraftTransport.instance.pipeIconProvider.getIcon(PipeIconProvider.TYPE.PipeStructureCobblestone.ordinal()); // Structure Pipe

		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			if (state.facadeMatrix.getFacadeBlockId(direction) != 0 && !state.pipeConnectionMatrix.isConnected(direction)) {
				float[][] rotated = MatrixTranformations.deepClone(zeroState);
				MatrixTranformations.transform(rotated, direction);

				renderblocks.setRenderBounds(rotated[0][0], rotated[1][0], rotated[2][0], rotated[0][1], rotated[1][1], rotated[2][1]);
				renderblocks.renderStandardBlock(block, x, y, z);
			}
		}
	}

	private void pipePlugRenderer(RenderBlocks renderblocks, Block block, PipeRenderState state, int x, int y, int z) {

		float zFightOffset = 1F / 4096F;

		float[][] zeroState = new float[3][2];
		// X START - END
		zeroState[0][0] = 0.25F + zFightOffset;
		zeroState[0][1] = 0.75F - zFightOffset;
		// Y START - END
		zeroState[1][0] = 0.125F;
		zeroState[1][1] = 0.251F;
		// Z START - END
		zeroState[2][0] = 0.25F + zFightOffset;
		zeroState[2][1] = 0.75F - zFightOffset;

		state.currentTexture = BuildCraftTransport.instance.pipeIconProvider.getIcon(PipeIconProvider.TYPE.PipeStructureCobblestone.ordinal()); // Structure Pipe

		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			if (state.plugMatrix.isConnected(direction)) {
				float[][] rotated = MatrixTranformations.deepClone(zeroState);
				MatrixTranformations.transform(rotated, direction);

				renderblocks.setRenderBounds(rotated[0][0], rotated[1][0], rotated[2][0], rotated[0][1], rotated[1][1], rotated[2][1]);
				renderblocks.renderStandardBlock(block, x, y, z);
			}
		}

		// X START - END
		zeroState[0][0] = 0.25F + 0.125F / 2 + zFightOffset;
		zeroState[0][1] = 0.75F - 0.125F / 2 + zFightOffset;
		// Y START - END
		zeroState[1][0] = 0.25F;
		zeroState[1][1] = 0.25F + 0.125F;
		// Z START - END
		zeroState[2][0] = 0.25F + 0.125F / 2;
		zeroState[2][1] = 0.75F - 0.125F / 2;

		state.currentTexture = BuildCraftTransport.instance.pipeIconProvider.getIcon(PipeIconProvider.TYPE.PipeStructureCobblestone.ordinal()); // Structure Pipe

		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			if (state.plugMatrix.isConnected(direction)) {
				float[][] rotated = MatrixTranformations.deepClone(zeroState);
				MatrixTranformations.transform(rotated, direction);

				renderblocks.setRenderBounds(rotated[0][0], rotated[1][0], rotated[2][0], rotated[0][1], rotated[1][1], rotated[2][1]);
				renderblocks.renderStandardBlock(block, x, y, z);
			}
		}

	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		TileEntity tile = world.getBlockTileEntity(x, y, z);

		if (tile instanceof IPipeRenderState) {
			IPipeRenderState pipeTile = (IPipeRenderState) tile;
			renderPipe(renderer, world, (BlockGenericPipe) block, pipeTile, x, y, z);
		}
		return true;
	}

	@Override
	public boolean shouldRender3DInInventory() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getRenderId() {
		return TransportProxy.pipeModel;
	}
}
