package buildcraft.transport.gui;

import org.lwjgl.opengl.GL11;
import buildcraft.core.DefaultProps;
import buildcraft.core.utils.StringUtils;
import buildcraft.transport.TileFilteredBuffer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;

public class GuiFilteredBuffer extends GuiContainer {

	IInventory playerInventory;
	TileFilteredBuffer filteredBuffer;

	public GuiFilteredBuffer(InventoryPlayer playerInventory, TileFilteredBuffer filteredBuffer) {
		super(new ContainerFilteredBuffer(playerInventory, filteredBuffer));

		this.playerInventory = playerInventory;
		this.filteredBuffer = filteredBuffer;
		xSize = 175;
		ySize = 169;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		mc.renderEngine.bindTexture(DefaultProps.TEXTURE_PATH_GUI + "/filteredBuffer_gui.png");
		int cornerX = (width - xSize) / 2;
		int cornerY = (height - ySize) / 2;

		drawTexturedModalRect(cornerX, cornerY, 0, 0, xSize, ySize);
		
		IInventory filters = filteredBuffer.getFilters();

		for (int col = 0; col < filters.getSizeInventory(); col++) {
			if(filters.getStackInSlot(col) == null) {
				drawTexturedModalRect(cornerX + 7 + col * 18, cornerY + 60, 176, 0, 18, 18);
			}
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		String title = StringUtils.localize("tile.filteredBufferBlock");
		int xPos = (xSize - fontRenderer.getStringWidth(title)) / 2;
		fontRenderer.drawString(title, xPos, 10, 0x404040);
	}
}
