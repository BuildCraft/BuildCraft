/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.silicon;

import net.minecraft.src.Block;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.RenderBlocks;
import net.minecraft.src.Tessellator;


import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

import buildcraft.api.core.Orientations;
import buildcraft.core.Utils;
import buildcraft.silicon.SiliconProxyClient;

public class SiliconRenderBlock implements ISimpleBlockRenderingHandler {
	@Override
	public int getRenderId() {
		return SiliconProxyClient.laserBlockModel;
	}
	@Override
	public boolean shouldRender3DInInventory() {
		return true;
	}
	@Override
	public boolean renderWorldBlock(IBlockAccess iblockaccess, int x, int y, int z, Block block, int l, RenderBlocks renderblocks) {

		int meta = iblockaccess.getBlockMetadata(x, y, z);

		if (meta == Orientations.XPos.ordinal()) {
			renderblocks.uvRotateEast = 2;
			renderblocks.uvRotateWest = 1;
			renderblocks.uvRotateTop = 1;
			renderblocks.uvRotateBottom = 2;

			block.setBlockBounds(0.0F, 0.0F, 0.0F, 4F / 16F, 1, 1);
			renderblocks.renderStandardBlock(block, x, y, z);

			block.setBlockBounds(4F / 16F, 5F / 16F, 5F / 16F, 13F / 16F, 11F / 16F, 11F / 16F);
			renderblocks.renderStandardBlock(block, x, y, z);
		} else if (meta == Orientations.XNeg.ordinal()) {
			renderblocks.uvRotateEast = 1;
			renderblocks.uvRotateWest = 2;
			renderblocks.uvRotateTop = 2;
			renderblocks.uvRotateBottom = 1;

			block.setBlockBounds(1F - 4F / 16F, 0.0F, 0.0F, 1, 1, 1);
			renderblocks.renderStandardBlock(block, x, y, z);

			block.setBlockBounds(1F - 13F / 16F, 5F / 16F, 5F / 16F, 1F - 4F / 16F, 11F / 16F, 11F / 16F);
			renderblocks.renderStandardBlock(block, x, y, z);
		} else if (meta == Orientations.ZNeg.ordinal()) {
			renderblocks.uvRotateSouth = 1;
			renderblocks.uvRotateNorth = 2;

			block.setBlockBounds(0.0F, 0.0F, 1F - 4F / 16F, 1, 1, 1);
			renderblocks.renderStandardBlock(block, x, y, z);

			block.setBlockBounds(5F / 16F, 5F / 16F, 1F - 13F / 16F, 11F / 16F, 11F / 16F, 1F - 4F / 16F);
			renderblocks.renderStandardBlock(block, x, y, z);
		} else if (meta == Orientations.ZPos.ordinal()) {
			renderblocks.uvRotateSouth = 2;
			renderblocks.uvRotateNorth = 1;
			renderblocks.uvRotateTop = 3;
			renderblocks.uvRotateBottom = 3;

			block.setBlockBounds(0.0F, 0.0F, 0.0F, 1, 1, 4F / 16F);
			renderblocks.renderStandardBlock(block, x, y, z);

			block.setBlockBounds(5F / 16F, 5F / 16F, 4F / 16F, 11F / 16F, 11F / 16F, 13F / 16F);
			renderblocks.renderStandardBlock(block, x, y, z);
		} else if (meta == Orientations.YNeg.ordinal()) {
			renderblocks.uvRotateEast = 3;
			renderblocks.uvRotateWest = 3;
			renderblocks.uvRotateSouth = 3;
			renderblocks.uvRotateNorth = 3;

			block.setBlockBounds(0.0F, 1.0F - 4F / 16F, 0.0F, 1.0F, 1.0F, 1.0F);
			renderblocks.renderStandardBlock(block, x, y, z);

			block.setBlockBounds(5F / 16F, 1F - 13F / 16F, 5F / 16F, 11F / 16F, 1F - 4F / 16F, 11F / 16F);
			renderblocks.renderStandardBlock(block, x, y, z);
		} else if (meta == Orientations.YPos.ordinal()) {
			block.setBlockBounds(0.0F, 0.0F, 0.0F, 1, 4F / 16F, 1);
			renderblocks.renderStandardBlock(block, x, y, z);

			block.setBlockBounds(5F / 16F, 4F / 16F, 5F / 16F, 11F / 16F, 13F / 16F, 11F / 16F);
			renderblocks.renderStandardBlock(block, x, y, z);
		}

		block.setBlockBounds(0.0F, 0.0F, 0.0F, 1, 1, 1);
		renderblocks.uvRotateEast = 0;
		renderblocks.uvRotateWest = 0;
		renderblocks.uvRotateSouth = 0;
		renderblocks.uvRotateNorth = 0;
		renderblocks.uvRotateTop = 0;
		renderblocks.uvRotateBottom = 0;

		return true;

	}

	@Override
	public void renderInventoryBlock(Block block, int i, int j, RenderBlocks renderblocks) {
		block.setBlockBounds(Utils.pipeMinPos, 0.0F, Utils.pipeMinPos, Utils.pipeMaxPos, 1.0F, Utils.pipeMaxPos);
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);

		block.setBlockBounds(0.0F, 0.0F, 0.0F, 1, 4F / 16F, 1);
		renderBlockInInv(renderblocks, block, 0);

		block.setBlockBounds(5F / 16F, 4F / 16F, 5F / 16F, 11F / 16F, 13F / 16F, 11F / 16F);
		renderBlockInInv(renderblocks, block, 1);

		GL11.glTranslatef(0.5F, 0.5F, 0.5F);
		block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
	}

	private void renderBlockInInv(RenderBlocks renderblocks, Block block, int i) {
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, -1F, 0.0F);
		renderblocks.renderBottomFace(block, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSideAndMetadata(0, i));
		tessellator.draw();

		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 1.0F, 0.0F);
		renderblocks.renderTopFace(block, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSideAndMetadata(1, i));
		tessellator.draw();

		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 0.0F, -1F);
		renderblocks.renderEastFace(block, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSideAndMetadata(2, i));
		tessellator.draw();

		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 0.0F, 1.0F);
		renderblocks.renderWestFace(block, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSideAndMetadata(3, i));
		tessellator.draw();

		tessellator.startDrawingQuads();
		tessellator.setNormal(-1F, 0.0F, 0.0F);
		renderblocks.renderNorthFace(block, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSideAndMetadata(4, i));
		tessellator.draw();

		tessellator.startDrawingQuads();
		tessellator.setNormal(1.0F, 0.0F, 0.0F);
		renderblocks.renderSouthFace(block, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSideAndMetadata(5, i));
		tessellator.draw();
	}

	/*
	 * @Override public GuiScreen handleGUI(int i) { switch
	 * (Utils.intToPacketId(i)) { case AssemblyTableGUI: return new
	 * GuiAssemblyTable( ModLoader.getMinecraftInstance().thePlayer.inventory,
	 * new TileAssemblyTable()); default: return null; } }
	 */

	/*
	 * @Override public void handlePacket(Packet230ModLoader packet) { switch
	 * (PacketIds.values()[packet.packetType]) { case AssemblyTableSelect:
	 * GuiScreen screen = ModLoader.getMinecraftInstance().currentScreen;
	 *
	 * if (screen instanceof GuiAssemblyTable) { GuiAssemblyTable gui =
	 * (GuiAssemblyTable) screen; SelectionMessage message = new
	 * SelectionMessage();
	 *
	 * TileAssemblyTable.selectionMessageWrapper.updateFromPacket(message,
	 * packet);
	 *
	 * gui.handleSelectionMessage (message); }
	 *
	 * break; } }
	 */

}
