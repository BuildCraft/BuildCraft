/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import buildcraft.core.DefaultProps;

public abstract class AdvancedSlot {

	private static final ResourceLocation TEXTURE_SLOT = new ResourceLocation(
			"buildcraft", DefaultProps.TEXTURE_PATH_GUI + "/slot.png");

	public int x, y;
	public GuiAdvancedInterface gui;
	public boolean drawBackround = false;

	public AdvancedSlot(GuiAdvancedInterface gui, int x, int y) {
		this.x = x;
		this.y = y;
		this.gui = gui;
	}

	public String getDescription() {
		return null;
	}

	public final void drawTooltip(GuiAdvancedInterface gui, int x, int y) {
		String s = getDescription();

		if (s != null) {
			gui.drawTooltip(s, x, y);
		} else {
			ItemStack stack = getItemStack();

			if (stack != null) {
				int cornerX = (gui.width - gui.getXSize()) / 2;
				int cornerY = (gui.height - gui.getYSize()) / 2;

				int xS = x - cornerX;
				int yS = y - cornerY;

				gui.renderToolTip(stack, xS, yS);
			}
		}
	}

	public IIcon getIcon() {
		return null;
	}

	public ResourceLocation getTexture() {
		return TextureMap.locationItemsTexture;
	}

	public ItemStack getItemStack() {
		return null;
	}

	public boolean isDefined() {
		return true;
	}

	public void drawSprite(int cornerX, int cornerY) {
		Minecraft mc = Minecraft.getMinecraft();

		if (drawBackround) {
			mc.renderEngine.bindTexture(TEXTURE_SLOT);
			gui.drawTexturedModalRect(cornerX + x - 1, cornerY + y - 1, 0, 0, 18, 18);
		}

		if (!isDefined()) {
			return;
		}

		if (getItemStack() != null) {
			drawStack(getItemStack());
		} else if (getIcon() != null) {
			mc.renderEngine.bindTexture(getTexture());
			//System.out.printf("Drawing advanced sprite %s (%d,%d) at %d %d\n", getIcon().getIconName(), getIcon().getOriginX(),getIcon().getOriginY(),cornerX + x, cornerY + y);
			gui.drawTexturedModelRectFromIcon(cornerX + x, cornerY + y, getIcon(), 16, 16);
		}

	}

	public void drawStack(ItemStack item) {
		int cornerX = (gui.width - gui.getXSize()) / 2;
		int cornerY = (gui.height - gui.getYSize()) / 2;

		gui.drawStack(item, cornerX + x, cornerY + y);
	}

	public void selected () {

	}
}