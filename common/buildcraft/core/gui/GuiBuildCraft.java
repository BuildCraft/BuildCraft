package buildcraft.core.gui;

import buildcraft.core.DefaultProps;
import buildcraft.core.gui.slots.IPhantomSlot;
import buildcraft.core.gui.tooltips.IToolTipProvider;
import buildcraft.core.gui.tooltips.ToolTip;
import buildcraft.core.gui.tooltips.ToolTipLine;
import buildcraft.core.gui.widgets.Widget;
import buildcraft.core.utils.SessionVars;
import java.util.ArrayList;
import java.util.Collection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public abstract class GuiBuildCraft extends GuiContainer {

	public static final ResourceLocation LEDGER_TEXTURE = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_GUI + "/ledger.png");
	public final LedgerManager ledgerManager = new LedgerManager(this);
	public final TileEntity tile;
	public final ResourceLocation texture;
	public final BuildCraftContainer container;

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
		GL11.glTranslatef((float) left, (float) top, 0.0F);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderHelper.disableStandardItemLighting();

		InventoryPlayer playerInv = this.mc.thePlayer.inventory;

		if (playerInv.getItemStack() == null) {
			drawToolTips(container.getWidgets(), mouseX, mouseY);
			drawToolTips(buttonList, mouseX, mouseY);
			drawToolTips(inventorySlots.inventorySlots, mouseX, mouseY);
		}

		GL11.glPopMatrix();
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}

	private void drawToolTips(Collection objects, int mouseX, int mouseY) {
		for (Object obj : objects) {
			if (!(obj instanceof IToolTipProvider))
				continue;
			IToolTipProvider provider = (IToolTipProvider) obj;
			if (!provider.isToolTipVisible())
				continue;
			ToolTip tips = provider.getToolTip();
			if (tips == null)
				continue;
			boolean mouseOver = provider.isMouseOver(mouseX - guiLeft, mouseY - guiTop);
			tips.onTick(mouseOver);
			if (mouseOver && tips.isReady()) {
				tips.refresh();
				drawToolTips(tips, mouseX, mouseY);
			}
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		bindTexture(texture);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		drawTexturedModalRect(x, y, 0, 0, xSize, ySize);

		int mX = mouseX - guiLeft;
		int mY = mouseY - guiTop;

		for (Widget widget : container.getWidgets()) {
			if (widget.hidden)
				continue;
			bindTexture(texture);
			widget.draw(this, x, y, mX, mY);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		ledgerManager.drawLedgers(par1, par2);
	}

	protected int getCenteredOffset(String string) {
		return getCenteredOffset(string, xSize);
	}

	protected int getCenteredOffset(String string, int xWidth) {
		return (xWidth - fontRenderer.getStringWidth(string)) / 2;
	}

	/**
	 * Returns if the passed mouse position is over the specified slot.
	 */
	private boolean isMouseOverSlot(Slot slot, int mouseX, int mouseY) {
		int left = this.guiLeft;
		int top = this.guiTop;
		mouseX -= left;
		mouseY -= top;
		return mouseX >= slot.xDisplayPosition - 1 && mouseX < slot.xDisplayPosition + 16 + 1 && mouseY >= slot.yDisplayPosition - 1 && mouseY < slot.yDisplayPosition + 16 + 1;
	}

	// / MOUSE CLICKS
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		int mX = mouseX - guiLeft;
		int mY = mouseY - guiTop;

		for (Widget widget : container.getWidgets()) {
			if (widget.hidden)
				continue;
			if (!widget.isMouseOver(mX, mY))
				continue;
			if (widget.handleMouseClick(mX, mY, mouseButton))
				return;
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
			if (widget.hidden)
				continue;
			widget.handleMouseMove(mX, mY, mouseButton, time);
		}

		Slot slot = getSlotAtPosition(mouseX, mouseY);
		if (mouseButton == 1 && slot instanceof IPhantomSlot)
			return;
		super.mouseClickMove(mouseX, mouseY, mouseButton, time);
	}

	@Override
	protected void mouseMovedOrUp(int mouseX, int mouseY, int eventType) {
		super.mouseMovedOrUp(mouseX, mouseY, eventType);

		int mX = mouseX - guiLeft;
		int mY = mouseY - guiTop;
		for (Widget widget : container.getWidgets()) {
			if (widget.hidden)
				continue;
			widget.handleMouseRelease(mX, mY, eventType);
		}
	}

	public Slot getSlotAtPosition(int x, int y) {
		for (int slotIndex = 0; slotIndex < this.inventorySlots.inventorySlots.size(); ++slotIndex) {
			Slot slot = (Slot) this.inventorySlots.inventorySlots.get(slotIndex);
			if (isMouseOverSlot(slot, x, y))
				return slot;
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
			int lenght = 0;
			int x;
			int y;

			for (ToolTipLine tip : toolTips) {
				y = this.fontRenderer.getStringWidth(tip.text);

				if (y > lenght) {
					lenght = y;
				}
			}

			x = mouseX - left + 12;
			y = mouseY - top - 12;
			int var14 = 8;

			if (toolTips.size() > 1) {
				var14 += 2 + (toolTips.size() - 1) * 10;
			}

			this.zLevel = 300.0F;
			itemRenderer.zLevel = 300.0F;
			int var15 = -267386864;
			this.drawGradientRect(x - 3, y - 4, x + lenght + 3, y - 3, var15, var15);
			this.drawGradientRect(x - 3, y + var14 + 3, x + lenght + 3, y + var14 + 4, var15, var15);
			this.drawGradientRect(x - 3, y - 3, x + lenght + 3, y + var14 + 3, var15, var15);
			this.drawGradientRect(x - 4, y - 3, x - 3, y + var14 + 3, var15, var15);
			this.drawGradientRect(x + lenght + 3, y - 3, x + lenght + 4, y + var14 + 3, var15, var15);
			int var16 = 1347420415;
			int var17 = (var16 & 16711422) >> 1 | var16 & -16777216;
			this.drawGradientRect(x - 3, y - 3 + 1, x - 3 + 1, y + var14 + 3 - 1, var16, var17);
			this.drawGradientRect(x + lenght + 2, y - 3 + 1, x + lenght + 3, y + var14 + 3 - 1, var16, var17);
			this.drawGradientRect(x - 3, y - 3, x + lenght + 3, y - 3 + 1, var16, var16);
			this.drawGradientRect(x - 3, y + var14 + 2, x + lenght + 3, y + var14 + 3, var17, var17);

			for (ToolTipLine tip : toolTips) {
				String line = tip.text;

				if (tip.color == -1) {
					line = "\u00a77" + line;
				} else {
					line = "\u00a7" + Integer.toHexString(tip.color) + line;
				}

				this.fontRenderer.drawStringWithShadow(line, x, y, -1);

				y += 10 + tip.getSpacing();
			}

			this.zLevel = 0.0F;
			itemRenderer.zLevel = 0.0F;
		}
	}

	protected class LedgerManager {

		private GuiBuildCraft gui;
		protected ArrayList<Ledger> ledgers = new ArrayList<Ledger>();

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

			for (int i = 0; i < ledgers.size(); i++) {
				Ledger ledger = ledgers.get(i);
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

			int xPos = 8;
			for (Ledger ledger : ledgers) {

				ledger.update();
				if (!ledger.isVisible()) {
					continue;
				}

				ledger.draw(xSize, xPos);
				xPos += ledger.getHeight();
			}

			Ledger ledger = getAtPosition(mouseX, mouseY);
			if (ledger != null) {
				int startX = mouseX - ((gui.width - gui.xSize) / 2) + 12;
				int startY = mouseY - ((gui.height - gui.ySize) / 2) - 12;

				String tooltip = ledger.getTooltip();
				int textWidth = fontRenderer.getStringWidth(tooltip);
				drawGradientRect(startX - 3, startY - 3, startX + textWidth + 3, startY + 8 + 3, 0xc0000000, 0xc0000000);
				fontRenderer.drawStringWithShadow(tooltip, startX, startY, -1);
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

		private boolean open;
		protected int overlayColor = 0xffffff;
		public int currentShiftX = 0;
		public int currentShiftY = 0;
		protected int limitWidth = 128;
		protected int maxWidth = 124;
		protected int minWidth = 24;
		protected int currentWidth = minWidth;
		protected int maxHeight = 24;
		protected int minHeight = 24;
		protected int currentHeight = minHeight;

		public void update() {
			// Width
			if (open && currentWidth < maxWidth) {
				currentWidth += 4;
			} else if (!open && currentWidth > minWidth) {
				currentWidth -= 4;
			}

			// Height
			if (open && currentHeight < maxHeight) {
				currentHeight += 4;
			} else if (!open && currentHeight > minHeight) {
				currentHeight -= 4;
			}
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

			float colorR = (overlayColor >> 16 & 255) / 255.0F;
			float colorG = (overlayColor >> 8 & 255) / 255.0F;
			float colorB = (overlayColor & 255) / 255.0F;

			GL11.glColor4f(colorR, colorG, colorB, 1.0F);

			mc.renderEngine.bindTexture(LEDGER_TEXTURE);
			drawTexturedModalRect(x, y, 0, 256 - currentHeight, 4, currentHeight);
			drawTexturedModalRect(x + 4, y, 256 - currentWidth + 4, 0, currentWidth - 4, 4);
			// Add in top left corner again
			drawTexturedModalRect(x, y, 0, 0, 4, 4);

			drawTexturedModalRect(x + 4, y + 4, 256 - currentWidth + 4, 256 - currentHeight + 4, currentWidth - 4, currentHeight - 4);

			GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0F);
		}

		protected void drawIcon(Icon icon, int x, int y) {

			GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0F);
			drawTexturedModelRectFromIcon(x, y, icon, 16, 16);
		}
	}
}
