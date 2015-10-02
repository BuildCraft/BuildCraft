package buildcraft.builders.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.IBlockAccess;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.builders.BuilderProxy;
import buildcraft.core.CoreConstants;
import buildcraft.core.lib.render.FakeBlock;
import buildcraft.core.lib.render.RenderUtils;

public class RenderFrame implements ISimpleBlockRenderingHandler {
	private void renderTwoWayBlock(RenderBlocks renderblocks, FakeBlock stateHost, int x, int y, int z, float[] dim, int mask) {
		assert mask != 0;

		stateHost.setRenderMask(mask);
		renderblocks.setRenderBounds(dim[2], dim[0], dim[1], dim[5], dim[3], dim[4]);
		renderblocks.renderStandardBlockWithColorMultiplier(stateHost, x, y, z, 1.0f, 1.0f, 1.0f);

		stateHost.setRenderMask((mask & 0x15) << 1 | (mask & 0x2a) >> 1); // pairwise swapped mask
		renderblocks.setRenderBounds(dim[5], dim[3], dim[4], dim[2], dim[0], dim[1]);
		renderblocks.renderStandardBlockWithColorMultiplier(stateHost, x, y, z, 0.8f, 0.8f, 0.8f);

		stateHost.setRenderAllSides();
	}

	private void resetToCenterDimensions(float[] dim) {
		for (int i = 0; i < 3; i++) {
			dim[i] = CoreConstants.PIPE_MIN_POS;
			dim[i + 3] = CoreConstants.PIPE_MAX_POS;
		}
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
		renderer.setRenderBounds(0.25F, 0.0F, 0.25F, 0.75F, 1.0F, 0.75F);
		RenderUtils.drawBlockItem(renderer, Tessellator.instance, block, metadata);
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		FakeBlock fakeBlock = FakeBlock.INSTANCE;
		fakeBlock.getTextureState().set(block.getIcon(0, 0));

		int connectivity = 0;
		int connections = 0;

		float[] dim = new float[6];
		resetToCenterDimensions(dim);

		for (int i = 0; i < 6; i++) {
			ForgeDirection d = ForgeDirection.getOrientation(i);
			if (world.getBlock(x + d.offsetX, y + d.offsetY, z + d.offsetZ) == block) {
				connectivity |= 1 << i;
				connections++;
			}
		}

		if (connections != 2) {
			renderTwoWayBlock(renderer, fakeBlock, x, y, z, dim, 0x3f);
		} else {
			renderTwoWayBlock(renderer, fakeBlock, x, y, z, dim, connectivity ^ 0x3f);
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
			int renderMask = (3 << (dir & 0x6)) ^ 0x3f;
			renderTwoWayBlock(renderer, fakeBlock, x, y, z, dim, renderMask);
		}

		renderer.setRenderBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
		return true;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId) {
		return true;
	}

	@Override
	public int getRenderId() {
		return BuilderProxy.frameRenderId;
	}
}
