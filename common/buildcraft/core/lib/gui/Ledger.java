package buildcraft.core.lib.gui;

import java.util.Date;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import buildcraft.core.lib.render.RenderUtils;
import buildcraft.core.lib.utils.SessionVars;

/** Side ledger for guis */
public abstract class Ledger {
    private final GuiBuildCraft guiBuildCraft;

    public Ledger(GuiBuildCraft guiBuildCraft) {
        this.guiBuildCraft = guiBuildCraft;
    }

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

    public int getWidth() {
        return currentWidth;
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

        this.guiBuildCraft.mc.renderEngine.bindTexture(GuiBuildCraft.LEDGER_TEXTURE);
        this.guiBuildCraft.drawTexturedModalRect(x, y, 0, 256 - currentHeight, 4, currentHeight);
        this.guiBuildCraft.drawTexturedModalRect(x + 4, y, 256 - currentWidth + 4, 0, currentWidth - 4, 4);
        // Add in top left corner again
        this.guiBuildCraft.drawTexturedModalRect(x, y, 0, 0, 4, 4);

        this.guiBuildCraft.drawTexturedModalRect(x + 4, y + 4, 256 - currentWidth + 4, 256 - currentHeight + 4, currentWidth - 4, currentHeight - 4);

        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0F);
    }

    protected void drawIcon(TextureAtlasSprite icon, int x, int y) {
        this.guiBuildCraft.drawTexturedModalRect(x, y, icon, 16, 16);
    }
}
