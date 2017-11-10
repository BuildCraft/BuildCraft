/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon.gui;

import org.lwjgl.opengl.GL11;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import buildcraft.silicon.TileIntegrationTable;

public class GuiIntegrationTable extends GuiLaserTable {

	public static final ResourceLocation TEXTURE = new ResourceLocation("buildcraftsilicon:textures/gui/integration_table.png");
	private static final int FLASH_DELAY = 3;
	private final TileIntegrationTable integrationTable;
	private boolean flash;
	private int flashDelay;

	public GuiIntegrationTable(InventoryPlayer playerInventory, TileIntegrationTable table) {
		super(playerInventory, new ContainerIntegrationTable(playerInventory, table), table, TEXTURE);
		this.integrationTable = table;
		xSize = 176;
		ySize = 186;
	}

	@Override
	public void updateScreen() {
		super.updateScreen();

		if (flashDelay <= 0) {
			flashDelay = FLASH_DELAY;
			flash = !flash;
		} else {
			flashDelay--;
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		drawLedgers(par1, par2);

		String title = table.getInventoryName();
		fontRendererObj.drawString(title, getCenteredOffset(title), 6, 0x404040);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
		super.drawGuiContainerBackgroundLayer(f, mouseX, mouseY);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(TEXTURE);
		if (integrationTable.getEnergy() > 0) {
			int h = table.getProgressScaled(69);
			drawTexturedModalRect(guiLeft + 164, guiTop + 18 + 74 - h, 176, 18, 4, h);
		}
		if (integrationTable.getMaxExpansionCount() > 0) {
			for (int i = 8; i > integrationTable.getMaxExpansionCount(); i--) {
				drawTexturedModalRect(guiLeft + ContainerIntegrationTable.SLOT_X[i] - 1, guiTop + ContainerIntegrationTable.SLOT_Y[i] - 1,
						180, 17, 18, 18);
			}
		}
	}
}
