/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.builders.gui;

import net.minecraft.inventory.IInventory;

import org.lwjgl.opengl.GL11;

import buildcraft.builders.TileFiller;
import buildcraft.core.DefaultProps;
import buildcraft.core.gui.GuiBuildCraft;
import buildcraft.core.utils.StringUtil;

public class GuiFiller extends GuiBuildCraft {

	IInventory playerInventory;
	TileFiller filler;

	public GuiFiller(IInventory playerInventory, TileFiller filler) {
		super(new ContainerFiller(playerInventory, filler), filler);
		this.playerInventory = playerInventory;
		this.filler = filler;
		xSize = 175;
		ySize = 240;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		String title = StringUtil.localize("tile.fillerBlock");
		fontRenderer.drawString(title, getCenteredOffset(title), 6, 0x404040);
		fontRenderer.drawString(StringUtil.localize("gui.filling.resources"), 8, 74, 0x404040);
		fontRenderer.drawString(StringUtil.localize("gui.inventory"), 8, 142, 0x404040);

		if (filler.currentPattern != null) {
			drawForegroundSelection(filler.currentPattern.getName());
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {

		int i = mc.renderEngine.getTexture(DefaultProps.TEXTURE_PATH_GUI + "/filler.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(i);

		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		if (filler.currentPattern != null) {
			i = mc.renderEngine.getTexture(filler.currentPattern.getTextureFile());
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			mc.renderEngine.bindTexture(i);

			int textureI = filler.currentPattern.getTextureIndex() >> 4;
			int textureJ = filler.currentPattern.getTextureIndex() - textureI * 16;

			drawTexturedModalRect(guiLeft + patternSymbolX, guiTop + patternSymbolY, 16 * textureJ, 16 * textureI, 16, 16);
		}

	}

	public boolean intersectsWith(int i, int j) {

		if (i >= patternSymbolX && i <= patternSymbolX + 16 && j >= patternSymbolY && j <= patternSymbolY + 16)
			return true;

		return false;
	}

	protected void drawForegroundSelection(String tag) {

		if (!intersectsWith(lastX - guiLeft, lastY - guiTop))
			return;

		if (tag.length() > 0) {
			int i2 = (lastX - guiLeft) + 12;
			int k2 = lastY - guiTop - 12;
			int l2 = fontRenderer.getStringWidth(tag);
			drawGradientRect(i2 - 3, k2 - 3, i2 + l2 + 3, k2 + 8 + 3, 0xc0000000, 0xc0000000);
			fontRenderer.drawStringWithShadow(tag, i2, k2, -1);
		}
	}

	@Override
	protected void mouseMovedOrUp(int i, int j, int k) {
		super.mouseMovedOrUp(i, j, k);

		lastX = i;
		lastY = j;
	}

	private int lastX = 0;
	private int lastY = 0;

	public final int patternSymbolX = 125;
	public final int patternSymbolY = 34;
}
