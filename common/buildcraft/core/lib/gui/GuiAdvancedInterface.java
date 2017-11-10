/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.gui;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public abstract class GuiAdvancedInterface extends GuiBuildCraft {

	public ArrayList<AdvancedSlot> slots = new ArrayList<AdvancedSlot>();

	public GuiAdvancedInterface(BuildCraftContainer container, IInventory inventory, ResourceLocation texture) {
		super(container, inventory, texture);
	}

	public int getSlotIndexAtLocation(int i, int j) {
		int x = i - guiLeft;
		int y = j - guiTop;

		for (int position = 0; position < slots.size(); ++position) {
			AdvancedSlot s = slots.get(position);

			if (s != null && x >= s.x && x <= s.x + 16 && y >= s.y && y <= s.y + 16) {
				return position;
			}
		}
		return -1;
	}

	public AdvancedSlot getSlotAtLocation(int i, int j) {
		int id = getSlotIndexAtLocation(i, j);

		if (id != -1) {
			return slots.get(id);
		} else {
			return null;
		}
	}

	private boolean isMouseOverSlot(AdvancedSlot slot, int mouseX, int mouseY) {
		int realMouseX = mouseX - this.guiLeft;
		int realMouseY = mouseY - this.guiTop;
		return realMouseX >= slot.x - 1 && realMouseX < slot.x + 16 + 1 && realMouseY >= slot.y - 1 && realMouseY < slot.y + 16 + 1;
	}

	protected void drawSlotHighlight(AdvancedSlot slot, int mouseX, int mouseY) {
		if (this.isMouseOverSlot(slot, mouseX, mouseY) && slot.shouldDrawHighlight()) {
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glColorMask(true, true, true, false);
			this.drawGradientRect(guiLeft + slot.x, guiTop + slot.y, guiLeft + slot.x + 16, guiTop + slot.y + 16, -2130706433, -2130706433);
			GL11.glColorMask(true, true, true, true);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
		}
	}

	protected void drawBackgroundSlots(int mouseX, int mouseY) {
		RenderHelper.enableGUIStandardItemLighting();
		GL11.glPushMatrix();
		GL11.glPushAttrib(GL11.GL_TRANSFORM_BIT);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		int i1 = 240;
		int k1 = 240;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, i1 / 1.0F, k1 / 1.0F);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		if (slots != null) {
			for (AdvancedSlot slot : slots) {
				if (slot != null) {
					slot.drawSprite(guiLeft, guiTop);
					drawSlotHighlight(slot, mouseX, mouseY);
				}
			}
		}

		GL11.glPopAttrib();
		GL11.glPopMatrix();
	}

	public void drawTooltipForSlotAt(int mouseX, int mouseY) {
		AdvancedSlot slot = getSlotAtLocation(mouseX, mouseY);

		if (slot != null) {
			slot.drawTooltip(this, mouseX, mouseY);
			RenderHelper.enableGUIStandardItemLighting();
		}
	}

	public void drawTooltip(String caption, int mouseX, int mouseY) {
		if (caption.length() > 0) {
			int i2 = mouseX - guiLeft;
			int k2 = mouseY - guiTop;
			drawCreativeTabHoveringText(caption, i2, k2);
			RenderHelper.enableGUIStandardItemLighting();
		}
	}

	public static RenderItem getItemRenderer() {
		return itemRender;
	}

	public int getXSize() {
		return xSize;
	}

	public int getYSize() {
		return ySize;
	}

	@Override
	public void renderToolTip(ItemStack stack, int x, int y) {
		super.renderToolTip(stack, x, y);
	}

	public void drawStack(ItemStack item, int x, int y) {
		Minecraft mc = Minecraft.getMinecraft();

		if (item != null) {
			GL11.glEnable(GL11.GL_LIGHTING);
			float prevZ = GuiAdvancedInterface.getItemRenderer().zLevel;
			GuiAdvancedInterface.getItemRenderer().zLevel = 200F;
			GuiAdvancedInterface.getItemRenderer().renderItemAndEffectIntoGUI(getFontRenderer(), mc.renderEngine, item, x, y);
			GuiAdvancedInterface.getItemRenderer().renderItemOverlayIntoGUI(getFontRenderer(), mc.renderEngine, item, x, y);
			GuiAdvancedInterface.getItemRenderer().zLevel = prevZ;
			GL11.glDisable(GL11.GL_LIGHTING);
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		super.mouseClicked(mouseX, mouseY, mouseButton);

		AdvancedSlot slot = getSlotAtLocation(mouseX, mouseY);

		if (slot != null && slot.isDefined()) {
			slotClicked(slot, mouseButton);
		}
	}

	public void resetNullSlots(int size) {
		slots.clear();

		for (int i = 0; i < size; ++i) {
			slots.add(null);
		}
	}

	// TODO: Use this for all children of this class
	protected void slotClicked(AdvancedSlot slot, int mouseButton) {

	}
}
