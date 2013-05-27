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
import buildcraft.transport.pipes.PipeLogicDiamond;

public class GuiDiamondPipe extends GuiBuildCraft {

	IInventory playerInventory;
	PipeLogicDiamond filterInventory;

	public GuiDiamondPipe(IInventory playerInventory, TileGenericPipe tile) {
		super(new ContainerDiamondPipe(playerInventory, (IInventory) tile.pipe.logic), (IInventory) tile.pipe.logic);
		this.playerInventory = playerInventory;
		this.filterInventory = (PipeLogicDiamond) tile.pipe.logic;
		xSize = 175;
		ySize = 225;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		fontRenderer.drawString(filterInventory.getInvName(), getCenteredOffset(filterInventory.getInvName()), 6, 0x404040);
		fontRenderer.drawString(StringUtils.localize("gui.inventory"), 8, ySize - 97, 0x404040);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(DefaultProps.TEXTURE_PATH_GUI + "/filter.png");
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
	}
}
