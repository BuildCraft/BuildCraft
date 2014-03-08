/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.gui;

import java.util.Date;

import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import buildcraft.builders.TileArchitect;
import buildcraft.core.DefaultProps;
import buildcraft.core.gui.GuiBuildCraft;
import buildcraft.core.network.RPCHandler;
import buildcraft.core.utils.StringUtils;

public class GuiArchitect extends GuiBuildCraft {

	private static final ResourceLocation TEXTURE = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_GUI + "/template_gui.png");
	IInventory playerInventory;
	TileArchitect template;
	boolean editMode = false;

	public GuiArchitect(IInventory playerInventory, TileArchitect template) {
		super(new ContainerArchitect(playerInventory, template), template, TEXTURE);
		this.playerInventory = playerInventory;
		this.template = template;
		xSize = 175;
		ySize = 225;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		String title = StringUtils.localize("tile.architectBlock.name");
		fontRendererObj.drawString(title, getCenteredOffset(title), 6, 0x404040);
		fontRendererObj.drawString(StringUtils.localize("gui.inventory"), 8, ySize - 152, 0x404040);

		if (editMode && ((new Date()).getTime() / 100) % 8 >= 4) {
			fontRendererObj.drawString(template.name + "|", 51, 62, 0x404040);
		} else {
			fontRendererObj.drawString(template.name, 51, 62, 0x404040);
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(TEXTURE);
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

		if (editMode) {
			editMode = false;
		} else if (x >= 50 && y >= 61 && x <= 137 && y <= 139) {
			editMode = true;
		}
	}

	@Override
	protected void keyTyped(char c, int i) {
		if (i != 1 && editMode) {
			if (c == 13) {
				editMode = false;
				return;
			}

			RPCHandler.rpcServer(template, "handleClientInput", c);
		} else {
			super.keyTyped(c, i);
		}
	}
}
