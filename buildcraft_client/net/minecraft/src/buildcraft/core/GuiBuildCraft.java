package net.minecraft.src.buildcraft.core;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import net.minecraft.src.Container;
import net.minecraft.src.GuiContainer;
import net.minecraft.src.IInventory;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.core.utils.SessionVars;

public abstract class GuiBuildCraft extends GuiContainer {

	/// LEDGERS
	protected LedgerManager ledgerManager = new LedgerManager(this);
	
	protected class LedgerManager {

		private GuiBuildCraft gui;
		protected ArrayList<Ledger> ledgers = new ArrayList<Ledger>();
				
		public LedgerManager(GuiBuildCraft gui) {
			this.gui = gui;
		}
		
		public void add(Ledger ledger) {
			this.ledgers.add(ledger);
			if(SessionVars.getOpenedLedger() != null
					&& ledger.getClass().equals(SessionVars.getOpenedLedger()))
				ledger.setFullyOpen();
		}
		
		/**
		 * Inserts a ledger into the next-to-last position.
		 * @param ledger
		 */
		public void insert(Ledger ledger) {
			this.ledgers.add(ledgers.size() - 1, ledger);
		}
		
		protected Ledger getAtPosition(int mX, int mY) {
			
			int xShift = ((gui.width - gui.xSize) / 2) + gui.xSize;
			int yShift = ((gui.height - gui.ySize) / 2) + 8;
			
			for(int i = 0; i < ledgers.size(); i++) {
				Ledger ledger = ledgers.get(i);
				if(!ledger.isVisible())
					continue;
				
				ledger.currentShiftX = xShift;
				ledger.currentShiftY = yShift;
				if(ledger.intersectsWith(mX, mY, xShift, yShift))
					return ledger;
				
				yShift += ledger.getHeight();
			}
			
			return null;
		}
		
	    protected void drawLedgers(int mouseX, int mouseY) {
	    	
	    	int xPos = 8;
	    	for(Ledger ledger : ledgers) {
	    		
	    		ledger.update();
	    		if(!ledger.isVisible())
	    			continue;
	    		
	    		ledger.draw(xSize, xPos);
	    		xPos += ledger.getHeight();
	    	}
	    	
	    	Ledger ledger = getAtPosition(mouseX, mouseY);
			if(ledger != null) {
				int startX = mouseX - ((gui.width - gui.xSize) / 2) + 12;
				int startY = mouseY - ((gui.height - gui.ySize) / 2) - 12;
				
				String tooltip = ledger.getTooltip();
				int textWidth = fontRenderer.getStringWidth(tooltip);
				drawGradientRect(startX - 3, startY - 3, startX + textWidth + 3, startY + 8 + 3, 0xc0000000, 0xc0000000);
				fontRenderer.drawStringWithShadow(tooltip, startX, startY, -1);
			}
	    }
	    
	    public void handleMouseClicked(int x, int y, int mouseButton) {
	    	
	        if(mouseButton == 0) {
	        	
	        	Ledger ledger = this.getAtPosition(x, y);
	        	
	        	// Default action only if the mouse click was not handled by the ledger itself.
	        	if(ledger != null
	        			&& !ledger.handleMouseClicked(x, y, mouseButton)) {
	        		
	        		for(Ledger other : ledgers)
	        			if(other != ledger
	        				&& other.isOpen())
	        				other.toggleOpen();
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
			if(open && currentWidth < maxWidth)
				currentWidth += 4;
			else if(!open && currentWidth > minWidth)
				currentWidth -= 4;
			
			// Height
			if(open && currentHeight < maxHeight)
				currentHeight += 4;
			else if(!open && currentHeight > minHeight)
				currentHeight -= 4;
		}
		
		public int getHeight() { return currentHeight; }
		public abstract void draw(int x, int y);
		public abstract String getTooltip();
		
	    public boolean handleMouseClicked(int x, int y, int mouseButton) { return false; }
		public boolean intersectsWith(int mouseX, int mouseY, int shiftX, int shiftY) {
			
			if(mouseX >= shiftX && mouseX <= shiftX + currentWidth
					&& mouseY >= shiftY && mouseY <= shiftY + getHeight())
				return true;
					
			return false;
		}
		
		public void setFullyOpen() {
			open = true;
			currentWidth = maxWidth;
			currentHeight = maxHeight;
		}
		public void toggleOpen() {
			if(open) {
				open = false;
        		SessionVars.setOpenedLedger(null);
			} else {
				open = true;
        		SessionVars.setOpenedLedger(this.getClass());
			}
		}
		
		public boolean isVisible() { return true; }
		public boolean isOpen() { return this.open; }
		
		protected boolean isFullyOpened() {
			return currentWidth >= maxWidth;
		}
		
		protected void drawBackground(int x, int y) {
    		int texture = mc.renderEngine.getTexture("/gfx/gui/ledger.png");
    		
            float colorR = (overlayColor >> 16 & 255) / 255.0F;
            float colorG = (overlayColor >> 8 & 255) / 255.0F;
            float colorB = (overlayColor & 255) / 255.0F;

            GL11.glColor4f(colorR, colorG, colorB, 1.0F);
    		
    		mc.renderEngine.bindTexture(texture);
    		drawTexturedModalRect(x, y, 0, 256 - currentHeight, 4, currentHeight);
    		drawTexturedModalRect(x + 4, y, 256 - currentWidth + 4, 0, currentWidth - 4, 4);
    		// Add in top left corner again
    		drawTexturedModalRect(x, y, 0, 0, 4, 4);
    		
    		drawTexturedModalRect(x + 4, y + 4, 256 - currentWidth + 4, 256 - currentHeight + 4, currentWidth - 4, currentHeight - 4);
			
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0F);
		}
		
		protected void drawIcon(String texture, int iconIndex, int x, int y) {
			
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0F);
    		int tex = mc.renderEngine.getTexture(texture); 
    		mc.renderEngine.bindTexture(tex);
    		int textureRow = iconIndex >> 4;
    		int textureColumn = iconIndex - 16 * textureRow;
    		drawTexturedModalRect(x , y, 16 * textureColumn, 16 * textureRow, 16, 16);			
            
		}
	}
	
	protected TileEntity tile;
	
	public GuiBuildCraft(BuildCraftContainer container) {
		super(container);
		
		if(container.inventory instanceof TileEntity)
			tile = (TileEntity)container.inventory;
		
		initLedgers(container.inventory);
	}

	protected void initLedgers(IInventory inventory) {
	}
	
	@Override
    protected void drawGuiContainerForegroundLayer() {
    	ledgerManager.drawLedgers(mouseX, mouseY);
    }
	
	protected int getCenteredOffset(String string) {
		return getCenteredOffset(string, xSize);
	}
	protected int getCenteredOffset(String string, int xWidth) {
		return (xWidth - fontRenderer.getStringWidth(string)) / 2;
	}
	
	/// MOUSE CLICKS
    @Override
    protected void mouseClicked(int par1, int par2, int mouseButton) {
    	super.mouseClicked(par1, par2, mouseButton);
    	
        /// Handle ledger clicks
        ledgerManager.handleMouseClicked(par1, par2, mouseButton);
    }
	
	/// MOUSE MOVEMENT
	private int mouseX = 0;
	private int mouseY = 0;

	@Override
	protected void mouseMovedOrUp(int i, int j, int k) {
		super.mouseMovedOrUp(i, j, k);

		mouseX = i;
		mouseY = j;
	}

}
