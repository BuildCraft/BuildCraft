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

import buildcraft.core.lib.gui.GuiBuildCraft;
import buildcraft.core.lib.utils.StringUtils;
import buildcraft.silicon.TilePackager;

public class GuiPackager extends GuiBuildCraft {

	public static final ResourceLocation TEXTURE = new ResourceLocation("buildcraftsilicon:textures/gui/packager.png");
	private TilePackager bench;

	public GuiPackager(InventoryPlayer inventoryplayer, TilePackager tile) {
		super(new ContainerPackager(inventoryplayer, tile), tile, TEXTURE);
		this.bench = tile;
		xSize = 176;
		ySize = 197;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		fontRendererObj.drawString(StringUtils.localize("gui.inventory"), 8, (ySize - 96) + 2, 0x404040);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(TEXTURE);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 3; x++) {
				if (bench.isPatternSlotSet(y * 3 + x)) {
					drawTexturedModalRect(guiLeft + 29 + x * 18, guiTop + 16 + y * 18, xSize, 0, 18, 18);
				}
			}
		}
	}
}
