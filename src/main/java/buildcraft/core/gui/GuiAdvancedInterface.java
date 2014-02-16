/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.gui;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;

import org.lwjgl.opengl.GL11;

import buildcraft.core.render.RenderUtils;
import buildcraft.core.render.FluidRenderer;

public abstract class GuiAdvancedInterface extends GuiBuildCraft {

	public abstract class AdvancedSlot {

		final public int x, y;

		public AdvancedSlot(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public String getDescription() {
			if (getItemStack() != null)
				return getItemStack().getItem().getItemStackDisplayName(getItemStack());
			else
				return "";
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
			if (!isDefined()) {
				return;
			}

			if (getItemStack() != null) {
				drawStack(getItemStack());
			} else if (getIcon() != null) {
				mc.renderEngine.bindTexture(getTexture());
				//System.out.printf("Drawing advanced sprite %s (%d,%d) at %d %d\n", getIcon().getIconName(), getIcon().getOriginX(),getIcon().getOriginY(),cornerX + x, cornerY + y);
				drawTexturedModelRectFromIcon(cornerX + x, cornerY + y, getIcon(), 16, 16);
			}

		}

		public void drawStack(ItemStack item) {
			if (item != null) {
				int cornerX = (width - xSize) / 2;
				int cornerY = (height - ySize) / 2;

				itemRender.zLevel = 200F;
				itemRender.renderItemAndEffectIntoGUI(fontRendererObj, mc.renderEngine, item, cornerX + x, cornerY + y);
				itemRender.renderItemOverlayIntoGUI(fontRendererObj, mc.renderEngine, item, cornerX + x, cornerY + y);
				itemRender.zLevel = 0.0F;
			}
		}
	}

	public class ItemSlot extends AdvancedSlot {

		public ItemStack stack;

		public ItemSlot(int x, int y) {
			super(x, y);
		}

		@Override
		public ItemStack getItemStack() {
			return stack;
		}
	}

	/**
	 * More dynamic slot displaying an inventory fluid at specified position in
	 * the passed IInventory
	 */
	public class IInventorySlot extends AdvancedSlot {

		private IInventory tile;
		private int slot;

		public IInventorySlot(int x, int y, IInventory tile, int slot) {
			super(x, y);
			this.tile = tile;
			this.slot = slot;
		}

		@Override
		public ItemStack getItemStack() {
			return tile.getStackInSlot(slot);
		}
	}
	public AdvancedSlot[] slots;

	public GuiAdvancedInterface(BuildCraftContainer container, IInventory inventory, ResourceLocation texture) {
		super(container, inventory, texture);
	}

	public int getSlotAtLocation(int i, int j) {
		for (int position = 0; position < slots.length; ++position) {
			AdvancedSlot s = slots[position];
			if (i >= s.x && i <= s.x + 16 && j >= s.y && j <= s.y + 16)
				return position;
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

		for (int s = 0; s < slots.length; ++s) {
			AdvancedSlot slot = slots[s];

			if (slot != null) {
				slot.drawSprite(cornerX, cornerY);
			}
		}

		GL11.glPopMatrix();
	}

	protected void drawForegroundSelection(int mouseX, int mouseY) {
		String s = "";

		int cornerX = (width - xSize) / 2;
		int cornerY = (height - ySize) / 2;

		int position = getSlotAtLocation(mouseX - cornerX, mouseY - cornerY);

		if (position != -1) {
			AdvancedSlot slot = slots[position];

			if (slot != null) {
				s = slot.getDescription();
			}
		}

		if (s.length() > 0) {
			int i2 = (mouseX - cornerX);
			int k2 = mouseY - cornerY;
			drawCreativeTabHoveringText(s, i2, k2);
			RenderHelper.enableGUIStandardItemLighting();
		}
	}

	/**
	 * For the refinery, a kind of phantom slot for fluid.
	 */
	//TODO Get this class working well (Now it's just here to let the refinery compil)
	public class FluidSlot extends AdvancedSlot {

		public Fluid fluid;
		public int colorRenderCache;

		public FluidSlot(int x, int y) {
			super(x, y);
		}

		@Override
		public void drawSprite(int cornerX, int cornerY) {
			if (fluid != null) {
				RenderUtils.setGLColorFromInt(colorRenderCache);
			}
			super.drawSprite(cornerX, cornerY);
		}

		@Override
		public IIcon getIcon() {
			return FluidRenderer.getFluidTexture(fluid, false);
		}

		@Override
		public ResourceLocation getTexture() {
			return FluidRenderer.getFluidSheet(fluid);
		}
	}
}
