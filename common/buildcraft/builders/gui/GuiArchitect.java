/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.gui;

import buildcraft.builders.TileArchitect;
import buildcraft.core.DefaultProps;
import buildcraft.core.blueprints.BlueprintReadConfiguration;
import buildcraft.core.gui.GuiBuildCraft;
import buildcraft.core.network.RPCHandler;
import buildcraft.core.utils.StringUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.Date;

public class GuiArchitect extends GuiBuildCraft {

	private static final ResourceLocation TEXTURE = new ResourceLocation(
			"buildcraft", DefaultProps.TEXTURE_PATH_GUI + "/architect_gui.png");

	IInventory playerInventory;
	TileArchitect architect;
	boolean editMode = false;

	private GuiButton optionRotate;
	private GuiButton optionReadMods;
	private GuiButton optionReadBlocks;
	private GuiButton optionExcavate;

	public GuiArchitect(IInventory playerInventory, TileArchitect architect) {
		super(new ContainerArchitect(playerInventory, architect), architect, TEXTURE);
		this.playerInventory = playerInventory;
		this.architect = architect;
		xSize = 256;
		ySize = 166;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;

		optionRotate = new GuiButton(0, x + 5, y + 30, 77, 20, "");
		buttonList.add(optionRotate);

		optionReadBlocks = new GuiButton(1, x + 5, y + 55, 77, 20, "");
		buttonList.add(optionReadBlocks);

		optionExcavate = new GuiButton(2, x + 5, y + 80, 77, 20, "");
		buttonList.add(optionExcavate);

		updateButtons();
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		BlueprintReadConfiguration conf = architect.readConfiguration;

		if (button == optionRotate) {
			conf.rotate = !conf.rotate;
		} else if (button == optionReadBlocks) {
			conf.readTiles = !conf.readTiles;
		} else if (button == optionExcavate) {
			conf.excavate = !conf.excavate;
		}

		architect.rpcSetConfiguration(conf);

		updateButtons();
	}

	private void updateButtons() {
		BlueprintReadConfiguration conf = architect.readConfiguration;

		if (conf.rotate) {
			optionRotate.displayString = "Rotate: On";
		} else {
			optionRotate.displayString = "Rotate: Off";
		}

		if (conf.readTiles) {
			optionReadBlocks.displayString = "Blocks: All";
		} else {
			optionReadBlocks.displayString = "Blocks: Simple";
		}

		if (conf.excavate) {
			optionExcavate.displayString = "Excavate: On";
		} else {
			optionExcavate.displayString = "Excavate: Off";
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		String title = StringUtils.localize("tile.architectBlock.name");
		String inv = StringUtils.localize("gui.inventory");
		fontRendererObj.drawString(title, getCenteredOffset(title), 6, 0x404040);
		fontRendererObj.drawString(inv, getCenteredOffset(inv) + 40, ySize - 94, 0x404040);

		if (editMode && ((new Date()).getTime() / 100) % 8 >= 4) {
			fontRendererObj.drawString(architect.name + "|", 131, 62, 0x404040);
		} else {
			fontRendererObj.drawString(architect.name, 131, 62, 0x404040);
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(TEXTURE);
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
		int i1 = architect.getComputingProgressScaled(24);
		drawTexturedModalRect(j + 159, k + 34, 0, 166, i1 + 1, 16);
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
		} else if (x >= 130 && y >= 61 && x <= 217 && y <= 139) {
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

			RPCHandler.rpcServer(architect, "handleClientInput", c);
		} else {
			super.keyTyped(c, i);
		}
	}
}
