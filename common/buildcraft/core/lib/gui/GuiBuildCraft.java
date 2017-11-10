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
import java.util.Collection;
import java.util.Date;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fluids.FluidStack;

import buildcraft.core.lib.gui.slots.IPhantomSlot;
import buildcraft.core.lib.gui.tooltips.IToolTipProvider;
import buildcraft.core.lib.gui.tooltips.ToolTip;
import buildcraft.core.lib.gui.tooltips.ToolTipLine;
import buildcraft.core.lib.gui.widgets.Widget;
import buildcraft.core.lib.render.RenderUtils;
import buildcraft.core.lib.utils.SessionVars;

public abstract class GuiBuildCraft extends GuiContainer {

	public static final ResourceLocation LEDGER_TEXTURE = new ResourceLocation("buildcraftcore:textures/gui/ledger.png");
	public final LedgerManager ledgerManager = new LedgerManager(this);
	public final TileEntity tile;
	public final BuildCraftContainer container;
	public ResourceLocation texture;

	public GuiBuildCraft(BuildCraftContainer container, IInventory inventory, ResourceLocation texture) {
		super(container);
		this.container = container;

		this.texture = texture;

		if (inventory instanceof TileEntity) {
			tile = (TileEntity) inventory;
		} else {
			tile = null;
		}

		initLedgers(inventory);
	}

	public FontRenderer getFontRenderer() {
		return fontRendererObj;
	}

	protected void initLedgers(IInventory inventory) {
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	@Override
	public void drawScreen(int mouseX, int mouseY, float par3) {
		super.drawScreen(mouseX, mouseY, par3);
		int left = this.guiLeft;
		int top = this.guiTop;

		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glPushMatrix();
		GL11.glTranslatef(left, top, 0.0F);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderHelper.disableStandardItemLighting();

		InventoryPlayer playerInv = this.mc.thePlayer.inventory;

		if (playerInv.getItemStack() == null) {
			drawToolTips(container.getWidgets(), mouseX - left, mouseY - top, left, top);
			drawToolTips(buttonList, mouseX, mouseY, 0, 0);
			drawToolTips(inventorySlots.inventorySlots, mouseX, mouseY, 0, 0);
		}

		GL11.glPopMatrix();
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}

	private void drawToolTips(Collection<?> objects, int mouseX, int mouseY, int offsetX, int offsetY) {
		for (Object obj : objects) {
			if (!(obj instanceof IToolTipProvider)) {
				continue;
			}
			IToolTipProvider provider = (IToolTipProvider) obj;
			if (!provider.isToolTipVisible()) {
				continue;
			}
			ToolTip tips = provider.getToolTip();
			if (tips == null) {
				continue;
			}
			boolean mouseOver = provider.isMouseOver(mouseX, mouseY);
			tips.onTick(mouseOver);
			if (mouseOver && tips.isReady()) {
				tips.refresh();
				drawToolTips(tips, mouseX + offsetX, mouseY + offsetY);
			}
		}
	}

	public void drawFluid(FluidStack fluid, int x, int y, int width, int height, int maxCapacity) {
		if (fluid == null || fluid.getFluid() == null) {
			return;
		}
		IIcon icon = fluid.getFluid().getIcon(fluid);

		if (icon == null) {
			icon = ((TextureMap) Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.locationBlocksTexture)).getAtlasSprite("missingno");
		}

		mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
		RenderUtils.setGLColorFromInt(fluid.getFluid().getColor(fluid));
		int fullX = width / 16;
		int fullY = height / 16;
		int lastX = width - fullX * 16;
		int lastY = height - fullY * 16;
		int level = fluid.amount * height / maxCapacity;
		int fullLvl = (height - level) / 16;
		int lastLvl = (height - level) - fullLvl * 16;
		for (int i = 0; i < fullX; i++) {
			for (int j = 0; j < fullY; j++) {
				if (j >= fullLvl) {
					drawCutIcon(icon, x + i * 16, y + j * 16, 16, 16, j == fullLvl ? lastLvl : 0);
				}
			}
		}
		for (int i = 0; i < fullX; i++) {
			drawCutIcon(icon, x + i * 16, y + fullY * 16, 16, lastY, fullLvl == fullY ? lastLvl : 0);
		}
		for (int i = 0; i < fullY; i++) {
			if (i >= fullLvl) {
				drawCutIcon(icon, x + fullX * 16, y + i * 16, lastX, 16, i == fullLvl ? lastLvl : 0);
			}
		}
		drawCutIcon(icon, x + fullX * 16, y + fullY * 16, lastX, lastY, fullLvl == fullY ? lastLvl : 0);
	}

	//The magic is here
	private void drawCutIcon(IIcon icon, int x, int y, int width, int height, int cut) {
		Tessellator tess = Tessellator.instance;
		tess.startDrawingQuads();
		tess.addVertexWithUV(x, y + height, zLevel, icon.getMinU(), icon.getInterpolatedV(height));
		tess.addVertexWithUV(x + width, y + height, zLevel, icon.getInterpolatedU(width), icon.getInterpolatedV(height));
		tess.addVertexWithUV(x + width, y + cut, zLevel, icon.getInterpolatedU(width), icon.getInterpolatedV(cut));
		tess.addVertexWithUV(x, y + cut, zLevel, icon.getMinU(), icon.getInterpolatedV(cut));
		tess.draw();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		int mX = mouseX - guiLeft;
		int mY = mouseY - guiTop;

		drawWidgets(mX, mY);
	}

	protected void drawWidgets(int mX, int mY) {
		for (Widget widget : container.getWidgets()) {
			if (widget.hidden) {
				continue;
			}
			bindTexture(texture);
			widget.draw(this, guiLeft, guiTop, mX, mY);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		drawLedgers(par1, par2);
	}

	protected void drawLedgers(int x, int y) {
		ledgerManager.drawLedgers(x, y);
	}

	public void drawCenteredString(String string, int xCenter, int yCenter, int textColor) {
		fontRendererObj.drawString(string, xCenter - fontRendererObj.getStringWidth(string) / 2, yCenter - fontRendererObj.FONT_HEIGHT / 2, textColor);
	}

	protected int getCenteredOffset(String string) {
		return getCenteredOffset(string, xSize);
	}

	protected int getCenteredOffset(String string, int xWidth) {
		return (xWidth - fontRendererObj.getStringWidth(string)) / 2;
	}

	/**
	 * Returns if the passed mouse position is over the specified slot.
	 */
	private boolean isMouseOverSlot(Slot slot, int mouseX, int mouseY) {
		int left = this.guiLeft;
		int top = this.guiTop;
		int realMouseX = mouseX - left;
		int realMouseY = mouseY - top;
		return realMouseX >= slot.xDisplayPosition - 1 && realMouseX < slot.xDisplayPosition + 16 + 1 && realMouseY >= slot.yDisplayPosition - 1 && realMouseY < slot.yDisplayPosition + 16 + 1;
	}

	// / MOUSE CLICKS
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		int mX = mouseX - guiLeft;
		int mY = mouseY - guiTop;

		for (Widget widget : container.getWidgets()) {
			if (widget.hidden) {
				continue;
			} else if (!widget.isMouseOver(mX, mY)) {
				continue;
			} else if (widget.handleMouseClick(mX, mY, mouseButton)) {
				return;
			}
		}
		super.mouseClicked(mouseX, mouseY, mouseButton);

		// / Handle ledger clicks
		ledgerManager.handleMouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int mouseButton, long time) {
		int mX = mouseX - guiLeft;
		int mY = mouseY - guiTop;
		for (Widget widget : container.getWidgets()) {
			if (widget.hidden) {
				continue;
			}
			widget.handleMouseMove(mX, mY, mouseButton, time);
		}

		Slot slot = getSlotAtPosition(mouseX, mouseY);
		if (mouseButton == 1 && slot instanceof IPhantomSlot) {
			return;
		}
		super.mouseClickMove(mouseX, mouseY, mouseButton, time);
	}

	@Override
	protected void mouseMovedOrUp(int mouseX, int mouseY, int eventType) {
		super.mouseMovedOrUp(mouseX, mouseY, eventType);

		int mX = mouseX - guiLeft;
		int mY = mouseY - guiTop;
		for (Widget widget : container.getWidgets()) {
			if (widget.hidden) {
				continue;
			}
			widget.handleMouseRelease(mX, mY, eventType);
		}
	}

	public Slot getSlotAtPosition(int x, int y) {
		for (int slotIndex = 0; slotIndex < this.inventorySlots.inventorySlots.size(); ++slotIndex) {
			Slot slot = (Slot) this.inventorySlots.inventorySlots.get(slotIndex);
			if (isMouseOverSlot(slot, x, y)) {
				return slot;
			}
		}
		return null;
	}

	public void bindTexture(ResourceLocation texture) {
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);
	}

	private void drawToolTips(ToolTip toolTips, int mouseX, int mouseY) {
		if (toolTips.size() > 0) {
			int left = this.guiLeft;
			int top = this.guiTop;
			int length = 0;
			int x;
			int y;

			for (ToolTipLine tip : toolTips) {
				y = this.fontRendererObj.getStringWidth(tip.text);

				if (y > length) {
					length = y;
				}
			}

			x = mouseX - left + 12;
			y = mouseY - top - 12;
			int var14 = 8;

			if (toolTips.size() > 1) {
				var14 += 2 + (toolTips.size() - 1) * 10;
			}

			this.zLevel = 300.0F;
			itemRender.zLevel = 300.0F;
			int var15 = -267386864;
			this.drawGradientRect(x - 3, y - 4, x + length + 3, y - 3, var15, var15);
			this.drawGradientRect(x - 3, y + var14 + 3, x + length + 3, y + var14 + 4, var15, var15);
			this.drawGradientRect(x - 3, y - 3, x + length + 3, y + var14 + 3, var15, var15);
			this.drawGradientRect(x - 4, y - 3, x - 3, y + var14 + 3, var15, var15);
			this.drawGradientRect(x + length + 3, y - 3, x + length + 4, y + var14 + 3, var15, var15);
			int var16 = 1347420415;
			int var17 = (var16 & 16711422) >> 1 | var16 & -16777216;
			this.drawGradientRect(x - 3, y - 3 + 1, x - 3 + 1, y + var14 + 3 - 1, var16, var17);
			this.drawGradientRect(x + length + 2, y - 3 + 1, x + length + 3, y + var14 + 3 - 1, var16, var17);
			this.drawGradientRect(x - 3, y - 3, x + length + 3, y - 3 + 1, var16, var16);
			this.drawGradientRect(x - 3, y + var14 + 2, x + length + 3, y + var14 + 3, var17, var17);

			for (ToolTipLine tip : toolTips) {
				String line = tip.text;

				if (tip.color == -1) {
					line = "\u00a77" + line;
				} else {
					line = "\u00a7" + Integer.toHexString(tip.color) + line;
				}

				this.fontRendererObj.drawStringWithShadow(line, x, y, -1);

				y += 10 + tip.getSpacing();
			}

			this.zLevel = 0.0F;
			itemRender.zLevel = 0.0F;
		}
	}

	protected class LedgerManager {

		protected ArrayList<Ledger> ledgers = new ArrayList<Ledger>();
		private GuiBuildCraft gui;

		public LedgerManager(GuiBuildCraft gui) {
			this.gui = gui;
		}

		public void add(Ledger ledger) {
			this.ledgers.add(ledger);
			if (SessionVars.getOpenedLedger() != null && ledger.getClass().equals(SessionVars.getOpenedLedger())) {
				ledger.setFullyOpen();
			}
		}

		/**
		 * Inserts a ledger into the next-to-last position.
		 *
		 * @param ledger
		 */
		public void insert(Ledger ledger) {
			this.ledgers.add(ledgers.size() - 1, ledger);
		}

		protected Ledger getAtPosition(int mX, int mY) {

			int xShift = ((gui.width - gui.xSize) / 2) + gui.xSize;
			int yShift = ((gui.height - gui.ySize) / 2) + 8;

			for (Ledger ledger : ledgers) {
				if (!ledger.isVisible()) {
					continue;
				}

				ledger.currentShiftX = xShift;
				ledger.currentShiftY = yShift;
				if (ledger.intersectsWith(mX, mY, xShift, yShift)) {
					return ledger;
				}

				yShift += ledger.getHeight();
			}

			return null;
		}

		protected void drawLedgers(int mouseX, int mouseY) {
			int yPos = 8;
			for (Ledger ledger : ledgers) {

				ledger.update();
				if (!ledger.isVisible()) {
					continue;
				}

				ledger.draw(xSize, yPos);
				yPos += ledger.getHeight();
			}

			Ledger ledger = getAtPosition(mouseX, mouseY);
			if (ledger != null) {
				int startX = mouseX - ((gui.width - gui.xSize) / 2) + 12;
				int startY = mouseY - ((gui.height - gui.ySize) / 2) - 12;

				String tooltip = ledger.getTooltip();
				int textWidth = fontRendererObj.getStringWidth(tooltip);
				drawGradientRect(startX - 3, startY - 3, startX + textWidth + 3, startY + 8 + 3, 0xc0000000, 0xc0000000);
				fontRendererObj.drawStringWithShadow(tooltip, startX, startY, -1);
			}
		}

		public void handleMouseClicked(int x, int y, int mouseButton) {

			if (mouseButton == 0) {

				Ledger ledger = this.getAtPosition(x, y);

				// Default action only if the mouse click was not handled by the
				// ledger itself.
				if (ledger != null && !ledger.handleMouseClicked(x, y, mouseButton)) {

					for (Ledger other : ledgers) {
						if (other != ledger && other.isOpen()) {
							other.toggleOpen();
						}
					}
					ledger.toggleOpen();
				}
			}

		}
	}

	/**
	 * Side ledger for guis
	 */
	protected abstract class Ledger {
		public int currentShiftX = 0;
		public int currentShiftY = 0;
		protected int overlayColor = 0xffffff;
		protected int maxWidth = 124;
		protected int minWidth = 24;
		protected int currentWidth = minWidth;
		protected int maxHeight = 24;
		protected int minHeight = 24;
		protected int currentHeight = minHeight;
		private boolean open;

		private long lastUpdateTime = -1;

		public void update() {
			if (lastUpdateTime < 0) {
				lastUpdateTime = (new Date()).getTime();
			}

			long updateTime = (new Date()).getTime();
			int updateVal = (int) Math.round((updateTime - lastUpdateTime) / 8.0);

			// Width
			if (open && currentWidth < maxWidth) {
				currentWidth += updateVal;
				currentWidth = Math.min(maxWidth, currentWidth);
			} else if (!open && currentWidth > minWidth) {
				currentWidth -= updateVal;
				currentWidth = Math.max(minWidth, currentWidth);
			}

			// Height
			if (open && currentHeight < maxHeight) {
				currentHeight += updateVal;
				currentHeight = Math.min(maxWidth, currentHeight);
			} else if (!open && currentHeight > minHeight) {
				currentHeight -= updateVal;
				currentHeight = Math.max(minHeight, currentHeight);
			}

			lastUpdateTime = updateTime;
		}

		public int getHeight() {
			return currentHeight;
		}

		public abstract void draw(int x, int y);

		public abstract String getTooltip();

		public boolean handleMouseClicked(int x, int y, int mouseButton) {
			return false;
		}

		public boolean intersectsWith(int mouseX, int mouseY, int shiftX, int shiftY) {

			if (mouseX >= shiftX && mouseX <= shiftX + currentWidth && mouseY >= shiftY && mouseY <= shiftY + getHeight()) {
				return true;
			}

			return false;
		}

		public void setFullyOpen() {
			open = true;
			currentWidth = maxWidth;
			currentHeight = maxHeight;
		}

		public void toggleOpen() {
			if (open) {
				open = false;
				SessionVars.setOpenedLedger(null);
			} else {
				open = true;
				SessionVars.setOpenedLedger(this.getClass());
			}
		}

		public boolean isVisible() {
			return true;
		}

		public boolean isOpen() {
			return this.open;
		}

		protected boolean isFullyOpened() {
			return currentWidth >= maxWidth;
		}

		protected void drawBackground(int x, int y) {

			RenderUtils.setGLColorFromInt(overlayColor);

			mc.renderEngine.bindTexture(LEDGER_TEXTURE);
			drawTexturedModalRect(x, y, 0, 256 - currentHeight, 4, currentHeight);
			drawTexturedModalRect(x + 4, y, 256 - currentWidth + 4, 0, currentWidth - 4, 4);
			// Add in top left corner again
			drawTexturedModalRect(x, y, 0, 0, 4, 4);

			drawTexturedModalRect(x + 4, y + 4, 256 - currentWidth + 4, 256 - currentHeight + 4, currentWidth - 4, currentHeight - 4);

			GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0F);
		}

		protected void drawIcon(IIcon icon, int x, int y) {
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0F);
			drawTexturedModelRectFromIcon(x, y, icon, 16, 16);
		}
	}

	public BuildCraftContainer getContainer() {
		return container;
	}
}
