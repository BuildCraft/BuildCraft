package buildcraft.transport.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.IPipe.WireColor;
import buildcraft.core.DefaultProps;
import buildcraft.core.utils.Utils;
import buildcraft.transport.IPipeRenderState;
import buildcraft.transport.PipeRenderState;
import buildcraft.transport.TransportProxy;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

public class PipeWorldRenderer implements ISimpleBlockRenderingHandler {

	public static final float facadeThickness = 1F / 16F;

	/**
	 * Mirrors the array on the Y axis by calculating offsets from 0.5F
	 * 
	 * @param targetArray
	 */
	private void mirrorY(float[][] targetArray) {
		float temp = targetArray[1][0];
		targetArray[1][0] = (targetArray[1][1] - 0.5F) * -1F + 0.5F; // 1 -> 0.5F -> -0.5F -> 0F
		targetArray[1][1] = (temp - 0.5F) * -1F + 0.5F; // 0 -> -0.5F -> 0.5F -> 1F
	}

	/**
	 * Shifts the coordinates around effectivly rotating something. Zero state is DOWN then -> NORTH -> WEST Note - To obtain Pos, do a mirrorY() before
	 * rotating
	 * 
	 * @param targetArray
	 *            the array that should be rotated
	 */
	private void rotate(float[][] targetArray) {
		for (int i = 0; i < 2; i++) {
			float temp = targetArray[2][i];
			targetArray[2][i] = targetArray[1][i];
			targetArray[1][i] = targetArray[0][i];
			targetArray[0][i] = temp;
		}
	}

	/**
	 * @param targetArray
	 *            the array that should be transformed
	 * @param direction
	 */
	private void transform(float[][] targetArray, ForgeDirection direction) {
		if ((direction.ordinal() & 0x1) == 1) {
			mirrorY(targetArray);
		}

		for (int i = 0; i < (direction.ordinal() >> 1); i++) {
			rotate(targetArray);
		}
	}

	/**
	 * Clones both dimensions of a float[][]
	 * 
	 * @param source
	 *            the float[][] to deepClone
	 * @return
	 */
	private float[][] deepClone(float[][] source) {
		float[][] target = source.clone();
		for (int i = 0; i < target.length; i++) {
			target[i] = source[i].clone();
		}
		return target;
	}

	public void renderPipe(RenderBlocks renderblocks, IBlockAccess iblockaccess, Block block, PipeRenderState state, int x, int y, int z) {

		float minSize = Utils.pipeMinPos;
		float maxSize = Utils.pipeMaxPos;

		ForgeHooksClient.bindTexture(state.getTextureFile(), 0);

		state.currentTextureIndex = state.textureMatrix.getTextureIndex(ForgeDirection.UNKNOWN);
		block.setBlockBounds(minSize, minSize, minSize, maxSize, maxSize, maxSize);
		renderblocks.updateCustomBlockBounds(block);
		renderblocks.renderStandardBlock(block, x, y, z);

		if (state.pipeConnectionMatrix.isConnected(ForgeDirection.WEST)) {
			state.currentTextureIndex = state.textureMatrix.getTextureIndex(ForgeDirection.WEST);
			block.setBlockBounds(0.0F, minSize, minSize, minSize, maxSize, maxSize);
			renderblocks.updateCustomBlockBounds(block);
			renderblocks.renderStandardBlock(block, x, y, z);
		}

		if (state.pipeConnectionMatrix.isConnected(ForgeDirection.EAST)) {
			state.currentTextureIndex = state.textureMatrix.getTextureIndex(ForgeDirection.EAST);
			block.setBlockBounds(maxSize, minSize, minSize, 1.0F, maxSize, maxSize);
			renderblocks.updateCustomBlockBounds(block);
			renderblocks.renderStandardBlock(block, x, y, z);
		}

		if (state.pipeConnectionMatrix.isConnected(ForgeDirection.DOWN)) {
			state.currentTextureIndex = state.textureMatrix.getTextureIndex(ForgeDirection.DOWN);
			block.setBlockBounds(minSize, 0.0F, minSize, maxSize, minSize, maxSize);
			renderblocks.updateCustomBlockBounds(block);
			renderblocks.renderStandardBlock(block, x, y, z);
		}

		if (state.pipeConnectionMatrix.isConnected(ForgeDirection.UP)) {
			state.currentTextureIndex = state.textureMatrix.getTextureIndex(ForgeDirection.UP);
			block.setBlockBounds(minSize, maxSize, minSize, maxSize, 1.0F, maxSize);
			renderblocks.updateCustomBlockBounds(block);
			renderblocks.renderStandardBlock(block, x, y, z);
		}

		if (state.pipeConnectionMatrix.isConnected(ForgeDirection.NORTH)) {
			state.currentTextureIndex = state.textureMatrix.getTextureIndex(ForgeDirection.NORTH);
			block.setBlockBounds(minSize, minSize, 0.0F, maxSize, maxSize, minSize);
			renderblocks.updateCustomBlockBounds(block);
			renderblocks.renderStandardBlock(block, x, y, z);
		}

		if (state.pipeConnectionMatrix.isConnected(ForgeDirection.SOUTH)) {
			state.currentTextureIndex = state.textureMatrix.getTextureIndex(ForgeDirection.SOUTH);
			block.setBlockBounds(minSize, minSize, maxSize, maxSize, maxSize, 1.0F);
			renderblocks.updateCustomBlockBounds(block);
			renderblocks.renderStandardBlock(block, x, y, z);
		}

		block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);

		ForgeHooksClient.bindTexture(DefaultProps.TEXTURE_BLOCKS, 0);

		if (state.wireMatrix.hasWire(WireColor.Red)) {
			state.currentTextureIndex = state.wireMatrix.getTextureIndex(WireColor.Red);
			pipeWireRender(renderblocks, block, state, Utils.pipeMinPos, Utils.pipeMaxPos, Utils.pipeMinPos, IPipe.WireColor.Red, x, y, z);
		}

		if (state.wireMatrix.hasWire(WireColor.Blue)) {
			state.currentTextureIndex = state.wireMatrix.getTextureIndex(WireColor.Blue);
			pipeWireRender(renderblocks, block, state, Utils.pipeMaxPos, Utils.pipeMaxPos, Utils.pipeMaxPos, IPipe.WireColor.Blue, x, y, z);
		}

		if (state.wireMatrix.hasWire(WireColor.Green)) {
			state.currentTextureIndex = state.wireMatrix.getTextureIndex(WireColor.Green);
			pipeWireRender(renderblocks, block, state, Utils.pipeMaxPos, Utils.pipeMinPos, Utils.pipeMinPos, IPipe.WireColor.Green, x, y, z);
		}

		if (state.wireMatrix.hasWire(WireColor.Yellow)) {
			state.currentTextureIndex = state.wireMatrix.getTextureIndex(WireColor.Yellow);
			pipeWireRender(renderblocks, block, state, Utils.pipeMinPos, Utils.pipeMinPos, Utils.pipeMaxPos, IPipe.WireColor.Yellow, x, y, z);
		}

		if (state.hasGate()) {
			pipeGateRender(renderblocks, block, state, x, y, z);
		}

		pipeFacadeRenderer(renderblocks, block, state, x, y, z);

	}

	private void pipeFacadeRenderer(RenderBlocks renderblocks, Block block, PipeRenderState state, int x, int y, int z) {

		float zFightOffset = 1F / 4096F;

		float[][] zeroState = new float[3][2];
		// X START - END
		zeroState[0][0] = 0.0F - zFightOffset / 2;
		zeroState[0][1] = 1.0F + zFightOffset / 2;
		// Y START - END
		zeroState[1][0] = 0.0F - zFightOffset;
		zeroState[1][1] = facadeThickness;
		// Z START - END
		zeroState[2][0] = 0.0F;
		zeroState[2][1] = 1.0F;

		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			if (state.facadeMatrix.isConnected(direction)) {
				ForgeHooksClient.bindTexture(state.facadeMatrix.getTextureFile(direction), 0);
				state.currentTextureIndex = state.facadeMatrix.getTextureIndex(direction);

				// Hollow facade
				if (state.pipeConnectionMatrix.isConnected(direction)) {
					float[][] rotated = deepClone(zeroState);
					rotated[2][0] = 0.0F;
					rotated[2][1] = Utils.pipeMinPos;
					rotated[1][0] -= zFightOffset / 2;
					transform(rotated, direction);
					block.setBlockBounds(rotated[0][0], rotated[1][0], rotated[2][0], rotated[0][1], rotated[1][1], rotated[2][1]);
					renderblocks.updateCustomBlockBounds(block);
					renderblocks.renderStandardBlock(block, x, y, z);

					rotated = deepClone(zeroState);
					rotated[2][0] = Utils.pipeMaxPos;
					rotated[1][0] -= zFightOffset / 2;
					transform(rotated, direction);
					block.setBlockBounds(rotated[0][0], rotated[1][0], rotated[2][0], rotated[0][1], rotated[1][1], rotated[2][1]);
					renderblocks.updateCustomBlockBounds(block);
					renderblocks.renderStandardBlock(block, x, y, z);

					rotated = deepClone(zeroState);
					rotated[0][0] = 0.0F;
					rotated[0][1] = Utils.pipeMinPos;
					rotated[1][1] -= zFightOffset;
					transform(rotated, direction);
					block.setBlockBounds(rotated[0][0], rotated[1][0], rotated[2][0], rotated[0][1], rotated[1][1], rotated[2][1]);
					renderblocks.updateCustomBlockBounds(block);
					renderblocks.renderStandardBlock(block, x, y, z);

					rotated = deepClone(zeroState);
					rotated[0][0] = Utils.pipeMaxPos;
					rotated[0][1] = 1F;
					rotated[1][1] -= zFightOffset;
					transform(rotated, direction);
					block.setBlockBounds(rotated[0][0], rotated[1][0], rotated[2][0], rotated[0][1], rotated[1][1], rotated[2][1]);
					renderblocks.updateCustomBlockBounds(block);
					renderblocks.renderStandardBlock(block, x, y, z);
				} else { // Solid facade
					float[][] rotated = deepClone(zeroState);
					transform(rotated, direction);
					block.setBlockBounds(rotated[0][0], rotated[1][0], rotated[2][0], rotated[0][1], rotated[1][1], rotated[2][1]);
					renderblocks.updateCustomBlockBounds(block);
					renderblocks.renderStandardBlock(block, x, y, z);
				}
			}
		}

		// X START - END
		zeroState[0][0] = Utils.pipeMinPos;
		zeroState[0][1] = Utils.pipeMaxPos;
		// Y START - END
		zeroState[1][0] = facadeThickness;
		zeroState[1][1] = Utils.pipeMinPos;
		// Z START - END
		zeroState[2][0] = Utils.pipeMinPos;
		zeroState[2][1] = Utils.pipeMaxPos;

		ForgeHooksClient.bindTexture(DefaultProps.TEXTURE_BLOCKS, 0);
		state.currentTextureIndex = 7 * 16 + 13; // Structure Pipe

		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			if (state.facadeMatrix.isConnected(direction) && !state.pipeConnectionMatrix.isConnected(direction)) {
				float[][] rotated = deepClone(zeroState);
				transform(rotated, direction);

				block.setBlockBounds(rotated[0][0], rotated[1][0], rotated[2][0], rotated[0][1], rotated[1][1], rotated[2][1]);
				renderblocks.updateCustomBlockBounds(block);
				renderblocks.renderStandardBlock(block, x, y, z);
			}
		}

		/** WHOLE BUNCH OF OLD (WORKING) RENDER CODE, WILL CLEAN UP LATER **/

		// if (state.facadeMatrix.isConnected(ForgeDirection.WEST)){
		// MinecraftForgeClient.bindTexture(state.facadeMatrix.getTextureFile(ForgeDirection.WEST));
		// state.currentTextureIndex = state.facadeMatrix.getTextureIndex(ForgeDirection.WEST);
		// block.setBlockBounds(0.0F - zFightOffset, 0.0F, 0.0F, facadeThickness, 1.0F, 1F);
		// renderblocks.renderStandardBlock(block, x, y, z);
		// }
		//
		// if (state.facadeMatrix.isConnected(ForgeDirection.EAST)){
		// MinecraftForgeClient.bindTexture(state.facadeMatrix.getTextureFile(ForgeDirection.EAST));
		// state.currentTextureIndex = state.facadeMatrix.getTextureIndex(ForgeDirection.EAST);
		// block.setBlockBounds(1F-facadeThickness, 0.0F, 0.0F, 1.0F + zFightOffset, 1.0F, 1F);
		// renderblocks.renderStandardBlock(block, x, y, z);
		// }
		//
		//
		// if (state.facadeMatrix.isConnected(ForgeDirection.DOWN)){
		// MinecraftForgeClient.bindTexture(state.facadeMatrix.getTextureFile(ForgeDirection.DOWN));
		// state.currentTextureIndex = state.facadeMatrix.getTextureIndex(ForgeDirection.DOWN);
		// block.setBlockBounds(0.0F, 0.0F - zFightOffset, 0.0F, 1.0F, facadeThickness, 1F);
		// renderblocks.renderStandardBlock(block, x, y, z);
		// }
		//
		// if (state.facadeMatrix.isConnected(ForgeDirection.UP)){
		// MinecraftForgeClient.bindTexture(state.facadeMatrix.getTextureFile(ForgeDirection.UP));
		// state.currentTextureIndex = state.facadeMatrix.getTextureIndex(ForgeDirection.UP);
		//
		// if (state.pipeConnectionMatrix.isConnected(ForgeDirection.UP)){
		// block.setBlockBounds(0.0F, 1F-facadeThickness, 0.0F, 1.0F - Utils.pipeMaxPos, 1.0F + zFightOffset / 2 , 1F);
		// renderblocks.renderStandardBlock(block, x, y, z);
		//
		// block.setBlockBounds(0.0F, 1F-facadeThickness, 0.0F, 1.0F, 1.0F + zFightOffset , 1F - Utils.pipeMaxPos);
		// renderblocks.renderStandardBlock(block, x, y, z);
		//
		// block.setBlockBounds(0.0F + Utils.pipeMaxPos, 1F-facadeThickness, 0.0F, 1.0F, 1.0F + zFightOffset / 2 , 1F);
		// renderblocks.renderStandardBlock(block, x, y, z);
		//
		// block.setBlockBounds(0.0F, 1F-facadeThickness, 0.0F + Utils.pipeMaxPos, 1.0F, 1.0F + zFightOffset , 1F);
		// renderblocks.renderStandardBlock(block, x, y, z);
		// } else {
		// block.setBlockBounds(0.0F, 1F-facadeThickness, 0.0F, 1.0F, 1.0F + zFightOffset , 1F);
		// renderblocks.renderStandardBlock(block, x, y, z);
		// }
		// }
		//
		// if (state.facadeMatrix.isConnected(ForgeDirection.NORTH)){
		// MinecraftForgeClient.bindTexture(state.facadeMatrix.getTextureFile(ForgeDirection.NORTH));
		// state.currentTextureIndex = state.facadeMatrix.getTextureIndex(ForgeDirection.NORTH);
		// block.setBlockBounds(0.0F, 0.0F, 0.0F - zFightOffset, 1.0F, 1F, facadeThickness);
		// renderblocks.renderStandardBlock(block, x, y, z);
		// }
		//
		//
		// if (state.facadeMatrix.isConnected(ForgeDirection.SOUTH)){
		// MinecraftForgeClient.bindTexture(state.facadeMatrix.getTextureFile(ForgeDirection.SOUTH));
		// state.currentTextureIndex = state.facadeMatrix.getTextureIndex(ForgeDirection.SOUTH);
		// block.setBlockBounds(0.0F, 0.0F, 1F-facadeThickness, 1.0F, 1F, 1.0F + zFightOffset);
		// renderblocks.renderStandardBlock(block, x, y, z);
		// }
		//
		// MinecraftForgeClient.bindTexture(DefaultProps.TEXTURE_BLOCKS);
		// state.currentTextureIndex = 7 * 16 + 13; // Structure Pipe
		//
		// if (state.facadeMatrix.isConnected(ForgeDirection.WEST) && !state.pipeConnectionMatrix.isConnected(ForgeDirection.WEST)){
		// block.setBlockBounds(0 + facadeThickness, Utils.pipeMinPos, Utils.pipeMinPos, Utils.pipeMinPos, Utils.pipeMaxPos, Utils.pipeMaxPos);
		// renderblocks.renderStandardBlock(block, x, y, z);
		// }
		//
		// if (state.facadeMatrix.isConnected(ForgeDirection.EAST) && !state.pipeConnectionMatrix.isConnected(ForgeDirection.EAST)){
		// block.setBlockBounds(Utils.pipeMaxPos, Utils.pipeMinPos, Utils.pipeMinPos, 1F - facadeThickness, Utils.pipeMaxPos, Utils.pipeMaxPos);
		// renderblocks.renderStandardBlock(block, x, y, z);
		// }
		//
		// if (state.facadeMatrix.isConnected(ForgeDirection.DOWN) && !state.pipeConnectionMatrix.isConnected(ForgeDirection.DOWN)){
		// block.setBlockBounds(Utils.pipeMinPos, 0 + facadeThickness, Utils.pipeMinPos, Utils.pipeMaxPos, Utils.pipeMinPos, Utils.pipeMaxPos);
		// renderblocks.renderStandardBlock(block, x, y, z);
		// }
		//
		// if (state.facadeMatrix.isConnected(ForgeDirection.UP) && !state.pipeConnectionMatrix.isConnected(ForgeDirection.UP)){
		// block.setBlockBounds(Utils.pipeMinPos, Utils.pipeMaxPos, Utils.pipeMinPos, Utils.pipeMaxPos, 1F - facadeThickness, Utils.pipeMaxPos);
		// renderblocks.renderStandardBlock(block, x, y, z);
		// }
		//
		// if (state.facadeMatrix.isConnected(ForgeDirection.NORTH) && !state.pipeConnectionMatrix.isConnected(ForgeDirection.NORTH)){
		// block.setBlockBounds(Utils.pipeMinPos, Utils.pipeMinPos, 0 + facadeThickness, Utils.pipeMaxPos, Utils.pipeMaxPos, Utils.pipeMinPos);
		// renderblocks.renderStandardBlock(block, x, y, z);
		// }
		//
		// if (state.facadeMatrix.isConnected(ForgeDirection.SOUTH) && !state.pipeConnectionMatrix.isConnected(ForgeDirection.SOUTH)){
		// block.setBlockBounds(Utils.pipeMinPos, Utils.pipeMinPos, Utils.pipeMaxPos, Utils.pipeMaxPos, Utils.pipeMaxPos, 1F - facadeThickness);
		// renderblocks.renderStandardBlock(block, x, y, z);
		// }
	}

	private void pipeWireRender(RenderBlocks renderblocks, Block block, PipeRenderState state, float cx, float cy, float cz, IPipe.WireColor color, int x,
			int y, int z) {

		float minX = Utils.pipeMinPos;
		float minY = Utils.pipeMinPos;
		float minZ = Utils.pipeMinPos;

		float maxX = Utils.pipeMaxPos;
		float maxY = Utils.pipeMaxPos;
		float maxZ = Utils.pipeMaxPos;

		boolean foundX = false, foundY = false, foundZ = false;

		if (state.wireMatrix.isWireConnected(color, ForgeDirection.WEST)) {
			minX = 0;
			foundX = true;
		}

		if (state.wireMatrix.isWireConnected(color, ForgeDirection.EAST)) {
			maxX = 1;
			foundX = true;
		}

		if (state.wireMatrix.isWireConnected(color, ForgeDirection.DOWN)) {
			minY = 0;
			foundY = true;
		}

		if (state.wireMatrix.isWireConnected(color, ForgeDirection.UP)) {
			maxY = 1;
			foundY = true;
		}

		if (state.wireMatrix.isWireConnected(color, ForgeDirection.NORTH)) {
			minZ = 0;
			foundZ = true;
		}

		if (state.wireMatrix.isWireConnected(color, ForgeDirection.SOUTH)) {
			maxZ = 1;
			foundZ = true;
		}

		boolean center = false;

		if (minX == 0 && maxX != 1 && (foundY || foundZ))
			if (cx == Utils.pipeMinPos) {
				maxX = Utils.pipeMinPos;
			} else {
				center = true;
			}

		if (minX != 0 && maxX == 1 && (foundY || foundZ))
			if (cx == Utils.pipeMaxPos) {
				minX = Utils.pipeMaxPos;
			} else {
				center = true;
			}

		if (minY == 0 && maxY != 1 && (foundX || foundZ))
			if (cy == Utils.pipeMinPos) {
				maxY = Utils.pipeMinPos;
			} else {
				center = true;
			}

		if (minY != 0 && maxY == 1 && (foundX || foundZ))
			if (cy == Utils.pipeMaxPos) {
				minY = Utils.pipeMaxPos;
			} else {
				center = true;
			}

		if (minZ == 0 && maxZ != 1 && (foundX || foundY))
			if (cz == Utils.pipeMinPos) {
				maxZ = Utils.pipeMinPos;
			} else {
				center = true;
			}

		if (minZ != 0 && maxZ == 1 && (foundX || foundY))
			if (cz == Utils.pipeMaxPos) {
				minZ = Utils.pipeMaxPos;
			} else {
				center = true;
			}

		boolean found = foundX || foundY || foundZ;

		// Z render

		if (minZ != Utils.pipeMinPos || maxZ != Utils.pipeMaxPos || !found) {
			block.setBlockBounds(cx == Utils.pipeMinPos ? cx - 0.05F : cx, cy == Utils.pipeMinPos ? cy - 0.05F : cy, minZ, cx == Utils.pipeMinPos ? cx
					: cx + 0.05F, cy == Utils.pipeMinPos ? cy : cy + 0.05F, maxZ);
			renderblocks.updateCustomBlockBounds(block);
			renderblocks.renderStandardBlock(block, x, y, z);
		}

		// X render

		if (minX != Utils.pipeMinPos || maxX != Utils.pipeMaxPos || !found) {
			block.setBlockBounds(minX, cy == Utils.pipeMinPos ? cy - 0.05F : cy, cz == Utils.pipeMinPos ? cz - 0.05F : cz, maxX, cy == Utils.pipeMinPos ? cy
					: cy + 0.05F, cz == Utils.pipeMinPos ? cz : cz + 0.05F);
			renderblocks.updateCustomBlockBounds(block);
			renderblocks.renderStandardBlock(block, x, y, z);
		}

		// Y render

		if (minY != Utils.pipeMinPos || maxY != Utils.pipeMaxPos || !found) {
			block.setBlockBounds(cx == Utils.pipeMinPos ? cx - 0.05F : cx, minY, cz == Utils.pipeMinPos ? cz - 0.05F : cz, cx == Utils.pipeMinPos ? cx
					: cx + 0.05F, maxY, cz == Utils.pipeMinPos ? cz : cz + 0.05F);
			renderblocks.updateCustomBlockBounds(block);
			renderblocks.renderStandardBlock(block, x, y, z);
		}

		if (center || !found) {
			block.setBlockBounds(cx == Utils.pipeMinPos ? cx - 0.05F : cx, cy == Utils.pipeMinPos ? cy - 0.05F : cy, cz == Utils.pipeMinPos ? cz - 0.05F : cz,
					cx == Utils.pipeMinPos ? cx : cx + 0.05F, cy == Utils.pipeMinPos ? cy : cy + 0.05F, cz == Utils.pipeMinPos ? cz : cz + 0.05F);
			renderblocks.updateCustomBlockBounds(block);
			renderblocks.renderStandardBlock(block, x, y, z);
		}

	}

	private void pipeGateRender(RenderBlocks renderblocks, Block block, PipeRenderState state, int x, int y, int z) {

		state.currentTextureIndex = state.getGateTextureIndex();

		float min = Utils.pipeMinPos + 0.05F;
		float max = Utils.pipeMaxPos - 0.05F;

		if (!state.pipeConnectionMatrix.isConnected(ForgeDirection.WEST) && !state.facadeMatrix.isConnected(ForgeDirection.WEST)) {
			block.setBlockBounds(Utils.pipeMinPos - 0.10F, min, min, Utils.pipeMinPos, max, max);
			renderblocks.updateCustomBlockBounds(block);
			renderblocks.renderStandardBlock(block, x, y, z);
		}

		if (!state.pipeConnectionMatrix.isConnected(ForgeDirection.EAST) && !state.facadeMatrix.isConnected(ForgeDirection.EAST)) {
			block.setBlockBounds(Utils.pipeMaxPos, min, min, Utils.pipeMaxPos + 0.10F, max, max);
			renderblocks.updateCustomBlockBounds(block);
			renderblocks.renderStandardBlock(block, x, y, z);
		}

		if (!state.pipeConnectionMatrix.isConnected(ForgeDirection.DOWN) && !state.facadeMatrix.isConnected(ForgeDirection.DOWN)) {
			block.setBlockBounds(min, Utils.pipeMinPos - 0.10F, min, max, Utils.pipeMinPos, max);
			renderblocks.updateCustomBlockBounds(block);
			renderblocks.renderStandardBlock(block, x, y, z);
		}

		if (!state.pipeConnectionMatrix.isConnected(ForgeDirection.UP) && !state.facadeMatrix.isConnected(ForgeDirection.UP)) {
			block.setBlockBounds(min, Utils.pipeMaxPos, min, max, Utils.pipeMaxPos + 0.10F, max);
			renderblocks.updateCustomBlockBounds(block);
			renderblocks.renderStandardBlock(block, x, y, z);
		}

		if (!state.pipeConnectionMatrix.isConnected(ForgeDirection.NORTH) && !state.facadeMatrix.isConnected(ForgeDirection.NORTH)) {
			block.setBlockBounds(min, min, Utils.pipeMinPos - 0.10F, max, max, Utils.pipeMinPos);
			renderblocks.updateCustomBlockBounds(block);
			renderblocks.renderStandardBlock(block, x, y, z);
		}

		if (!state.pipeConnectionMatrix.isConnected(ForgeDirection.SOUTH) && !state.facadeMatrix.isConnected(ForgeDirection.SOUTH)) {
			block.setBlockBounds(min, min, Utils.pipeMaxPos, max, max, Utils.pipeMaxPos + 0.10F);
			renderblocks.updateCustomBlockBounds(block);
			renderblocks.renderStandardBlock(block, x, y, z);
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
			renderPipe(renderer, world, block, pipeTile.getRenderState(), x, y, z);
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
