package buildcraft.transport.render;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.IPipe.WireColor;
import buildcraft.core.utils.Utils;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.IPipeRenderState;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeRenderState;
import buildcraft.transport.TransportProxy;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.ForgeDirection;

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
	 * Shifts the coordinates around effectivly rotating something. Zero state
	 * is DOWN then -> NORTH -> WEST Note - To obtain Pos, do a mirrorY() before
	 * rotating
	 *
	 * @param targetArray the array that should be rotated
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
	 * @param targetArray the array that should be transformed
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
	 * @param source the float[][] to deepClone
	 * @return
	 */
	private float[][] deepClone(float[][] source) {
		float[][] target = source.clone();
		for (int i = 0; i < target.length; i++) {
			target[i] = source[i].clone();
		}
		return target;
	}

	private void renderAllFaceExeptAxe(RenderBlocks renderblocks, Block block, Icon icon, int x, int y, int z, char axe) {
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

	private void renderTwoWayXFace(RenderBlocks renderblocks, Block block, Icon icon, int xCoord, int yCoord, int zCoord, float minY, float minZ, float maxY, float maxZ, float x) {
		renderblocks.setRenderBounds(x, minY, minZ, x, maxY, maxZ);
		renderblocks.renderStandardBlock(block, xCoord, yCoord, zCoord);
	}

	private void renderTwoWayYFace(RenderBlocks renderblocks, Block block, Icon icon, int xCoord, int yCoord, int zCoord, float minX, float minZ, float maxX, float maxZ, float y) {
		renderblocks.setRenderBounds(minX, y, minZ, maxX, y, maxZ);
		renderblocks.renderStandardBlock(block, xCoord, yCoord, zCoord);
	}

	private void renderTwoWayZFace(RenderBlocks renderblocks, Block block, Icon icon, int xCoord, int yCoord, int zCoord, float minX, float minY, float maxX, float maxY, float z) {
		renderblocks.setRenderBounds(minX, minY, z, maxX, maxY, z);
		renderblocks.renderStandardBlock(block, xCoord, yCoord, zCoord);
	}

	public void renderPipe(RenderBlocks renderblocks, IBlockAccess iblockaccess, Block block, IPipeRenderState renderState, int x, int y, int z) {

		float minSize = Utils.pipeMinPos;
		float maxSize = Utils.pipeMaxPos;

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

		if (state.wireMatrix.hasWire(WireColor.Red)) {
			state.currentTexture = BuildCraftTransport.instance.wireIconProvider.getIcon(state.wireMatrix.getWireIconIndex(WireColor.Red));

			pipeWireRender(renderblocks, block, state, Utils.pipeMinPos, Utils.pipeMaxPos, Utils.pipeMinPos, IPipe.WireColor.Red, x, y, z);
		}

		if (state.wireMatrix.hasWire(WireColor.Blue)) {
			state.currentTexture = BuildCraftTransport.instance.wireIconProvider.getIcon(state.wireMatrix.getWireIconIndex(WireColor.Blue));
			pipeWireRender(renderblocks, block, state, Utils.pipeMaxPos, Utils.pipeMaxPos, Utils.pipeMaxPos, IPipe.WireColor.Blue, x, y, z);
		}

		if (state.wireMatrix.hasWire(WireColor.Green)) {
			state.currentTexture = BuildCraftTransport.instance.wireIconProvider.getIcon(state.wireMatrix.getWireIconIndex(WireColor.Green));
			pipeWireRender(renderblocks, block, state, Utils.pipeMaxPos, Utils.pipeMinPos, Utils.pipeMinPos, IPipe.WireColor.Green, x, y, z);
		}

		if (state.wireMatrix.hasWire(WireColor.Yellow)) {
			state.currentTexture = BuildCraftTransport.instance.wireIconProvider.getIcon(state.wireMatrix.getWireIconIndex(WireColor.Yellow));
			pipeWireRender(renderblocks, block, state, Utils.pipeMinPos, Utils.pipeMinPos, Utils.pipeMaxPos, IPipe.WireColor.Yellow, x, y, z);
		}

		if (state.hasGate()) {
			pipeGateRender(renderblocks, block, state, x, y, z);
		}

		pipeFacadeRenderer(renderblocks, block, state, x, y, z);
		pipePlugRenderer(renderblocks, block, state, x, y, z);

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
			int facadeId = state.facadeMatrix.getFacadeBlockId(direction);
			if (facadeId != 0) {
				Block renderBlock = Block.blocksList[facadeId];
				int renderMeta = state.facadeMatrix.getFacadeMetaId(direction);
				state.currentTexture = renderBlock.getIcon(direction.ordinal(), renderMeta);

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
					float[][] rotated = deepClone(zeroState);
					rotated[2][0] = 0.0F;
					rotated[2][1] = Utils.pipeMinPos;
					rotated[1][0] -= zFightOffset / 2;
					transform(rotated, direction);
					renderblocks.setRenderBounds(rotated[0][0], rotated[1][0], rotated[2][0], rotated[0][1], rotated[1][1], rotated[2][1]);
					renderblocks.renderStandardBlock(block, x, y, z);

					rotated = deepClone(zeroState);
					rotated[2][0] = Utils.pipeMaxPos;
					rotated[1][0] -= zFightOffset / 2;
					transform(rotated, direction);
					renderblocks.setRenderBounds(rotated[0][0], rotated[1][0], rotated[2][0], rotated[0][1], rotated[1][1], rotated[2][1]);
					renderblocks.renderStandardBlock(block, x, y, z);

					rotated = deepClone(zeroState);
					rotated[0][0] = 0.0F;
					rotated[0][1] = Utils.pipeMinPos;
					rotated[1][1] -= zFightOffset;
					transform(rotated, direction);
					renderblocks.setRenderBounds(rotated[0][0], rotated[1][0], rotated[2][0], rotated[0][1], rotated[1][1], rotated[2][1]);
					renderblocks.renderStandardBlock(block, x, y, z);

					rotated = deepClone(zeroState);
					rotated[0][0] = Utils.pipeMaxPos;
					rotated[0][1] = 1F;
					rotated[1][1] -= zFightOffset;
					transform(rotated, direction);
					renderblocks.setRenderBounds(rotated[0][0], rotated[1][0], rotated[2][0], rotated[0][1], rotated[1][1], rotated[2][1]);
					renderblocks.renderStandardBlock(block, x, y, z);
				} else { // Solid facade
					float[][] rotated = deepClone(zeroState);
					transform(rotated, direction);
					renderblocks.setRenderBounds(rotated[0][0], rotated[1][0], rotated[2][0], rotated[0][1], rotated[1][1], rotated[2][1]);
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

		// X START - END
		zeroState[0][0] = Utils.pipeMinPos;
		zeroState[0][1] = Utils.pipeMaxPos;
		// Y START - END
		zeroState[1][0] = facadeThickness;
		zeroState[1][1] = Utils.pipeMinPos;
		// Z START - END
		zeroState[2][0] = Utils.pipeMinPos;
		zeroState[2][1] = Utils.pipeMaxPos;

		state.currentTexture = BuildCraftTransport.instance.pipeIconProvider.getIcon(PipeIconProvider.TYPE.PipeStructureCobblestone.ordinal()); // Structure Pipe

		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			if (state.facadeMatrix.getFacadeBlockId(direction) != 0 && !state.pipeConnectionMatrix.isConnected(direction)) {
				float[][] rotated = deepClone(zeroState);
				transform(rotated, direction);

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
				float[][] rotated = deepClone(zeroState);
				transform(rotated, direction);

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
				float[][] rotated = deepClone(zeroState);
				transform(rotated, direction);

				renderblocks.setRenderBounds(rotated[0][0], rotated[1][0], rotated[2][0], rotated[0][1], rotated[1][1], rotated[2][1]);
				renderblocks.renderStandardBlock(block, x, y, z);
			}
		}

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
			renderblocks.setRenderBounds(cx == Utils.pipeMinPos ? cx - 0.05F : cx, cy == Utils.pipeMinPos ? cy - 0.05F : cy, minZ, cx == Utils.pipeMinPos ? cx
					: cx + 0.05F, cy == Utils.pipeMinPos ? cy : cy + 0.05F, maxZ);
			renderblocks.renderStandardBlock(block, x, y, z);
		}

		// X render

		if (minX != Utils.pipeMinPos || maxX != Utils.pipeMaxPos || !found) {
			renderblocks.setRenderBounds(minX, cy == Utils.pipeMinPos ? cy - 0.05F : cy, cz == Utils.pipeMinPos ? cz - 0.05F : cz, maxX, cy == Utils.pipeMinPos ? cy
					: cy + 0.05F, cz == Utils.pipeMinPos ? cz : cz + 0.05F);
			renderblocks.renderStandardBlock(block, x, y, z);
		}

		// Y render

		if (minY != Utils.pipeMinPos || maxY != Utils.pipeMaxPos || !found) {
			renderblocks.setRenderBounds(cx == Utils.pipeMinPos ? cx - 0.05F : cx, minY, cz == Utils.pipeMinPos ? cz - 0.05F : cz, cx == Utils.pipeMinPos ? cx
					: cx + 0.05F, maxY, cz == Utils.pipeMinPos ? cz : cz + 0.05F);
			renderblocks.renderStandardBlock(block, x, y, z);
		}

		if (center || !found) {
			renderblocks.setRenderBounds(cx == Utils.pipeMinPos ? cx - 0.05F : cx, cy == Utils.pipeMinPos ? cy - 0.05F : cy, cz == Utils.pipeMinPos ? cz - 0.05F : cz,
					cx == Utils.pipeMinPos ? cx : cx + 0.05F, cy == Utils.pipeMinPos ? cy : cy + 0.05F, cz == Utils.pipeMinPos ? cz : cz + 0.05F);
			renderblocks.renderStandardBlock(block, x, y, z);
		}

	}

	private void pipeGateRender(RenderBlocks renderblocks, Block block, PipeRenderState state, int x, int y, int z) {

		state.currentTexture = BuildCraftTransport.instance.gateIconProvider.getIcon(state.getGateIconIndex());

		float min = Utils.pipeMinPos + 0.05F;
		float max = Utils.pipeMaxPos - 0.05F;

		if (shouldRenderNormalPipeSide(state, ForgeDirection.WEST)) {
			renderblocks.setRenderBounds(Utils.pipeMinPos - 0.10F, min, min, Utils.pipeMinPos + 0.001F, max, max);
			renderblocks.renderStandardBlock(block, x, y, z);
		}

		if (shouldRenderNormalPipeSide(state, ForgeDirection.EAST)) {
			renderblocks.setRenderBounds(Utils.pipeMaxPos + 0.001F, min, min, Utils.pipeMaxPos + 0.10F, max, max);
			renderblocks.renderStandardBlock(block, x, y, z);
		}

		if (shouldRenderNormalPipeSide(state, ForgeDirection.DOWN)) {
			renderblocks.setRenderBounds(min, Utils.pipeMinPos - 0.10F, min, max, Utils.pipeMinPos + 0.001F, max);
			renderblocks.renderStandardBlock(block, x, y, z);
		}

		if (shouldRenderNormalPipeSide(state, ForgeDirection.UP)) {
			renderblocks.setRenderBounds(min, Utils.pipeMaxPos + 0.001F, min, max, Utils.pipeMaxPos + 0.10F, max);
			renderblocks.renderStandardBlock(block, x, y, z);
		}

		if (shouldRenderNormalPipeSide(state, ForgeDirection.NORTH)) {
			renderblocks.setRenderBounds(min, min, Utils.pipeMinPos - 0.10F, max, max, Utils.pipeMinPos + 0.001F);
			renderblocks.renderStandardBlock(block, x, y, z);
		}

		if (shouldRenderNormalPipeSide(state, ForgeDirection.SOUTH)) {
			renderblocks.setRenderBounds(min, min, Utils.pipeMaxPos + 0.001F, max, max, Utils.pipeMaxPos + 0.10F);
			renderblocks.renderStandardBlock(block, x, y, z);
		}
	}

	private boolean shouldRenderNormalPipeSide(PipeRenderState state, ForgeDirection direction) {
		return !state.pipeConnectionMatrix.isConnected(direction) && state.facadeMatrix.getFacadeBlockId(direction) == 0 && !state.plugMatrix.isConnected(direction);
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
			renderPipe(renderer, world, block, pipeTile, x, y, z);
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
