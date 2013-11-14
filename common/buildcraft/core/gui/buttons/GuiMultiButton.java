package buildcraft.core.gui.buttons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import org.lwjgl.opengl.GL11;

import buildcraft.core.gui.tooltips.ToolTip;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * @author CovertJaguar <railcraft.wikispaces.com>
 */
@SideOnly(Side.CLIENT)
public class GuiMultiButton extends GuiBetterButton {

	private final MultiButtonController control;

	public GuiMultiButton(int id, int x, int y, int width, MultiButtonController control) {
		super(id, x, y, width, StandardButtonTextureSets.LARGE_BUTTON, "");
		this.control = control;
	}

	@Override
	public int getHeight() {
		return texture.getHeight();
	}

	@Override
	public void drawButton(Minecraft minecraft, int x, int y) {
		if (!drawButton) {
			return;
		}
		FontRenderer fontrenderer = minecraft.fontRenderer;
		bindButtonTextures(minecraft);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		IMultiButtonState state = control.getButtonState();
		IButtonTextureSet tex = state.getTextureSet();
		int xOffset = tex.getX();
		int yOffset = tex.getY();
		int h = tex.getHeight();
		int w = tex.getWidth();
		boolean flag = x >= xPosition && y >= yPosition && x < xPosition + width && y < yPosition + h;
		int hoverState = getHoverState(flag);
		drawTexturedModalRect(xPosition, yPosition, xOffset, yOffset + hoverState * h, width / 2, h);
		drawTexturedModalRect(xPosition + width / 2, yPosition, xOffset + w - width / 2, yOffset + hoverState * h, width / 2, h);
		mouseDragged(minecraft, x, y);
		displayString = state.getLabel();
		if (!displayString.equals("")) {
			if (!enabled) {
				drawCenteredString(fontrenderer, displayString, xPosition + width / 2, yPosition + (h - 8) / 2, 0xffa0a0a0);
			} else if (flag) {
				drawCenteredString(fontrenderer, displayString, xPosition + width / 2, yPosition + (h - 8) / 2, 0xffffa0);
			} else {
				drawCenteredString(fontrenderer, displayString, xPosition + width / 2, yPosition + (h - 8) / 2, 0xe0e0e0);
			}
		}
	}

	@Override
	public boolean mousePressed(Minecraft par1Minecraft, int par2, int par3) {
		boolean pressed = super.mousePressed(par1Minecraft, par2, par3);
		if (pressed && enabled) {
			control.incrementState();
		}
		return pressed;
	}

	public MultiButtonController getController() {
		return control;
	}

	@Override
	public ToolTip getToolTip() {
		ToolTip tip = this.control.getButtonState().getToolTip();
		if (tip != null) {
			return tip;
		}
		return super.getToolTip();
	}
}
