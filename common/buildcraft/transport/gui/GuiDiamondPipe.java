/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.gui;

import net.minecraft.client.resources.ResourceLocation;
import net.minecraft.inventory.IInventory;

import org.lwjgl.opengl.GL11;

import buildcraft.core.DefaultProps;
import buildcraft.core.gui.GuiBuildCraft;
import buildcraft.core.utils.StringUtils;
import buildcraft.transport.pipes.PipeLogicDiamond;

public class GuiDiamondPipe extends GuiBuildCraft {
    public static final ResourceLocation gui = new ResourceLocation("buildcraft",DefaultProps.TEXTURE_PATH_GUI + "/filter.png");
	IInventory playerInventory;
	IInventory filterInventory;

	public GuiDiamondPipe(IInventory playerInventory, PipeLogicDiamond logic) {
		super(new ContainerDiamondPipe(playerInventory, logic), logic.getFilters());
		this.playerInventory = playerInventory;
		this.filterInventory = logic.getFilters();
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
		mc.renderEngine.func_110577_a(gui);
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
	}
}
