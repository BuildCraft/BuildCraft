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
		FacadeRenderHelper.pipeFacadeRenderer(renderblocks, block, state, x, y, z);
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
