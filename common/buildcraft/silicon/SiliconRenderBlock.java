/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.silicon;

import buildcraft.core.CoreConstants;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.ForgeDirection;
import org.lwjgl.opengl.GL11;

public class SiliconRenderBlock implements ISimpleBlockRenderingHandler {
	@Override
	public int getRenderId() {
		return SiliconProxy.laserBlockModel;
	}

	@Override
	public boolean shouldRender3DInInventory() {
		return true;
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess iblockaccess, int x, int y, int z, Block block, int l, RenderBlocks renderblocks) {

		int meta = iblockaccess.getBlockMetadata(x, y, z);

		if (meta == ForgeDirection.EAST.ordinal()) {
			renderblocks.uvRotateEast = 2;
			renderblocks.uvRotateWest = 1;
			renderblocks.uvRotateTop = 1;
			renderblocks.uvRotateBottom = 2;

			block.setBlockBounds(0.0F, 0.0F, 0.0F, 4F / 16F, 1, 1);
			renderblocks.setRenderBoundsFromBlock(block);
			renderblocks.renderStandardBlock(block, x, y, z);

			block.setBlockBounds(4F / 16F, 5F / 16F, 5F / 16F, 13F / 16F, 11F / 16F, 11F / 16F);
			renderblocks.setRenderBoundsFromBlock(block);
			renderblocks.renderStandardBlock(block, x, y, z);
		} else if (meta == ForgeDirection.WEST.ordinal()) {
			renderblocks.uvRotateEast = 1;
			renderblocks.uvRotateWest = 2;
			renderblocks.uvRotateTop = 2;
			renderblocks.uvRotateBottom = 1;

			block.setBlockBounds(1F - 4F / 16F, 0.0F, 0.0F, 1, 1, 1);
			renderblocks.setRenderBoundsFromBlock(block);
			renderblocks.renderStandardBlock(block, x, y, z);

			block.setBlockBounds(1F - 13F / 16F, 5F / 16F, 5F / 16F, 1F - 4F / 16F, 11F / 16F, 11F / 16F);
			renderblocks.setRenderBoundsFromBlock(block);
			renderblocks.renderStandardBlock(block, x, y, z);
		} else if (meta == ForgeDirection.NORTH.ordinal()) {
			renderblocks.uvRotateSouth = 1;
			renderblocks.uvRotateNorth = 2;

			block.setBlockBounds(0.0F, 0.0F, 1F - 4F / 16F, 1, 1, 1);
			renderblocks.setRenderBoundsFromBlock(block);
			renderblocks.renderStandardBlock(block, x, y, z);

			block.setBlockBounds(5F / 16F, 5F / 16F, 1F - 13F / 16F, 11F / 16F, 11F / 16F, 1F - 4F / 16F);
			renderblocks.setRenderBoundsFromBlock(block);
			renderblocks.renderStandardBlock(block, x, y, z);
		} else if (meta == ForgeDirection.SOUTH.ordinal()) {
			renderblocks.uvRotateSouth = 2;
			renderblocks.uvRotateNorth = 1;
			renderblocks.uvRotateTop = 3;
			renderblocks.uvRotateBottom = 3;

			block.setBlockBounds(0.0F, 0.0F, 0.0F, 1, 1, 4F / 16F);
			renderblocks.setRenderBoundsFromBlock(block);
			renderblocks.renderStandardBlock(block, x, y, z);

			block.setBlockBounds(5F / 16F, 5F / 16F, 4F / 16F, 11F / 16F, 11F / 16F, 13F / 16F);
			renderblocks.setRenderBoundsFromBlock(block);
			renderblocks.renderStandardBlock(block, x, y, z);
		} else if (meta == ForgeDirection.DOWN.ordinal()) {
			renderblocks.uvRotateEast = 3;
			renderblocks.uvRotateWest = 3;
			renderblocks.uvRotateSouth = 3;
			renderblocks.uvRotateNorth = 3;

			block.setBlockBounds(0.0F, 1.0F - 4F / 16F, 0.0F, 1.0F, 1.0F, 1.0F);
			renderblocks.setRenderBoundsFromBlock(block);
			renderblocks.renderStandardBlock(block, x, y, z);

			block.setBlockBounds(5F / 16F, 1F - 13F / 16F, 5F / 16F, 11F / 16F, 1F - 4F / 16F, 11F / 16F);
			renderblocks.setRenderBoundsFromBlock(block);
			renderblocks.renderStandardBlock(block, x, y, z);
		} else if (meta == ForgeDirection.UP.ordinal()) {
			block.setBlockBounds(0.0F, 0.0F, 0.0F, 1, 4F / 16F, 1);
			renderblocks.setRenderBoundsFromBlock(block);
			renderblocks.renderStandardBlock(block, x, y, z);

			block.setBlockBounds(5F / 16F, 4F / 16F, 5F / 16F, 11F / 16F, 13F / 16F, 11F / 16F);
			renderblocks.setRenderBoundsFromBlock(block);
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
		block.setBlockBounds(CoreConstants.PIPE_MIN_POS, 0.0F, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MAX_POS, 1.0F, CoreConstants.PIPE_MAX_POS);
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);

		block.setBlockBounds(0.0F, 0.0F, 0.0F, 1, 4F / 16F, 1);
		renderblocks.setRenderBoundsFromBlock(block);
		renderBlockInInv(renderblocks, block, 0);

		block.setBlockBounds(5F / 16F, 4F / 16F, 5F / 16F, 11F / 16F, 13F / 16F, 11F / 16F);
		renderblocks.setRenderBoundsFromBlock(block);
		renderBlockInInv(renderblocks, block, 1);

		GL11.glTranslatef(0.5F, 0.5F, 0.5F);
		block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
	}

	private void renderBlockInInv(RenderBlocks renderblocks, Block block, int i) {
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, -1F, 0.0F);
		renderblocks.renderFaceYNeg(block, 0.0D, 0.0D, 0.0D, block.getIcon(0, i));
		tessellator.draw();

		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 1.0F, 0.0F);
		renderblocks.renderFaceYPos(block, 0.0D, 0.0D, 0.0D, block.getIcon(1, i));
		tessellator.draw();

		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 0.0F, -1F);
		renderblocks.renderFaceZNeg(block, 0.0D, 0.0D, 0.0D, block.getIcon(2, i));
		tessellator.draw();

		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 0.0F, 1.0F);
		renderblocks.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, block.getIcon(3, i));
		tessellator.draw();

		tessellator.startDrawingQuads();
		tessellator.setNormal(-1F, 0.0F, 0.0F);
		renderblocks.renderFaceXNeg(block, 0.0D, 0.0D, 0.0D, block.getIcon(4, i));
		tessellator.draw();

		tessellator.startDrawingQuads();
		tessellator.setNormal(1.0F, 0.0F, 0.0F);
		renderblocks.renderFaceXPos(block, 0.0D, 0.0D, 0.0D, block.getIcon(5, i));
		tessellator.draw();
	}

	/*
	 * @Override public GuiScreen handleGUI(int i) { switch (Utils.intToPacketId(i)) { case AssemblyTableGUI: return new GuiAssemblyTable(
	 * ModLoader.getMinecraftInstance().thePlayer.inventory, new TileAssemblyTable()); default: return null; } }
	 */

	/*
	 * @Override public void handlePacket(Packet230ModLoader packet) { switch (PacketIds.values()[packet.packetType]) { case AssemblyTableSelect: GuiScreen
	 * screen = ModLoader.getMinecraftInstance().currentScreen;
	 *
	 * if (screen instanceof GuiAssemblyTable) { GuiAssemblyTable gui = (GuiAssemblyTable) screen; SelectionMessage message = new SelectionMessage();
	 *
	 * TileAssemblyTable.selectionMessageWrapper.updateFromPacket(message, packet);
	 *
	 * gui.handleSelectionMessage (message); }
	 *
	 * break; } }
	 */

}
