package buildcraft.factory.gui;

import buildcraft.factory.TileRefineryController;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;

public class GuiRefineryController extends GuiContainer {

	private final EntityPlayer player;

	private final TileRefineryController tile;

	public GuiRefineryController(EntityPlayer player, TileRefineryController tile) {
		super(new ContainerRefineryController(player, tile));
		this.player = player;
		this.tile = tile;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int x, int y) {
		fontRendererObj.drawString("Hello!", 5, 5, 0xFF0000);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {

	}

}
