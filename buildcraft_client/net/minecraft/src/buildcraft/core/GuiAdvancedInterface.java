package net.minecraft.src.buildcraft.core;

import net.minecraft.src.Container;
import net.minecraft.src.GuiContainer;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.OpenGlHelper;
import net.minecraft.src.RenderHelper;

import org.lwjgl.opengl.GL11;

public abstract class GuiAdvancedInterface extends GuiBuildCraft {

	public abstract class AdvancedSlot {
		final public int x, y;

		public AdvancedSlot(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public String getDescription() {
			if (getItemStack() != null)
				return getItemStack().getItem().getItemDisplayName(getItemStack());
			else
				return "";
		}

		public String getTexture() {
			return "";
		}

		public int getTextureIndex() {
			return 0;
		}

		public ItemStack getItemStack() {
			return null;
		}

		public boolean isDefined() {
			return true;
		}

		public void drawSprite(int cornerX, int cornerY) {
			if (isDefined())
				if (getItemStack() != null)
					drawStack(getItemStack());
				else if (getTexture() != null && !getTexture().equals("")) {
					int texture = mc.renderEngine.getTexture(getTexture());
					mc.renderEngine.bindTexture(texture);

					int textureI = getTextureIndex() >> 4;
					int textureJ = getTextureIndex() - textureI * 16;

					drawTexturedModalRect(cornerX + x, cornerY + y,
							16 * textureJ, 16 * textureI, 16, 16);
				}
		}

		public void drawStack(ItemStack item) {
			if (item != null) {
				int cornerX = (width - xSize) / 2;
				int cornerY = (height - ySize) / 2;

				itemRenderer.zLevel = 200F;
				itemRenderer.renderItemIntoGUI(fontRenderer, mc.renderEngine,
						item, cornerX + x, cornerY + y);
				itemRenderer.renderItemOverlayIntoGUI(fontRenderer,
						mc.renderEngine, item, cornerX + x, cornerY + y);
				itemRenderer.zLevel = 0.0F;
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
	 * More dynamic slot displaying an inventory stack at specified position in the passed IInventory 
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

	public GuiAdvancedInterface(BuildCraftContainer container) {
		super(container);
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
        GL11.glEnable(32826 /*GL_RESCALE_NORMAL_EXT*/);
        int i1 = 240;
        int k1 = 240;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, i1 / 1.0F, k1 / 1.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		for (int s = 0; s < slots.length; ++s) {
			AdvancedSlot slot = slots[s];

			if (slot != null)
				slot.drawSprite(cornerX, cornerY);
		}

		GL11.glPopMatrix();
	}

	protected void drawForegroundSelection() {
		String s = "";

		int cornerX = (width - xSize) / 2;
		int cornerY = (height - ySize) / 2;

		int position = getSlotAtLocation(lastX - cornerX, lastY - cornerY);

		if (position != -1) {
			AdvancedSlot slot = slots[position];

			if (slot != null)
				s = slot.getDescription();
		}

		if (s.length() > 0) {
			int i2 = (lastX - cornerX) + 12;
			int k2 = lastY - cornerY - 12;
			int l2 = fontRenderer.getStringWidth(s);
			drawGradientRect(i2 - 3, k2 - 3, i2 + l2 + 3, k2 + 8 + 3,
					0xc0000000, 0xc0000000);
			fontRenderer.drawStringWithShadow(s, i2, k2, -1);
		}
	}

	private int lastX = 0;
	private int lastY = 0;

	@Override
	protected void mouseMovedOrUp(int i, int j, int k) {
		super.mouseMovedOrUp(i, j, k);

		lastX = i;
		lastY = j;
	}
}
