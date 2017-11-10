/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.core.lib.gui.buttons;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import buildcraft.core.lib.gui.tooltips.IToolTipProvider;
import buildcraft.core.lib.gui.tooltips.ToolTip;


@SideOnly(Side.CLIENT)
public class GuiImageButton extends GuiButton implements IButtonClickEventTrigger, IToolTipProvider {
	private final int size, u, v, baseU, baseV;
	private final ResourceLocation texture;

	private ArrayList<IButtonClickEventListener> listeners = new ArrayList<IButtonClickEventListener>();
	private boolean active = false;
	private ToolTip toolTip;

	public GuiImageButton(int id, int x, int y, int size, ResourceLocation texture, int u, int v) {
		this(id, x, y, size, texture, 0, 0, u, v);
	}

	public GuiImageButton(int id, int x, int y, int size, ResourceLocation texture, int baseU, int baseV, int u, int v) {
		super(id, x, y, size, size, "");
		this.size = size;
		this.u = u;
		this.v = v;
		this.baseU = baseU;
		this.baseV = baseV;
		this.texture = texture;
	}

	public int getSize() {
		return size;
	}

	public boolean isActive() {
		return active;
	}

	public void activate() {
		active = true;
	}

	public void deActivate() {
		active = false;
	}

	@Override
	public void drawButton(Minecraft minecraft, int x, int y) {
		if (!visible) {
			return;
		}

		minecraft.renderEngine.bindTexture(texture);

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glDisable(GL11.GL_BLEND);

		int buttonState = getButtonState(x, y);

		drawTexturedModalRect(xPosition, yPosition, baseU + buttonState * size, baseV, size, size);
		drawTexturedModalRect(xPosition + 1, yPosition + 1, u, v, size - 2, size - 2);

		mouseDragged(minecraft, x, y);
	}

	@Override
	public boolean mousePressed(Minecraft par1Minecraft, int par2, int par3) {
		boolean pressed = super.mousePressed(par1Minecraft, par2, par3);

		if (pressed) {
			active = !active;
			notifyAllListeners();
		}

		return pressed;
	}

	@Override
	public void registerListener(IButtonClickEventListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(IButtonClickEventListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void notifyAllListeners() {
		for (IButtonClickEventListener listener : listeners) {
			listener.handleButtonClick(this, this.id);
		}
	}

	private int getButtonState(int mouseX, int mouseY) {
		if (!this.enabled) {
			return 0;
		}

		if (isMouseOverButton(mouseX, mouseY)) {
			if (!this.active) {
				return 2;
			} else {
				return 4;
			}
		}

		if (!this.active) {
			return 1;
		} else {
			return 3;
		}
	}

	private boolean isMouseOverButton(int mouseX, int mouseY) {
		return mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + size && mouseY < yPosition + size;
	}

	@Override
	public ToolTip getToolTip() {
		return toolTip;
	}

	public GuiImageButton setToolTip(ToolTip tips) {
		this.toolTip = tips;
		return this;
	}

	@Override
	public boolean isToolTipVisible() {
		return visible;
	}

	@Override
	public boolean isMouseOver(int mouseX, int mouseY) {
		return isMouseOverButton(mouseX, mouseY);
	}
}
