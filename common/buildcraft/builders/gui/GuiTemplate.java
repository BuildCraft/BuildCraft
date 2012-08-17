/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.builders.gui;

import java.util.Date;

import net.minecraft.src.IInventory;

import org.lwjgl.opengl.GL11;

import buildcraft.BuildCraftBuilders;
import buildcraft.builders.TileArchitect;
import buildcraft.core.DefaultProps;
import buildcraft.core.gui.GuiBuildCraft;
import buildcraft.core.utils.StringUtil;

public class GuiTemplate extends GuiBuildCraft {

	IInventory playerInventory;
	TileArchitect template;

	boolean editMode = false;

	public GuiTemplate(IInventory playerInventory, TileArchitect template) {
		super(new ContainerTemplate(playerInventory, template), template);
		this.playerInventory = playerInventory;
		this.template = template;
		xSize = 175;
		ySize = 225;
	}

	@Override
	protected void drawGuiContainerForegroundLayer() {
		fontRenderer.drawString(template.getInvName(), getCenteredOffset(template.getInvName()), 6, 0x404040);
		fontRenderer.drawString(StringUtil.localize("gui.inventory"), 8, ySize - 152, 0x404040);

		if (editMode && ((new Date()).getTime() / 100) % 8 >= 4)
			fontRenderer.drawString(template.name + "|", 51, 62, 0x404040);
		else
			fontRenderer.drawString(template.name, 51, 62, 0x404040);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		int i = mc.renderEngine.getTexture(DefaultProps.TEXTURE_PATH_GUI + "/template_gui.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(i);
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
		int i1 = template.getComputingProgressScaled(24);
		drawTexturedModalRect(j + 79, k + 34, 176, 14, i1 + 1, 16);
	}

	@Override
	protected void mouseClicked(int i, int j, int k) {
		super.mouseClicked(i, j, k);

		int xMin = (width - xSize) / 2;
		int yMin = (height - ySize) / 2;

		int x = i - xMin;
		int y = j - yMin;

		if (editMode)
			editMode = false;
		else if (x >= 50 && y >= 61 && x <= 137 && y <= 139)
			editMode = true;
	}

	@Override
	protected void keyTyped(char c, int i) {
		if (editMode)
			if (c == 13) {
				editMode = false;

				return;
			} else if (c == 8) {
				if (template.name.length() > 0)
					template.name = template.name.substring(0, template.name.length() - 1);

				return;
			} else if (Character.isLetterOrDigit(c) || c == ' ') {
				if (fontRenderer.getStringWidth(template.name + c) <= BuildCraftBuilders.MAX_BLUEPRINTS_NAME_SIZE)
					template.name += c;

				return;
			}

		super.keyTyped(c, i);

	}
}
