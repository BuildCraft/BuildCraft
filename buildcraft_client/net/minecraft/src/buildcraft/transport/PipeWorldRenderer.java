package net.minecraft.src.buildcraft.transport;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.TreeMap;

import net.minecraft.src.Block;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.Item;
import net.minecraft.src.ItemBlock;
import net.minecraft.src.ItemStack;
import net.minecraft.src.RenderBlocks;
import net.minecraft.src.buildcraft.api.IPipe;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.IPipe.WireColor;
import net.minecraft.src.buildcraft.core.DefaultProps;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.forge.MinecraftForgeClient;

public class PipeWorldRenderer {
	
	public void renderPipe(RenderBlocks renderblocks, IBlockAccess iblockaccess, Block block, PipeRenderState state, int x, int y, int z) {
		
		float minSize = Utils.pipeMinPos;
		float maxSize = Utils.pipeMaxPos;

		MinecraftForgeClient.bindTexture(state.getTextureFile());
		
		state.currentTextureIndex = state.textureMatrix.getTextureIndex(Orientations.Unknown);
		block.setBlockBounds(minSize, minSize, minSize, maxSize, maxSize, maxSize);
		renderblocks.renderStandardBlock(block, x, y, z);

		if (state.pipeConnectionMatrix.isConnected(Orientations.XNeg)) {
			state.currentTextureIndex = state.textureMatrix.getTextureIndex(Orientations.XNeg);
			block.setBlockBounds(0.0F, minSize, minSize, minSize, maxSize, maxSize);
			renderblocks.renderStandardBlock(block, x, y, z);
		}

		if (state.pipeConnectionMatrix.isConnected(Orientations.XPos)) {
			state.currentTextureIndex = state.textureMatrix.getTextureIndex(Orientations.XPos);
			block.setBlockBounds(maxSize, minSize, minSize, 1.0F, maxSize, maxSize);
			renderblocks.renderStandardBlock(block, x, y, z);
		}

		if (state.pipeConnectionMatrix.isConnected(Orientations.YNeg)) {
			state.currentTextureIndex = state.textureMatrix.getTextureIndex(Orientations.YNeg);
			block.setBlockBounds(minSize, 0.0F, minSize, maxSize, minSize, maxSize);
			renderblocks.renderStandardBlock(block, x, y, z);
		}

		if (state.pipeConnectionMatrix.isConnected(Orientations.YPos)) {
			state.currentTextureIndex = state.textureMatrix.getTextureIndex(Orientations.YPos);
			block.setBlockBounds(minSize, maxSize, minSize, maxSize, 1.0F, maxSize);
			renderblocks.renderStandardBlock(block, x, y, z);
		}

		if (state.pipeConnectionMatrix.isConnected(Orientations.ZNeg)) {
			state.currentTextureIndex = state.textureMatrix.getTextureIndex(Orientations.ZNeg);
			block.setBlockBounds(minSize, minSize, 0.0F, maxSize, maxSize, minSize);
			renderblocks.renderStandardBlock(block, x, y, z);
		}

		if (state.pipeConnectionMatrix.isConnected(Orientations.ZPos)) {
			state.currentTextureIndex = state.textureMatrix.getTextureIndex(Orientations.ZPos);
			block.setBlockBounds(minSize, minSize, maxSize, maxSize, maxSize, 1.0F);
			renderblocks.renderStandardBlock(block, x, y, z);
		}

		block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);

		MinecraftForgeClient.bindTexture(DefaultProps.TEXTURE_BLOCKS);

		if (state.wireMatrix.hasWire(WireColor.Red)) {
			state.currentTextureIndex = state.wireMatrix.getTextureIndex(WireColor.Red);
			pipeWireRender(renderblocks, block, state, Utils.pipeMinPos, Utils.pipeMaxPos,
					Utils.pipeMinPos, IPipe.WireColor.Red, x, y, z);
		}

		if (state.wireMatrix.hasWire(WireColor.Blue)) {
			state.currentTextureIndex = state.wireMatrix.getTextureIndex(WireColor.Blue);
			pipeWireRender(renderblocks, block, state, Utils.pipeMaxPos, Utils.pipeMaxPos,
					Utils.pipeMaxPos, IPipe.WireColor.Blue, x, y, z);
		}

		if (state.wireMatrix.hasWire(WireColor.Green)) {
			state.currentTextureIndex = state.wireMatrix.getTextureIndex(WireColor.Green);
			pipeWireRender(renderblocks, block, state, Utils.pipeMaxPos, Utils.pipeMinPos,
					Utils.pipeMinPos, IPipe.WireColor.Green, x, y, z);
		}

		if (state.wireMatrix.hasWire(WireColor.Yellow)) {
			state.currentTextureIndex = state.wireMatrix.getTextureIndex(WireColor.Yellow);
			pipeWireRender(renderblocks, block, state, Utils.pipeMinPos, Utils.pipeMinPos,
					Utils.pipeMaxPos, IPipe.WireColor.Yellow, x, y, z);
		}

		if (state.hasGate())
			pipeGateRender(renderblocks, block, state, x, y, z);
		
		pipeFacadeRenderer(renderblocks, block, state, x, y, z);
			
	}
	
	private void pipeFacadeRenderer(RenderBlocks renderblocks, Block block, PipeRenderState state, int x, int y, int z) {
		
		float facadeThickness = 1F / 16F;
		float zFightOffset = 1F / 8192F;
		
		if (state.facadeMatrix.isConnected(Orientations.XNeg)){
			MinecraftForgeClient.bindTexture(state.facadeMatrix.getTextureFile(Orientations.XNeg));
			state.currentTextureIndex = state.facadeMatrix.getTextureIndex(Orientations.XNeg);
			block.setBlockBounds(0.0F -  zFightOffset, 0.0F, 0.0F, facadeThickness, 1.0F, 1F);
			renderblocks.renderStandardBlock(block, x, y, z);
		}
		
		if (state.facadeMatrix.isConnected(Orientations.XPos)){
			MinecraftForgeClient.bindTexture(state.facadeMatrix.getTextureFile(Orientations.XPos));
			state.currentTextureIndex = state.facadeMatrix.getTextureIndex(Orientations.XPos);
			block.setBlockBounds(1F-facadeThickness, 0.0F, 0.0F, 1.0F + zFightOffset, 1.0F, 1F);
			renderblocks.renderStandardBlock(block, x, y, z);
		}
		
		
		if (state.facadeMatrix.isConnected(Orientations.YNeg)){
			MinecraftForgeClient.bindTexture(state.facadeMatrix.getTextureFile(Orientations.YNeg));
			state.currentTextureIndex = state.facadeMatrix.getTextureIndex(Orientations.YNeg);
			block.setBlockBounds(0.0F, 0.0F -  zFightOffset, 0.0F, 1.0F, facadeThickness, 1F);
			renderblocks.renderStandardBlock(block, x, y, z);
		}

		
		if (state.facadeMatrix.isConnected(Orientations.YPos)){
			MinecraftForgeClient.bindTexture(state.facadeMatrix.getTextureFile(Orientations.YPos));
			state.currentTextureIndex = state.facadeMatrix.getTextureIndex(Orientations.YPos);
			block.setBlockBounds(0.0F, 1F-facadeThickness, 0.0F, 1.0F, 1.0F + zFightOffset , 1F);
			renderblocks.renderStandardBlock(block, x, y, z);
		}
		
		if (state.facadeMatrix.isConnected(Orientations.ZNeg)){
			MinecraftForgeClient.bindTexture(state.facadeMatrix.getTextureFile(Orientations.ZNeg));
			state.currentTextureIndex = state.facadeMatrix.getTextureIndex(Orientations.ZNeg);
			block.setBlockBounds(0.0F, 0.0F, 0.0F -  zFightOffset, 1.0F, 1F, facadeThickness);
			renderblocks.renderStandardBlock(block, x, y, z);
		}

	
		if (state.facadeMatrix.isConnected(Orientations.ZPos)){
			MinecraftForgeClient.bindTexture(state.facadeMatrix.getTextureFile(Orientations.ZPos));
			state.currentTextureIndex = state.facadeMatrix.getTextureIndex(Orientations.ZPos);
			block.setBlockBounds(0.0F, 0.0F, 1F-facadeThickness, 1.0F, 1F, 1.0F + zFightOffset);
			renderblocks.renderStandardBlock(block, x, y, z);
		}
		
		MinecraftForgeClient.bindTexture(DefaultProps.TEXTURE_BLOCKS);
		state.currentTextureIndex = 7 * 16 + 13; // Structure Pipe
		
		if (state.facadeMatrix.isConnected(Orientations.XNeg) && !state.pipeConnectionMatrix.isConnected(Orientations.XNeg)){
			block.setBlockBounds(0 + facadeThickness, Utils.pipeMinPos, Utils.pipeMinPos, Utils.pipeMinPos, Utils.pipeMaxPos, Utils.pipeMaxPos);
			renderblocks.renderStandardBlock(block, x, y, z);
		}
		
		if (state.facadeMatrix.isConnected(Orientations.XPos) && !state.pipeConnectionMatrix.isConnected(Orientations.XPos)){
			block.setBlockBounds(Utils.pipeMaxPos, Utils.pipeMinPos, Utils.pipeMinPos, 1F - facadeThickness, Utils.pipeMaxPos, Utils.pipeMaxPos);
			renderblocks.renderStandardBlock(block, x, y, z);
		}
		
		if (state.facadeMatrix.isConnected(Orientations.YNeg) && !state.pipeConnectionMatrix.isConnected(Orientations.YNeg)){
			block.setBlockBounds(Utils.pipeMinPos, 0 + facadeThickness, Utils.pipeMinPos, Utils.pipeMaxPos, Utils.pipeMinPos, Utils.pipeMaxPos);
			renderblocks.renderStandardBlock(block, x, y, z);
		}
		
		if (state.facadeMatrix.isConnected(Orientations.YPos) && !state.pipeConnectionMatrix.isConnected(Orientations.YPos)){
			block.setBlockBounds(Utils.pipeMinPos, Utils.pipeMaxPos, Utils.pipeMinPos, Utils.pipeMaxPos, 1F - facadeThickness, Utils.pipeMaxPos);
			renderblocks.renderStandardBlock(block, x, y, z);
		}
		
		if (state.facadeMatrix.isConnected(Orientations.ZNeg) && !state.pipeConnectionMatrix.isConnected(Orientations.ZNeg)){
			block.setBlockBounds(Utils.pipeMinPos, Utils.pipeMinPos, 0 + facadeThickness, Utils.pipeMaxPos, Utils.pipeMaxPos, Utils.pipeMinPos);
			renderblocks.renderStandardBlock(block, x, y, z);
		}
	
		if (state.facadeMatrix.isConnected(Orientations.ZPos) && !state.pipeConnectionMatrix.isConnected(Orientations.ZPos)){
			block.setBlockBounds(Utils.pipeMinPos, Utils.pipeMinPos, Utils.pipeMaxPos, Utils.pipeMaxPos, Utils.pipeMaxPos, 1F - facadeThickness);
			renderblocks.renderStandardBlock(block, x, y, z);
		}
	}

	private void pipeWireRender(RenderBlocks renderblocks, Block block, PipeRenderState state, float cx, float cy, float cz, IPipe.WireColor color, int x, int y, int z) {

		float minX = Utils.pipeMinPos;
		float minY = Utils.pipeMinPos;
		float minZ = Utils.pipeMinPos;

		float maxX = Utils.pipeMaxPos;
		float maxY = Utils.pipeMaxPos;
		float maxZ = Utils.pipeMaxPos;

		boolean foundX = false, foundY = false, foundZ = false;

		if (state.wireMatrix.isWireConnected(color, Orientations.XNeg)) {
			minX = 0;
			foundX = true;
		}

		if (state.wireMatrix.isWireConnected(color, Orientations.XPos)) {
			maxX = 1;
			foundX = true;
		}

		if (state.wireMatrix.isWireConnected(color, Orientations.YNeg)) {
			minY = 0;
			foundY = true;
		}

		if (state.wireMatrix.isWireConnected(color, Orientations.YPos)) {
			maxY = 1;
			foundY = true;
		}

		if (state.wireMatrix.isWireConnected(color, Orientations.ZNeg)) {
			minZ = 0;
			foundZ = true;
		}

		if (state.wireMatrix.isWireConnected(color, Orientations.ZPos)) {
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

			renderblocks.renderStandardBlock(block, x, y, z);
		}

		// X render

		if (minX != Utils.pipeMinPos || maxX != Utils.pipeMaxPos || !found) {
			block.setBlockBounds(minX, cy == Utils.pipeMinPos ? cy - 0.05F : cy, cz == Utils.pipeMinPos ? cz - 0.05F : cz, maxX,
					cy == Utils.pipeMinPos ? cy : cy + 0.05F, cz == Utils.pipeMinPos ? cz : cz + 0.05F);

			renderblocks.renderStandardBlock(block, x, y, z);
		}

		// Y render

		if (minY != Utils.pipeMinPos || maxY != Utils.pipeMaxPos || !found) {
			block.setBlockBounds(cx == Utils.pipeMinPos ? cx - 0.05F : cx, minY, cz == Utils.pipeMinPos ? cz - 0.05F : cz,
					cx == Utils.pipeMinPos ? cx : cx + 0.05F, maxY, cz == Utils.pipeMinPos ? cz : cz + 0.05F);

			renderblocks.renderStandardBlock(block, x, y, z);
		}

		if (center || !found) {
			block.setBlockBounds(cx == Utils.pipeMinPos ? cx - 0.05F : cx, cy == Utils.pipeMinPos ? cy - 0.05F : cy,
					cz == Utils.pipeMinPos ? cz - 0.05F : cz, cx == Utils.pipeMinPos ? cx : cx + 0.05F,
					cy == Utils.pipeMinPos ? cy : cy + 0.05F, cz == Utils.pipeMinPos ? cz : cz + 0.05F);

			renderblocks.renderStandardBlock(block, x, y, z);
		}

	}
	
	private void pipeGateRender(RenderBlocks renderblocks, Block block, PipeRenderState state, int x, int y, int z) {

		state.currentTextureIndex = state.getGateTextureIndex();

		float min = Utils.pipeMinPos + 0.05F;
		float max = Utils.pipeMaxPos - 0.05F;

		if (!state.pipeConnectionMatrix.isConnected(Orientations.XNeg) && !state.facadeMatrix.isConnected(Orientations.XNeg)) {
			block.setBlockBounds(Utils.pipeMinPos - 0.10F, min, min, Utils.pipeMinPos, max, max);
			renderblocks.renderStandardBlock(block, x, y, z);
		}

		if (!state.pipeConnectionMatrix.isConnected(Orientations.XPos) && !state.facadeMatrix.isConnected(Orientations.XPos)) {
			block.setBlockBounds(Utils.pipeMaxPos, min, min, Utils.pipeMaxPos + 0.10F, max, max);
			renderblocks.renderStandardBlock(block, x, y, z);
		}

		if (!state.pipeConnectionMatrix.isConnected(Orientations.YNeg) && !state.facadeMatrix.isConnected(Orientations.YNeg)) {
			block.setBlockBounds(min, Utils.pipeMinPos - 0.10F, min, max, Utils.pipeMinPos, max);
			renderblocks.renderStandardBlock(block, x, y, z);
		}

		if (!state.pipeConnectionMatrix.isConnected(Orientations.YPos) && !state.facadeMatrix.isConnected(Orientations.YPos)) {
			block.setBlockBounds(min, Utils.pipeMaxPos, min, max, Utils.pipeMaxPos + 0.10F, max);
			renderblocks.renderStandardBlock(block, x, y, z);
		}

		if (!state.pipeConnectionMatrix.isConnected(Orientations.ZNeg) && !state.facadeMatrix.isConnected(Orientations.ZNeg)) {
			block.setBlockBounds(min, min, Utils.pipeMinPos - 0.10F, max, max, Utils.pipeMinPos);
			renderblocks.renderStandardBlock(block, x, y, z);
		}

		if (!state.pipeConnectionMatrix.isConnected(Orientations.ZPos) && !state.facadeMatrix.isConnected(Orientations.ZPos)) {
			block.setBlockBounds(min, min, Utils.pipeMaxPos, max, max, Utils.pipeMaxPos + 0.10F);
			renderblocks.renderStandardBlock(block, x, y, z);
		}
	}
}
