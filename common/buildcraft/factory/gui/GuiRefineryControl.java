package buildcraft.factory.gui;

import org.lwjgl.opengl.GL11;

import buildcraft.core.DefaultProps;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;

public class GuiRefineryControl extends GuiContainer{
	
	private static final ResourceLocation TEXTURE = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_GUI + "/ledger.png");

	public GuiRefineryControl(Container par1Container) {
		super(par1Container);
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
