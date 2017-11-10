/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.gui;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import io.netty.buffer.ByteBuf;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import buildcraft.BuildCraftCore;
import buildcraft.builders.TileArchitect;
import buildcraft.core.DefaultProps;
import buildcraft.core.blueprints.BlueprintReadConfiguration;
import buildcraft.core.lib.gui.GuiBuildCraft;
import buildcraft.core.lib.gui.buttons.GuiBetterButton;
import buildcraft.core.lib.gui.tooltips.ToolTip;
import buildcraft.core.lib.gui.tooltips.ToolTipLine;
import buildcraft.core.lib.network.command.CommandWriter;
import buildcraft.core.lib.network.command.PacketCommand;
import buildcraft.core.lib.utils.NetworkUtils;
import buildcraft.core.lib.utils.StringUtils;

public class GuiArchitect extends GuiBuildCraft {

	private static final int TEXT_X = 90;
	private static final int TEXT_Y = 62;
	private static final int TEXT_WIDTH = 156;
	private static final int TEXT_HEIGHT = 12;

	private static final ResourceLocation TEXTURE = new ResourceLocation(
			"buildcraftbuilders:textures/gui/architect_gui.png");

	private TileArchitect architect;

	private GuiBetterButton optionRotate;
	private GuiBetterButton optionExcavate;
	private GuiBetterButton optionAllowCreative;

	private GuiTextField textField;

	public GuiArchitect(EntityPlayer player, TileArchitect architect) {
		super(new ContainerArchitect(player, architect), architect, TEXTURE);
		this.architect = architect;
		xSize = 256;
		ySize = 166;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();

		Keyboard.enableRepeatEvents(true);

		optionRotate = new GuiBetterButton(0, guiLeft + 5, guiTop + 30, 79, "");
		buttonList.add(optionRotate);

		optionExcavate = new GuiBetterButton(1, guiLeft + 5, guiTop + 55, 79, "");
		buttonList.add(optionExcavate);

		optionAllowCreative = new GuiBetterButton(2, guiLeft + 5, guiTop + 80, 79, "");
		optionAllowCreative.setToolTip(new ToolTip(500,
				new ToolTipLine(StringUtils.localize("tile.architect.tooltip.allowCreative.1")),
				new ToolTipLine(StringUtils.localize("tile.architect.tooltip.allowCreative.2"))
		));
		buttonList.add(optionAllowCreative);

		textField = new GuiTextField(this.fontRendererObj, TEXT_X, TEXT_Y, TEXT_WIDTH, TEXT_HEIGHT);
		textField.setMaxStringLength(DefaultProps.MAX_NAME_SIZE);
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
		} else if (button == optionExcavate) {
			conf.excavate = !conf.excavate;
		} else if (button == optionAllowCreative) {
			conf.allowCreative = !conf.allowCreative;
		}

		architect.rpcSetConfiguration(conf);

		updateButtons();
	}

	private void updateButtons() {
		BlueprintReadConfiguration conf = architect.readConfiguration;

		if (conf.rotate) {
			optionRotate.displayString = StringUtils.localize("tile.architect.rotate");
		} else {
			optionRotate.displayString = StringUtils.localize("tile.architect.norotate");
		}

		if (conf.excavate) {
			optionExcavate.displayString = StringUtils.localize("tile.architect.excavate");
		} else {
			optionExcavate.displayString = StringUtils.localize("tile.architect.noexcavate");
		}

		if (conf.allowCreative) {
			optionAllowCreative.displayString = StringUtils.localize("tile.architect.allowCreative");
		} else {
			optionAllowCreative.displayString = StringUtils.localize("tile.architect.noallowCreative");
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
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		int i1 = ((ContainerArchitect) container).computingTime;
		drawTexturedModalRect(guiLeft + 159, guiTop + 34, 0, 166, i1 + 1, 16);
	}

	@Override
	protected void mouseClicked(int i, int j, int k) {
		super.mouseClicked(i, j, k);

		textField.mouseClicked(i - guiLeft, j - guiTop, k);
	}

	@Override
	protected void keyTyped(char c, int i) {
		if (textField.isFocused()) {
			if (c == 13 || c == 27) {
				textField.setFocused(false);
			} else {
				textField.textboxKeyTyped(c, i);
				final String text = textField.getText();
				BuildCraftCore.instance.sendToServer(new PacketCommand(architect, "setName", new CommandWriter() {
					public void write(ByteBuf data) {
						NetworkUtils.writeUTF(data, text);
					}
				}));
			}
		} else {
			super.keyTyped(c, i);
		}
	}
}
