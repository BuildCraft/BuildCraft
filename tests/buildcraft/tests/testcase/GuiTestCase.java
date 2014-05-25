/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.tests.testcase;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import buildcraft.BuildCraftBuilders;
import buildcraft.core.DefaultProps;
import buildcraft.core.network.RPCHandler;

public class GuiTestCase extends GuiContainer {

	private static final int TEXT_X = 10;
	private static final int TEXT_Y = 10;
	private static final int TEXT_WIDTH = 156;
	private static final int TEXT_HEIGHT = 12;

	private static final ResourceLocation TEXTURE = new ResourceLocation(
			"buildcraft", DefaultProps.TEXTURE_PATH_GUI + "/tester.png");

	private GuiTextField textField;
	private TileTestCase testCase;

	private GuiButton compress;
	private GuiButton save;

	public GuiTestCase(EntityPlayer player, int x, int y, int z) {
		super(new ContainerTestCase(player, x, y, z));
		xSize = 256;
		ySize = 166;

		testCase = (TileTestCase) player.worldObj.getTileEntity(x, y, z);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;

		Keyboard.enableRepeatEvents(true);

		textField = new GuiTextField(this.fontRendererObj, TEXT_X, TEXT_Y, TEXT_WIDTH, TEXT_HEIGHT);
		textField.setMaxStringLength(BuildCraftBuilders.MAX_BLUEPRINTS_NAME_SIZE);
		textField.setText(testCase.testName);
		textField.setFocused(true);

		compress = new GuiButton(0, x + 5, y + 50, 120, 20, "");
		compress.displayString = "Compress";
		buttonList.add(compress);

		save = new GuiButton(0, x + 5, y + 75, 120, 20, "");
		save.displayString = "Save";
		buttonList.add(save);
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button == compress) {
			RPCHandler.rpcServer(testCase, "compress");
		} else if (button == save) {
			RPCHandler.rpcServer(testCase, "save");
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		fontRendererObj.drawString(testCase.information, 10, 30, 0x404040);
		textField.drawTextBox();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(TEXTURE);
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
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
			}

			RPCHandler.rpcServer(testCase, "setName", textField.getText());
		} else {
			super.keyTyped(c, i);
		}
	}
}
