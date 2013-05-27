/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.gui;

import net.minecraft.inventory.IInventory;

import org.lwjgl.opengl.GL11;

import buildcraft.core.DefaultProps;
import buildcraft.core.gui.GuiBuildCraft;
import buildcraft.core.utils.StringUtils;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.pipes.PipeItemsEmerald;

public class GuiEmeraldPipe extends GuiBuildCraft {

	IInventory playerInventory;
	PipeItemsEmerald filterInventory;

	public GuiEmeraldPipe(IInventory playerInventory, TileGenericPipe tile) {
		super(new ContainerEmeraldPipe(playerInventory, (IInventory) tile.pipe), (IInventory) tile.pipe);
		this.playerInventory = playerInventory;
		this.filterInventory = (PipeItemsEmerald) tile.pipe;
		xSize = 175;
		ySize = 132;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		fontRenderer.drawString(filterInventory.getInvName(), getCenteredOffset(filterInventory.getInvName()), 6, 0x404040);
		fontRenderer.drawString(StringUtils.localize("gui.inventory"), 8, ySize - 93, 0x404040);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(DefaultProps.TEXTURE_PATH_GUI + "/filter_2.png");
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
	}
}
