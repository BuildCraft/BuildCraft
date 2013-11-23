/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory.gui;

import buildcraft.core.DefaultProps;
import buildcraft.core.gui.GuiBuildCraft;
import buildcraft.core.utils.StringUtils;
import buildcraft.factory.TileAutoWorkbench;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class GuiAutoCrafting extends GuiBuildCraft {

    public static final ResourceLocation TEXTURE = new ResourceLocation("buildcraft",DefaultProps.TEXTURE_PATH_GUI + "/autobench.png");
	private TileAutoWorkbench bench;

	public GuiAutoCrafting(InventoryPlayer inventoryplayer, World world, TileAutoWorkbench tile) {
		super(new ContainerAutoWorkbench(inventoryplayer, tile), tile, TEXTURE);
		this.bench = tile;
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		if (this.mc.thePlayer != null) {
			inventorySlots.onContainerClosed(mc.thePlayer);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		String title = StringUtils.localize("tile.autoWorkbenchBlock");
		fontRenderer.drawString(title, getCenteredOffset(title), 6, 0x404040);
		fontRenderer.drawString(StringUtils.localize("gui.inventory"), 8, (ySize - 96) + 2, 0x404040);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(TEXTURE);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
		if (bench.progress > 0) {
			int progress = bench.getProgressScaled(23);
			drawTexturedModalRect(x + 89, y + 45, 176, 0, progress + 1, 12);
		}
	}
}
