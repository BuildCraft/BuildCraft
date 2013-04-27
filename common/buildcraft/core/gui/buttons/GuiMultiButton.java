package buildcraft.core.gui.buttons;

import static buildcraft.core.gui.buttons.GuiBetterButton.BUTTON_TEXTURES;
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
public class GuiMultiButton extends GuiBetterButton {

	private final MultiButtonController control;
	protected int texOffset = 88;

	public GuiMultiButton(int id, int x, int y, int width, MultiButtonController control) {
		super(id, x, y, width, 20, "");
		this.control = control.copy();
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
		int hoverState = getHoverState(flag);
		drawTexturedModalRect(xPosition, yPosition, 0, texOffset + hoverState * height, width / 2, height);
		drawTexturedModalRect(xPosition + width / 2, yPosition, 200 - width / 2, texOffset + hoverState * height, width / 2, height);
		mouseDragged(minecraft, i, j);
		displayString = control.getButtonState().getLabel();
		if (!enabled) {
			drawCenteredString(fontrenderer, displayString, xPosition + width / 2, yPosition + (height - 8) / 2, 0xffa0a0a0);
		} else if (flag) {
			drawCenteredString(fontrenderer, displayString, xPosition + width / 2, yPosition + (height - 8) / 2, 0xffffa0);
		} else {
			drawCenteredString(fontrenderer, displayString, xPosition + width / 2, yPosition + (height - 8) / 2, 0xe0e0e0);
		}
	}

	@Override
	public boolean mousePressed(Minecraft par1Minecraft, int par2, int par3) {
		boolean pressed = super.mousePressed(par1Minecraft, par2, par3);
		if (pressed) {
			control.incrementState();
		}
		return pressed;
	}

	public MultiButtonController getController() {
		return control;
	}
}
