package buildcraft.core.gui.buttons;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import org.lwjgl.opengl.GL11;

/**
 *
 * @author CovertJaguar <railcraft.wikispaces.com>
 */
@SideOnly(Side.CLIENT)
public class GuiButtonSmall extends GuiBetterButton {

	public GuiButtonSmall(int i, int x, int y, String s) {
		this(i, x, y, 200, s);
	}

	public GuiButtonSmall(int i, int x, int y, int w, String s) {
		super(i, x, y, w, 15, s);
	}

	@Override
	public void drawButton(Minecraft minecraft, int i, int j) {
		if (!drawButton) {
			return;
		}
		FontRenderer fontrenderer = minecraft.fontRenderer;
		minecraft.renderEngine.bindTexture(BUTTON_TEXTURES);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		boolean flag = i >= xPosition && j >= yPosition && i < xPosition + width && j < yPosition + height;
		int k = getHoverState(flag);
		drawTexturedModalRect(xPosition, yPosition, 0, 168 + k * 15, width / 2, height);
		drawTexturedModalRect(xPosition + width / 2, yPosition, 200 - width / 2, 168 + k * 15, width / 2, height);
		mouseDragged(minecraft, i, j);
		if (!enabled) {
			drawCenteredString(fontrenderer, displayString, xPosition + width / 2, yPosition + (height - 8) / 2, 0xffa0a0a0);
		} else if (flag) {
			drawCenteredString(fontrenderer, displayString, xPosition + width / 2, yPosition + (height - 8) / 2, 0xffffa0);
		} else {
			drawCenteredString(fontrenderer, displayString, xPosition + width / 2, yPosition + (height - 8) / 2, 0xe0e0e0);
		}
	}
}
