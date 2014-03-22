package buildcraft.factory.gui;

import org.lwjgl.opengl.GL11;

import buildcraft.core.DefaultProps;
import buildcraft.factory.TileRefineryControl;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;

public class GuiRefineryControl extends GuiContainer{
	
	private static final ResourceLocation TEXTURE = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_GUI + "/ledger.png");

	
	public GuiRefineryControl(InventoryPlayer inventory, TileRefineryControl refineryControl) {
		super(new ContainerRefineryControl(inventory, refineryControl));
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(TEXTURE);
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
	}

}
