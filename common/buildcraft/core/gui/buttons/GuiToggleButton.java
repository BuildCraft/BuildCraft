package buildcraft.core.gui.buttons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import org.lwjgl.opengl.GL11;

/**
 *
 * @author CovertJaguar <railcraft.wikispaces.com>
 */
public class GuiToggleButton extends GuiBetterButton {

	public boolean active;

	public GuiToggleButton(int id, int x, int y, String label, boolean active) {
		this(id, x, y, 200, 20, label, active);
	}

	public GuiToggleButton(int id, int x, int y, int width, String s, boolean active) {
		super(id, x, y, width, 20, s);
		this.active = active;
	}

	public GuiToggleButton(int id, int x, int y, int width, int height, String s, boolean active) {
		super(id, x, y, width, height, s);
		this.active = active;
	}

	public void toggle() {
		active = !active;
	}

	@Override
	protected int getHoverState(boolean flag) {
		int state = 1;
		if (!enabled) {
			state = 0;
		} else if (flag) {
			state = 2;
		} else if (!active) {
			state = 3;
		}
		return state;
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
		drawTexturedModalRect(xPosition, yPosition, 0, 88 + k * 20, width / 2, height);
		drawTexturedModalRect(xPosition + width / 2, yPosition, 200 - width / 2, 88 + k * 20, width / 2, height);
		mouseDragged(minecraft, i, j);
		if (!enabled) {
			drawCenteredString(fontrenderer, displayString, xPosition + width / 2, yPosition + (height - 8) / 2, 0xffa0a0a0);
		} else if (flag) {
			drawCenteredString(fontrenderer, displayString, xPosition + width / 2, yPosition + (height - 8) / 2, 0xffffa0);
		} else if (!active) {
			drawCenteredString(fontrenderer, displayString, xPosition + width / 2, yPosition + (height - 8) / 2, 0x777777);
		} else {
			drawCenteredString(fontrenderer, displayString, xPosition + width / 2, yPosition + (height - 8) / 2, 0xe0e0e0);
		}
	}
}
