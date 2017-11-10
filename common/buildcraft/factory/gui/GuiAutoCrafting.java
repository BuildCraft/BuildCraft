/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory.gui;

import org.lwjgl.opengl.GL11;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import buildcraft.core.lib.gui.GuiBuildCraft;
import buildcraft.core.lib.utils.StringUtils;
import buildcraft.factory.TileAutoWorkbench;

public class GuiAutoCrafting extends GuiBuildCraft {

	public static final ResourceLocation TEXTURE = new ResourceLocation("buildcraftfactory:textures/gui/autobench.png");
	private TileAutoWorkbench bench;

	public GuiAutoCrafting(InventoryPlayer inventoryplayer, World world, TileAutoWorkbench tile) {
		super(new ContainerAutoWorkbench(inventoryplayer, tile), tile, TEXTURE);
		this.bench = tile;
		xSize = 176;
		ySize = 197;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		String title = StringUtils.localize("tile.autoWorkbenchBlock.name");
		fontRendererObj.drawString(title, getCenteredOffset(title), 6, 0x404040);
		fontRendererObj.drawString(StringUtils.localize("gui.inventory"), 8, (ySize - 96) + 2, 0x404040);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(TEXTURE);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		if (bench.progress > 0) {
			int progress = bench.getProgressScaled(23);
			drawTexturedModalRect(guiLeft + 89, guiTop + 45, 176, 0, progress + 1, 12);
		}
	}
}
