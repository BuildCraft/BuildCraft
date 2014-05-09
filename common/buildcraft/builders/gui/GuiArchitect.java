/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.gui;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

import buildcraft.BuildCraftBuilders;
import buildcraft.builders.TileArchitect;
import buildcraft.core.DefaultProps;
import buildcraft.core.blueprints.BlueprintReadConfiguration;
import buildcraft.core.gui.GuiBuildCraft;
import buildcraft.core.network.RPCHandler;
import buildcraft.core.utils.StringUtils;

public class GuiArchitect extends GuiBuildCraft {

	private static final int TEXT_X = 90;
	private static final int TEXT_Y = 62;
	private static final int TEXT_WIDTH = 156;
	private static final int TEXT_HEIGHT = 12;

	private static final ResourceLocation TEXTURE = new ResourceLocation(
			"buildcraft", DefaultProps.TEXTURE_PATH_GUI + "/architect_gui.png");

	private IInventory playerInventory;
	private TileArchitect architect;

	private GuiButton optionRotate;
	private GuiButton optionReadMods;
	private GuiButton optionReadBlocks;
	private GuiButton optionExcavate;
	private GuiButton optionExplicit;

	private GuiTextField textField;

	public GuiArchitect(EntityPlayer player, TileArchitect architect) {
		super(new ContainerArchitect(player, architect), architect, TEXTURE);
		this.playerInventory = player.inventory;
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

		Keyboard.enableRepeatEvents(true);

		optionRotate = new GuiButton(0, x + 5, y + 30, 77, 20, "");
		buttonList.add(optionRotate);

		optionReadBlocks = new GuiButton(1, x + 5, y + 55, 77, 20, "");
		buttonList.add(optionReadBlocks);

		optionExcavate = new GuiButton(2, x + 5, y + 80, 77, 20, "");
		buttonList.add(optionExcavate);

		optionExplicit = new GuiButton(3, x + 5, y + 105, 77, 20, "");
		buttonList.add(optionExplicit);

		textField = new GuiTextField(this.fontRendererObj, TEXT_X, TEXT_Y, TEXT_WIDTH, TEXT_HEIGHT);
		textField.setMaxStringLength(BuildCraftBuilders.MAX_BLUEPRINTS_NAME_SIZE);
		textField.setText(architect.name);
		textField.setFocused(true);

		updateButtons();
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
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
		} else if (button == optionExplicit) {
			conf.explicitOnly = !conf.explicitOnly;
		}

		architect.rpcSetConfiguration(conf);

		updateButtons();
	}

	private void updateButtons () {
		BlueprintReadConfiguration conf = architect.readConfiguration;

		if (conf.rotate) {
			optionRotate.displayString = StringUtils.localize("tile.architect.rotate");
		} else {
			optionRotate.displayString =  StringUtils.localize("tile.architect.norotate");
		}

		if (conf.readTiles) {
			optionReadBlocks.displayString = StringUtils.localize("tile.architect.allblocks");
		} else {
			optionReadBlocks.displayString = StringUtils.localize("tile.architect.simpleblocks");
		}

		if (conf.excavate) {
			optionExcavate.displayString = StringUtils.localize("tile.architect.excavate");
		} else {
			optionExcavate.displayString =  StringUtils.localize("tile.architect.noexcavate");
		}

		if (conf.explicitOnly) {
			optionExplicit.displayString =  StringUtils.localize("tile.architect.supportmods");
		} else {
			optionExplicit.displayString =  StringUtils.localize("tile.architect.allmods");
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		String title = StringUtils.localize("tile.architectBlock.name");
		fontRendererObj.drawString(title, getCenteredOffset(title), 6, 0x404040);

		/*
		 * if (editMode && ((new Date()).getTime() / 100) % 8 >= 4) {
		 * fontRendererObj.drawString(architect.name + "|", 131, 62, 0x404040);
		 * } else { fontRendererObj.drawString(architect.name, 131, 62,
		 * 0x404040); }
		 */

		textField.drawTextBox();
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

		if (x >= TEXT_X && y >= TEXT_Y
				&& x <= TEXT_X + TEXT_WIDTH && y <= TEXT_Y + TEXT_HEIGHT) {
			textField.setFocused(true);
		} else {
			textField.setFocused(false);
		}
	}

	@Override
	protected void keyTyped(char c, int i) {
		if (textField.isFocused()) {
			if (c == 13 || c == 27) {
				textField.setFocused(false);
			} else {
				textField.textboxKeyTyped(c, i);
				RPCHandler.rpcServer(architect, "handleClientSetName", textField.getText());
			}
		} else {
			super.keyTyped(c, i);
		}
	}
}
