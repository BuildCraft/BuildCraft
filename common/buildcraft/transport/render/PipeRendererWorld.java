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
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.core.render.ITextureStates;
import buildcraft.api.pipes.IPipePluggableRenderer;
import buildcraft.api.pipes.PipePluggable;
import buildcraft.core.CoreConstants;
import buildcraft.core.utils.ColorUtils;
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
					fakeBlock.getTextureState().set(icons.getIcon(state.textureMatrix.getTextureIndex(ForgeDirection.UNKNOWN)));
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
					fakeBlock.getTextureState().set(icons.getIcon(state.textureMatrix.getTextureIndex(ForgeDirection.VALID_DIRECTIONS[dir])));
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
		pipeFacadeRenderer(renderblocks, fakeBlock, tile, state, x, y, z);
		//block.setRenderAllSides();//Start fresh

		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			if (tile.hasPipePluggable(dir)) {
				PipePluggable p = tile.getPipePluggable(dir);
				IPipePluggableRenderer r = p.getRenderer();
				if (r != null) {
					r.renderPluggable(renderblocks, tile.getPipe(), dir, p, fakeBlock, renderPass, x, y, z);
				}
			}
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

	private void pipeFacadeRenderer(RenderBlocks renderblocks, ITextureStates blockStateMachine, TileGenericPipe tile, PipeRenderState state, int x, int y, int z) {
		FacadeRenderHelper.pipeFacadeRenderer(renderblocks, blockStateMachine, tile, state, x, y, z);
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
