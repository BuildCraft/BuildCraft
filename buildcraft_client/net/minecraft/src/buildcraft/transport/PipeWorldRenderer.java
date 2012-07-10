package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.Block;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.RenderBlocks;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.api.IPipe;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.IPipe.DrawingState;
import net.minecraft.src.buildcraft.core.DefaultProps;
import net.minecraft.src.buildcraft.core.ITileBufferHolder;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.forge.MinecraftForgeClient;

public class PipeWorldRenderer {
	
	public void renderPipe(RenderBlocks renderblocks, IBlockAccess iblockaccess, TileEntity tile, Block block, PipeRenderState state) {
		
		ITileBufferHolder holder = (ITileBufferHolder) tile;

		float minSize = Utils.pipeMinPos;
		float maxSize = Utils.pipeMaxPos;

		IPipe pipe = ((TileGenericPipe)tile).pipe;
		
		pipe.setDrawingState(DrawingState.DrawingPipe);

		pipe.prepareTextureFor(Orientations.Unknown);
		block.setBlockBounds(minSize, minSize, minSize, maxSize, maxSize, maxSize);
		renderblocks.renderStandardBlock(block, tile.xCoord, tile.yCoord, tile.zCoord);

		if (Utils.checkPipesConnections(tile, holder.getTile(Orientations.XNeg))) {
			pipe.prepareTextureFor(Orientations.XNeg);
			block.setBlockBounds(0.0F, minSize, minSize, minSize, maxSize, maxSize);
			renderblocks.renderStandardBlock(block, tile.xCoord, tile.yCoord, tile.zCoord);
		}

		if (Utils.checkPipesConnections(tile, holder.getTile(Orientations.XPos))) {
			pipe.prepareTextureFor(Orientations.XPos);
			block.setBlockBounds(maxSize, minSize, minSize, 1.0F, maxSize, maxSize);
			renderblocks.renderStandardBlock(block, tile.xCoord, tile.yCoord, tile.zCoord);
		}

		if (Utils.checkPipesConnections(tile, holder.getTile(Orientations.YNeg))) {
			pipe.prepareTextureFor(Orientations.YNeg);
			block.setBlockBounds(minSize, 0.0F, minSize, maxSize, minSize, maxSize);
			renderblocks.renderStandardBlock(block, tile.xCoord, tile.yCoord, tile.zCoord);
		}

		if (Utils.checkPipesConnections(tile, holder.getTile(Orientations.YPos))) {
			pipe.prepareTextureFor(Orientations.YPos);
			block.setBlockBounds(minSize, maxSize, minSize, maxSize, 1.0F, maxSize);
			renderblocks.renderStandardBlock(block, tile.xCoord, tile.yCoord, tile.zCoord);
		}

		if (Utils.checkPipesConnections(tile, holder.getTile(Orientations.ZNeg))) {
			pipe.prepareTextureFor(Orientations.ZNeg);
			block.setBlockBounds(minSize, minSize, 0.0F, maxSize, maxSize, minSize);
			renderblocks.renderStandardBlock(block, tile.xCoord, tile.yCoord, tile.zCoord);
		}

		if (Utils.checkPipesConnections(tile, holder.getTile(Orientations.ZPos))) {
			pipe.prepareTextureFor(Orientations.ZPos);
			block.setBlockBounds(minSize, minSize, maxSize, maxSize, maxSize, 1.0F);
			renderblocks.renderStandardBlock(block, tile.xCoord, tile.yCoord, tile.zCoord);
		}

		block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);

		pipe.prepareTextureFor(Orientations.Unknown);
		MinecraftForgeClient.bindTexture(DefaultProps.TEXTURE_BLOCKS);

		if (pipe.isWired(IPipe.WireColor.Red)) {
			pipe.setDrawingState(DrawingState.DrawingRedWire);
			pipeRedstoneRender(renderblocks, iblockaccess, tile, pipe, block, Utils.pipeMinPos, Utils.pipeMaxPos,
					Utils.pipeMinPos, IPipe.WireColor.Red);
		}

		if (pipe.isWired(IPipe.WireColor.Blue)) {
			pipe.setDrawingState(DrawingState.DrawingBlueWire);
			pipeRedstoneRender(renderblocks, iblockaccess, tile, pipe, block, Utils.pipeMaxPos, Utils.pipeMaxPos,
					Utils.pipeMaxPos, IPipe.WireColor.Blue);
		}

		if (pipe.isWired(IPipe.WireColor.Green)) {
			pipe.setDrawingState(DrawingState.DrawingGreenWire);
			pipeRedstoneRender(renderblocks, iblockaccess, tile, pipe, block, Utils.pipeMaxPos, Utils.pipeMinPos,
					Utils.pipeMinPos, IPipe.WireColor.Green);
		}

		if (pipe.isWired(IPipe.WireColor.Yellow)) {
			pipe.setDrawingState(DrawingState.DrawingYellowWire);
			pipeRedstoneRender(renderblocks, iblockaccess, tile, pipe, block, Utils.pipeMinPos, Utils.pipeMinPos,
					Utils.pipeMaxPos, IPipe.WireColor.Yellow);
		}

		if (pipe.hasInterface())
			pipeInterfaceRender(renderblocks, iblockaccess, tile, pipe, block);
	}
	
	private void pipeRedstoneRender(RenderBlocks renderblocks, IBlockAccess iblockaccess, TileEntity tile, IPipe pipe, 
			Block block, float cx, float cy, float cz, IPipe.WireColor color) {

		ITileBufferHolder holder = (ITileBufferHolder) tile;

		float minX = Utils.pipeMinPos;
		float minY = Utils.pipeMinPos;
		float minZ = Utils.pipeMinPos;

		float maxX = Utils.pipeMaxPos;
		float maxY = Utils.pipeMaxPos;
		float maxZ = Utils.pipeMaxPos;

		boolean foundX = false, foundY = false, foundZ = false;

		if (isConnectedWiredPipe(pipe, holder.getTile(Orientations.XNeg), color)) {
			minX = 0;
			foundX = true;
		}

		if (isConnectedWiredPipe(pipe, holder.getTile(Orientations.XPos), color)) {
			maxX = 1;
			foundX = true;
		}

		if (isConnectedWiredPipe(pipe, holder.getTile(Orientations.YNeg), color)) {
			minY = 0;
			foundY = true;
		}

		if (isConnectedWiredPipe(pipe, holder.getTile(Orientations.YPos), color)) {
			maxY = 1;
			foundY = true;
		}

		if (isConnectedWiredPipe(pipe, holder.getTile(Orientations.ZNeg), color)) {
			minZ = 0;
			foundZ = true;
		}

		if (isConnectedWiredPipe(pipe, holder.getTile(Orientations.ZPos), color)) {
			maxZ = 1;
			foundZ = true;
		}

		boolean center = false;

		if (minX == 0 && maxX != 1 && (foundY || foundZ))
			if (cx == Utils.pipeMinPos)
				maxX = Utils.pipeMinPos;
			else
				center = true;

		if (minX != 0 && maxX == 1 && (foundY || foundZ))
			if (cx == Utils.pipeMaxPos)
				minX = Utils.pipeMaxPos;
			else
				center = true;

		if (minY == 0 && maxY != 1 && (foundX || foundZ))
			if (cy == Utils.pipeMinPos)
				maxY = Utils.pipeMinPos;
			else
				center = true;

		if (minY != 0 && maxY == 1 && (foundX || foundZ))
			if (cy == Utils.pipeMaxPos)
				minY = Utils.pipeMaxPos;
			else
				center = true;

		if (minZ == 0 && maxZ != 1 && (foundX || foundY))
			if (cz == Utils.pipeMinPos)
				maxZ = Utils.pipeMinPos;
			else
				center = true;

		if (minZ != 0 && maxZ == 1 && (foundX || foundY))
			if (cz == Utils.pipeMaxPos)
				minZ = Utils.pipeMaxPos;
			else
				center = true;

		boolean found = foundX || foundY || foundZ;

		// Z render

		if (minZ != Utils.pipeMinPos || maxZ != Utils.pipeMaxPos || !found) {
			block.setBlockBounds(cx == Utils.pipeMinPos ? cx - 0.05F : cx, cy == Utils.pipeMinPos ? cy - 0.05F : cy, minZ,
					cx == Utils.pipeMinPos ? cx : cx + 0.05F, cy == Utils.pipeMinPos ? cy : cy + 0.05F, maxZ);

			renderblocks.renderStandardBlock(block, tile.xCoord, tile.yCoord, tile.zCoord);
		}

		// X render

		if (minX != Utils.pipeMinPos || maxX != Utils.pipeMaxPos || !found) {
			block.setBlockBounds(minX, cy == Utils.pipeMinPos ? cy - 0.05F : cy, cz == Utils.pipeMinPos ? cz - 0.05F : cz, maxX,
					cy == Utils.pipeMinPos ? cy : cy + 0.05F, cz == Utils.pipeMinPos ? cz : cz + 0.05F);

			renderblocks.renderStandardBlock(block, tile.xCoord, tile.yCoord, tile.zCoord);
		}

		// Y render

		if (minY != Utils.pipeMinPos || maxY != Utils.pipeMaxPos || !found) {
			block.setBlockBounds(cx == Utils.pipeMinPos ? cx - 0.05F : cx, minY, cz == Utils.pipeMinPos ? cz - 0.05F : cz,
					cx == Utils.pipeMinPos ? cx : cx + 0.05F, maxY, cz == Utils.pipeMinPos ? cz : cz + 0.05F);

			renderblocks.renderStandardBlock(block, tile.xCoord, tile.yCoord, tile.zCoord);
		}

		if (center || !found) {
			block.setBlockBounds(cx == Utils.pipeMinPos ? cx - 0.05F : cx, cy == Utils.pipeMinPos ? cy - 0.05F : cy,
					cz == Utils.pipeMinPos ? cz - 0.05F : cz, cx == Utils.pipeMinPos ? cx : cx + 0.05F,
					cy == Utils.pipeMinPos ? cy : cy + 0.05F, cz == Utils.pipeMinPos ? cz : cz + 0.05F);

			renderblocks.renderStandardBlock(block, tile.xCoord, tile.yCoord, tile.zCoord);
		}

	}
	
	private boolean isConnectedWiredPipe(IPipe pipe, TileEntity tile2, IPipe.WireColor color) {
		return pipe.isWireConnectedTo(tile2, color);
	}
	
	private void pipeInterfaceRender(RenderBlocks renderblocks, IBlockAccess iblockaccess, TileEntity tile, IPipe pipe,	Block block) {

		ITileBufferHolder holder = (ITileBufferHolder) tile;

		pipe.setDrawingState(DrawingState.DrawingGate);

		float min = Utils.pipeMinPos + 0.05F;
		float max = Utils.pipeMaxPos - 0.05F;

		if (!Utils.checkPipesConnections(tile, holder.getTile(Orientations.XNeg))) {
			block.setBlockBounds(Utils.pipeMinPos - 0.10F, min, min, Utils.pipeMinPos, max, max);

			renderblocks.renderStandardBlock(block, tile.xCoord, tile.yCoord, tile.zCoord);
		}

		if (!Utils.checkPipesConnections(tile, holder.getTile(Orientations.XPos))) {
			block.setBlockBounds(Utils.pipeMaxPos, min, min, Utils.pipeMaxPos + 0.10F, max, max);

			renderblocks.renderStandardBlock(block, tile.xCoord, tile.yCoord, tile.zCoord);
		}

		if (!Utils.checkPipesConnections(tile, holder.getTile(Orientations.YNeg))) {
			block.setBlockBounds(min, Utils.pipeMinPos - 0.10F, min, max, Utils.pipeMinPos, max);

			renderblocks.renderStandardBlock(block, tile.xCoord, tile.yCoord, tile.zCoord);
		}

		if (!Utils.checkPipesConnections(tile, holder.getTile(Orientations.YPos))) {
			block.setBlockBounds(min, Utils.pipeMaxPos, min, max, Utils.pipeMaxPos + 0.10F, max);

			renderblocks.renderStandardBlock(block, tile.xCoord, tile.yCoord, tile.zCoord);
		}

		if (!Utils.checkPipesConnections(tile, holder.getTile(Orientations.ZNeg))) {
			block.setBlockBounds(min, min, Utils.pipeMinPos - 0.10F, max, max, Utils.pipeMinPos);

			renderblocks.renderStandardBlock(block, tile.xCoord, tile.yCoord, tile.zCoord);
		}

		if (!Utils.checkPipesConnections(tile, holder.getTile(Orientations.ZPos))) {
			block.setBlockBounds(min, min, Utils.pipeMaxPos, max, max, Utils.pipeMaxPos + 0.10F);

			renderblocks.renderStandardBlock(block, tile.xCoord, tile.yCoord, tile.zCoord);
		}
	}
}
