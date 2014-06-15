/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.gui;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public abstract class GuiAdvancedInterface extends GuiBuildCraft {

	public AdvancedSlot[] slots;

	public GuiAdvancedInterface(BuildCraftContainer container, IInventory inventory, ResourceLocation texture) {
		super(container, inventory, texture);
	}

	public int getSlotAtLocation(int i, int j) {
		for (int position = 0; position < slots.length; ++position) {
			AdvancedSlot s = slots[position];
			if (i >= s.x && i <= s.x + 16 && j >= s.y && j <= s.y + 16) {
				return position;
			}
		}
		return -1;
	}

	protected void drawBackgroundSlots() {
		int cornerX = (width - xSize) / 2;
		int cornerY = (height - ySize) / 2;

		RenderHelper.enableGUIStandardItemLighting();
		GL11.glPushMatrix();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glEnable(32826 /* GL_RESCALE_NORMAL_EXT */);
		int i1 = 240;
		int k1 = 240;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, i1 / 1.0F, k1 / 1.0F);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		for (AdvancedSlot slot : slots) {
			if (slot != null) {
				slot.drawSprite(cornerX, cornerY);
			}
		}

		GL11.glPopMatrix();
	}

	public void drawTooltipForSlotAt(int mouseX, int mouseY) {
		int cornerX = (width - xSize) / 2;
		int cornerY = (height - ySize) / 2;

		int position = getSlotAtLocation(mouseX - cornerX, mouseY - cornerY);

		if (position != -1) {
			AdvancedSlot slot = slots[position];

			if (slot != null) {
				slot.drawTooltip(this, mouseX, mouseY);
			}
		}
	}

	public void drawTooltip(String caption, int mouseX, int mouseY) {
		int cornerX = (width - xSize) / 2;
		int cornerY = (height - ySize) / 2;

		if (caption.length() > 0) {
			int i2 = mouseX - cornerX;
			int k2 = mouseY - cornerY;
			drawCreativeTabHoveringText(caption, i2, k2);
			RenderHelper.enableGUIStandardItemLighting();
		}
	}

	public static RenderItem getItemRenderer () {
		return itemRender;
	}

    public int getXSize () {
    	return xSize;
    }

    public int getYSize () {
    	return ySize;
    }

    @Override
	public void renderToolTip(ItemStack stack, int x, int y) {
    	super.renderToolTip(stack, x, y);
    }
}
