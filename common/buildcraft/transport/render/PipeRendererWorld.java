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
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.util.EnumFacing;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.core.CoreConstants;
import buildcraft.core.utils.ColorUtils;
import buildcraft.core.utils.MatrixTranformations;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeRenderState;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.TransportProxy;

public class PipeRendererWorld implements ISimpleBlockRenderingHandler {
	
	public static int renderPass = -1;	
	public static float zFightOffset = 1F / 4096F;
	
	public void renderPipe(RenderBlocks renderblocks, IBlockAccess iblockaccess, TileGenericPipe tile, int x, int y, int z) {
		PipeRenderState state = tile.renderState;
		IIconProvider icons = tile.getPipeIcons();
		FakeBlock fakeBlock = FakeBlock.INSTANCE;
		int glassColor = tile.getColor();
		
		if (icons == null) {
			return;
		}	

		if (renderPass == 0 || glassColor >= 0) {
			// Pass 0 handles the pipe texture, pass 1 handles the transparent stained glass
			int connectivity = state.pipeConnectionMatrix.getMask();
			float[] dim = new float[6];
			
			if (renderPass == 1) {
				fakeBlock.setColor(ColorUtils.getRGBColor(glassColor));
			}
	
			// render the unconnected pipe faces of the center block (if any)
	
			if (connectivity != 0x3f) { // note: 0x3f = 0x111111 = all sides
				resetToCenterDimensions(dim);
				
				if (renderPass == 0) {
					fakeBlock.getTextureState().set(icons.getIcon(state.textureMatrix.getTextureIndex(null)));
				} else {
					fakeBlock.getTextureState().set(PipeIconProvider.TYPE.PipeStainedOverlay.getIcon());
				}
				
				fixForRenderPass(dim);
				
				renderTwoWayBlock(renderblocks, fakeBlock, x, y, z, dim, connectivity ^ 0x3f);
			}
	
			// render the connecting pipe faces
			for (int dir = 0; dir < 6; dir++) {
				int mask = 1 << dir;
	
				if ((connectivity & mask) == 0) {
					continue; // no connection towards dir
				}
	
				// center piece offsets
				resetToCenterDimensions(dim);
	
				// extend block towards dir as it's connected to there
				dim[dir / 2] = dir % 2 == 0 ? 0 : CoreConstants.PIPE_MAX_POS;
				dim[dir / 2 + 3] = dir % 2 == 0 ? CoreConstants.PIPE_MIN_POS : 1;
	
				// the mask points to all faces perpendicular to dir, i.e. dirs 0+1 -> mask 111100, 1+2 -> 110011, 3+5 -> 001111
				int renderMask = (3 << (dir / 2 * 2)) ^ 0x3f;
	
				fixForRenderPass(dim);
				
				// render sub block
				if (renderPass == 0) {
					fakeBlock.getTextureState().set(icons.getIcon(state.textureMatrix.getTextureIndex(EnumFacing.values()[dir])));
				} else {
					fakeBlock.getTextureState().set(PipeIconProvider.TYPE.PipeStainedOverlay.getIcon());
				}
				
				renderTwoWayBlock(renderblocks, fakeBlock, x, y, z, dim, renderMask);
			}
			
			fakeBlock.setColor(0xFFFFFF);
		} else if (renderPass == 1) {
			// Fix a bug in Minecraft 1.7.2-1.7.10
			// TODO: Remove in 1.8
			renderblocks.renderFaceXNeg(fakeBlock, x, y, z, PipeIconProvider.TYPE.Transparent.getIcon());
		}

		renderblocks.setRenderBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);

		// Facade renderer handles rendering in both passes
		pipeFacadeRenderer(renderblocks, fakeBlock, state, x, y, z);
		//block.setRenderAllSides();//Start fresh

		// Force other opaque renders into pass 0
		if (renderPass == 0) {
			pipePlugRenderer(renderblocks, fakeBlock, state, x, y, z);
			pipeRobotStationRenderer(renderblocks, fakeBlock, state, x, y, z);
		}
	}

	private void fixForRenderPass(float[] dim) {
		if (renderPass == 1) {
			for (int i = 0; i < 3; i++) {
				dim[i] += zFightOffset;
			}

			for (int i = 3; i < 6; i++) {
				dim[i] -= zFightOffset;
			}
		}
	}
	
	private void resetToCenterDimensions(float[] dim) {
		for (int i = 0; i < 3; i++) {
			dim[i] = CoreConstants.PIPE_MIN_POS;
		}

		for (int i = 3; i < 6; i++) {
			dim[i] = CoreConstants.PIPE_MAX_POS;
		}
	}

	/**
	 * Render a block with normal and inverted vertex order so back face culling
	 * doesn't have any effect.
	 */
	private void renderTwoWayBlock(RenderBlocks renderblocks, FakeBlock stateHost, int x, int y, int z, float[] dim, int mask) {
		assert mask != 0;
		
		stateHost.setRenderMask(mask);
		renderblocks.setRenderBounds(dim[2], dim[0], dim[1], dim[5], dim[3], dim[4]);
		renderblocks.renderStandardBlock(stateHost, x, y, z);
		stateHost.setRenderMask((mask & 0x15) << 1 | (mask & 0x2a) >> 1); // pairwise swapped mask
		renderblocks.setRenderBounds(dim[5], dim[3], dim[4], dim[2], dim[0], dim[1]);
		renderblocks.renderStandardBlock(stateHost, x, y, z);
	}

	private void pipeFacadeRenderer(RenderBlocks renderblocks, ITextureStates blockStateMachine, PipeRenderState state, int x, int y, int z) {
		FacadeRenderHelper.pipeFacadeRenderer(renderblocks, blockStateMachine, state, x, y, z);
	}

	private void pipePlugRenderer(RenderBlocks renderblocks, ITextureStates blockStateMachine, PipeRenderState state, int x, int y, int z) {
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

		blockStateMachine.getTextureState().set(BuildCraftTransport.instance.pipeIconProvider.getIcon(PipeIconProvider.TYPE.PipeStructureCobblestone.ordinal())); // Structure Pipe

		for (EnumFacing direction : EnumFacing.values()) {
			if (state.plugMatrix.isConnected(direction)) {
				float[][] rotated = MatrixTranformations.deepClone(zeroState);
				MatrixTranformations.transform(rotated, direction);

				renderblocks.setRenderBounds(rotated[0][0], rotated[1][0], rotated[2][0], rotated[0][1], rotated[1][1], rotated[2][1]);
				renderblocks.renderStandardBlock(blockStateMachine.getBlock(), x, y, z);
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

		blockStateMachine.getTextureState().set(BuildCraftTransport.instance.pipeIconProvider.getIcon(PipeIconProvider.TYPE.PipeStructureCobblestone.ordinal())); // Structure Pipe

		for (EnumFacing direction : EnumFacing.values()) {
			if (state.plugMatrix.isConnected(direction)) {
				float[][] rotated = MatrixTranformations.deepClone(zeroState);
				MatrixTranformations.transform(rotated, direction);

				renderblocks.setRenderBounds(rotated[0][0], rotated[1][0], rotated[2][0], rotated[0][1], rotated[1][1], rotated[2][1]);
				renderblocks.renderStandardBlock(blockStateMachine.getBlock(), x, y, z);
			}
		}

	}

	private void pipeRobotStationPartRender(RenderBlocks renderblocks,
			ITextureStates blockStateMachine, PipeRenderState state, int x, int y, int z,
			float xStart, float xEnd, float yStart, float yEnd, float zStart,
			float zEnd) {

		float[][] zeroState = new float[3][2];
		// X START - END
		zeroState[0][0] = xStart + zFightOffset;
		zeroState[0][1] = xEnd - zFightOffset;
		// Y START - END
		zeroState[1][0] = yStart;
		zeroState[1][1] = yEnd;
		// Z START - END
		zeroState[2][0] = zStart + zFightOffset;
		zeroState[2][1] = zEnd - zFightOffset;

		for (EnumFacing direction : EnumFacing.values()) {
			if (state.robotStationMatrix.isConnected(direction)) {
				switch (state.robotStationMatrix.getState(direction)) {
				case None:
				case Available:
					blockStateMachine.getTextureState().set(BuildCraftTransport.instance.pipeIconProvider
							.getIcon(PipeIconProvider.TYPE.PipeRobotStation.ordinal()));
					break;
				case Reserved:
					blockStateMachine.getTextureState().set(BuildCraftTransport.instance.pipeIconProvider
							.getIcon(PipeIconProvider.TYPE.PipeRobotStationReserved.ordinal()));
					break;
				case Linked:
					blockStateMachine.getTextureState().set(BuildCraftTransport.instance.pipeIconProvider
							.getIcon(PipeIconProvider.TYPE.PipeRobotStationLinked.ordinal()));
					break;
				}

				float[][] rotated = MatrixTranformations.deepClone(zeroState);
				MatrixTranformations.transform(rotated, direction);

				renderblocks.setRenderBounds(rotated[0][0], rotated[1][0],
						rotated[2][0], rotated[0][1], rotated[1][1],
						rotated[2][1]);
				renderblocks.renderStandardBlock(blockStateMachine.getBlock(), x, y, z);
			}
		}

	}

	private void pipeRobotStationRenderer(RenderBlocks renderblocks, ITextureStates blockStateMachine, PipeRenderState state, int x, int y, int z) {
		//float width = 0.075F;

		pipeRobotStationPartRender (renderblocks, blockStateMachine, state, x, y, z,
				0.45F, 0.55F,
				0.0F, 0.224F,
				0.45F, 0.55F);


		/*pipeRobotStationPartRender (renderblocks, block, state, x, y, z,
				0.25F, 0.75F,
				0.025F, 0.224F,
				0.25F, 0.25F + width);

		pipeRobotStationPartRender (renderblocks, block, state, x, y, z,
				0.25F, 0.75F,
				0.025F, 0.224F,
				0.75F - width, 0.75F);

		pipeRobotStationPartRender (renderblocks, block, state, x, y, z,
				0.25F, 0.25F + width,
				0.025F, 0.224F,
				0.25F + width, 0.75F - width);

		pipeRobotStationPartRender (renderblocks, block, state, x, y, z,
				0.75F - width, 0.75F,
				0.025F, 0.224F,
				0.25F + width, 0.75F - width);*/

		float[][] zeroState = new float[3][2];


		// X START - END
		zeroState[0][0] = 0.25F + zFightOffset;
		zeroState[0][1] = 0.75F - zFightOffset;
		// Y START - END
		zeroState[1][0] = 0.225F;
		zeroState[1][1] = 0.251F;
		// Z START - END
		zeroState[2][0] = 0.25F + zFightOffset;
		zeroState[2][1] = 0.75F - zFightOffset;

		for (EnumFacing direction : EnumFacing.values()) {
			if (state.robotStationMatrix.isConnected(direction)) {
				switch (state.robotStationMatrix.getState(direction)) {
				case None:
				case Available:
					blockStateMachine.getTextureState().set(BuildCraftTransport.instance.pipeIconProvider
							.getIcon(PipeIconProvider.TYPE.PipeRobotStation.ordinal()));
					break;
				case Reserved:
					blockStateMachine.getTextureState().set(BuildCraftTransport.instance.pipeIconProvider
							.getIcon(PipeIconProvider.TYPE.PipeRobotStationReserved.ordinal()));
					break;
				case Linked:
					blockStateMachine.getTextureState().set(BuildCraftTransport.instance.pipeIconProvider
							.getIcon(PipeIconProvider.TYPE.PipeRobotStationLinked.ordinal()));
					break;
				}

				float[][] rotated = MatrixTranformations.deepClone(zeroState);
				MatrixTranformations.transform(rotated, direction);

				renderblocks.setRenderBounds(rotated[0][0], rotated[1][0],
						rotated[2][0], rotated[0][1], rotated[1][1],
						rotated[2][1]);
				renderblocks.renderStandardBlock(blockStateMachine.getBlock(), x, y, z);
			}
		}
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		TileEntity tile = world.getTileEntity(x, y, z);

		// Here to prevent Minecraft from crashing when nothing renders on render pass zero
		// This is likely a bug, and has been submitted as an issue to the Forge team
		renderer.setRenderBounds(0, 0, 0, 0, 0, 0);
		renderer.renderStandardBlock(Blocks.stone, x, y, z);
		renderer.setRenderBoundsFromBlock(block);

		if (tile instanceof TileGenericPipe) {
			TileGenericPipe pipeTile = (TileGenericPipe) tile;
			renderPipe(renderer, world, pipeTile, x, y, z);
		}

		return true;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId) {
		return false;
	}

	@Override
	public int getRenderId() {
		return TransportProxy.pipeModel;
	}
}
