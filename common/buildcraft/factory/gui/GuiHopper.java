package buildcraft.factory.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.opengl.GL11;

import buildcraft.core.DefaultProps;
import buildcraft.factory.TileHopper;

public class GuiHopper extends GuiContainer {

	public GuiHopper(InventoryPlayer inventory, TileHopper tile) {
		super(new ContainerHopper(inventory, tile));
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(DefaultProps.TEXTURE_PATH_GUI + "/hopper_gui.png");
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);
	}

}
