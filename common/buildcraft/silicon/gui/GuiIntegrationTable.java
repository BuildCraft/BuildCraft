/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon.gui;

import buildcraft.core.DefaultProps;
import buildcraft.silicon.TileIntegrationTable;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GuiIntegrationTable extends GuiLaserTable {

	public static final ResourceLocation TEXTURE = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_GUI + "/integration_table.png");
	private static final int FLASH_DELAY = 3;
	private final TileIntegrationTable integrationTable;
	private boolean flash;
	private int flashDelay;

	public GuiIntegrationTable(InventoryPlayer playerInventory, TileIntegrationTable table) {
		super(playerInventory, new ContainerIntegrationTable(playerInventory, table), table, TEXTURE);
		this.integrationTable = table;
		xSize = 175;
		ySize = 166;
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
	protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
		super.drawGuiContainerBackgroundLayer(f, mouseX, mouseY);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(TEXTURE);
		int cornerX = (width - xSize) / 2;
		int cornerY = (height - ySize) / 2;
		if (integrationTable.getEnergy() > 0) {
			if (flash)
				drawTexturedModalRect(cornerX + 13, cornerY + 40, 0, 166, 98, 24);
			int progress = integrationTable.getProgressScaled(98);
			drawTexturedModalRect(cornerX + 13, cornerY + 40, 0, flash ? 190 : 214, progress, 24);
		}
	}
}
