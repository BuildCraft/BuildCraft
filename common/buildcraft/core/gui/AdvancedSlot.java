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

public abstract class AdvancedSlot {

	public int x, y;
	public GuiAdvancedInterface gui;

	public AdvancedSlot(GuiAdvancedInterface gui, int x, int y) {
		this.x = x;
		this.y = y;
		this.gui = gui;
	}

	public String getDescription() {
		if (getItemStack() != null) {
			return getItemStack().getItem().getItemStackDisplayName(getItemStack());
		} else {
			return "";
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
		Minecraft mc = Minecraft.getMinecraft();

		if (item != null) {
			int cornerX = (gui.width - gui.getXSize()) / 2;
			int cornerY = (gui.height - gui.getYSize()) / 2;

			GuiAdvancedInterface.getItemRenderer().zLevel = 200F;
			GuiAdvancedInterface.getItemRenderer().renderItemAndEffectIntoGUI(gui.getFontRenderer (), mc.renderEngine, item, cornerX + x, cornerY + y);
			GuiAdvancedInterface.getItemRenderer().renderItemOverlayIntoGUI(gui.getFontRenderer (), mc.renderEngine, item, cornerX + x, cornerY + y);
			GuiAdvancedInterface.getItemRenderer().zLevel = 0.0F;
		}
	}

	public void selected () {

	}
}