package buildcraft.lib.gui.ledger;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

import buildcraft.core.lib.client.render.RenderUtils;
import buildcraft.core.lib.gui.tooltips.ToolTip;
import buildcraft.lib.client.sprite.LibSprites;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraft.lib.client.sprite.SpriteSplit;
import buildcraft.lib.gui.*;
import buildcraft.lib.gui.elem.GuiElementDrawable;
import buildcraft.lib.gui.elem.GuiElementText;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.gui.pos.PositionCallable;

public abstract class Ledger_Neptune implements ITooltipElement {
    public static final SpriteHolder SPRITE_EXP_NEG = LibSprites.LEDGER_LEFT;
    public static final SpriteHolder SPRITE_EXP_POS = LibSprites.LEDGER_RIGHT;

    public static final SpriteSplit SPRITE_SPLIT_NEG = new SpriteSplit(SPRITE_EXP_NEG, 4, 4, 12, 12, 16);
    public static final SpriteSplit SPRITE_SPLIT_POS = new SpriteSplit(SPRITE_EXP_POS, 4, 4, 12, 12, 16);

    public static final int LEDGER_CHANGE_DIFF = 20;
    public static final int LEDGER_GAP = 4;

    public static final int CLOSED_WIDTH = LEDGER_GAP + 16 + LEDGER_GAP;
    public static final int CLOSED_HEIGHT = LEDGER_GAP + 16 + LEDGER_GAP;

    public final LedgerManager_Neptune manager;

    public final IGuiPosition positionLedgerStart = new PositionCallable(this::getX, this::getY);
    public final IGuiPosition positionLedgerIconStart = positionLedgerStart.offset(LEDGER_GAP, LEDGER_GAP);
    public final IGuiPosition positionLedgerInnerStart = positionLedgerIconStart.offset(20, 0);

    protected int maxWidth = 96, maxHeight = 48;

    protected int currentWidth = CLOSED_WIDTH;
    protected int currentHeight = CLOSED_HEIGHT;
    protected int lastWidth = currentWidth;
    protected int lastHeight = currentHeight;

    protected final List<IGuiElement> closedElements = new ArrayList<>();
    protected final List<IGuiElement> openElements = new ArrayList<>();

    protected String title = "unknown";

    /** -1 means shrinking, 0 no change, 1 expanding */
    private int currentDifference = 0;
    private int startX, startY;

    public Ledger_Neptune(LedgerManager_Neptune manager) {
        this.manager = manager;

        GuiRectangle iconRect = new GuiRectangle(0, 0, 16, 16);
        ISimpleDrawable drawable = this::drawIcon;
        GuiBC8<?> gui = manager.gui;
        closedElements.add(new GuiElementDrawable(gui, positionLedgerIconStart, iconRect, drawable, false));
        IGuiPosition pos = positionLedgerIconStart.offset(20, 3);
        GuiElementText elementTitle = new GuiElementText(gui, pos, iconRect, this::getTitle, this::getTitleColour, false);
        elementTitle.dropShadow = true;
        openElements.add(elementTitle);
        calculateMaxSize();
    }

    /** The default implementation only works if all the elements are based around {@link #positionLedgerStart} */
    protected void calculateMaxSize() {
        int w = CLOSED_WIDTH;
        int h = CLOSED_HEIGHT;

        for (IGuiElement element : openElements) {
            w = Math.max(w, element.getX() + element.getWidth());
            h = Math.max(h, element.getY() + element.getHeight());
        }

        maxWidth = w + LEDGER_GAP * 2;
        maxHeight = h + LEDGER_GAP * 2;
    }

    public void update() {
        lastWidth = currentWidth;
        lastHeight = currentHeight;

        if (currentDifference != 0) {
            int diff = currentDifference * LEDGER_CHANGE_DIFF;
            currentWidth = cap(currentWidth + diff, CLOSED_WIDTH, maxWidth);
            currentHeight = cap(currentHeight + diff, CLOSED_HEIGHT, maxHeight);
        }
        if (currentDifference == 1) {
            if (isFullyOpen()) {
                currentDifference = 0;
            }
        } else if (currentDifference == -1) {
            if (currentWidth <= CLOSED_WIDTH && currentHeight <= CLOSED_HEIGHT) {
                currentDifference = 0;
            }
        }
    }

    private static int cap(int toCap, int min, int max) {
        return Math.min(max, Math.max(toCap, min));
    }

    private static int interp(int past, int current, float partialTicks) {
        if (past == current) {
            return current;
        }
        if (partialTicks <= 0) {
            return past;
        }
        if (partialTicks >= 1) {
            return current;
        }
        return (int) (past * (1 - partialTicks) + current * partialTicks);
    }

    public GuiRectangle getEnclosingRectangle() {
        return new GuiRectangle(startX, startY, currentWidth, currentHeight);
    }

    public final boolean isFullyOpen() {
        return currentWidth >= maxWidth && currentHeight >= maxHeight;
    }

    public final void drawBackground(int x, int y, float partialTicks) {
        startY = y;
        final SpriteSplit split;
        int actualWidth = cap(interp(lastWidth, currentWidth, partialTicks), CLOSED_WIDTH, maxWidth);
        int actualHeight = cap(interp(lastHeight, currentHeight, partialTicks), CLOSED_HEIGHT, maxHeight);

        if (manager.expandPositive) {
            startX = x;
            split = SPRITE_SPLIT_POS;
        } else {
            startX = x - actualWidth;
            split = SPRITE_SPLIT_NEG;
        }
        RenderUtils.setGLColorFromIntPlusAlpha(getColour());
        split.draw(startX, startY, actualWidth, actualHeight);
        GlStateManager.color(1, 1, 1, 1);

        for (IGuiElement element : closedElements) {
            element.drawBackground(partialTicks);
        }
        if (isFullyOpen()) {
            for (IGuiElement element : openElements) {
                element.drawBackground(partialTicks);
            }
        }
    }

    public final void drawForeground(int x, int y, float partialTicks) {
        for (IGuiElement element : closedElements) {
            element.drawForeground(partialTicks);
        }
        if (isFullyOpen()) {
            for (IGuiElement element : openElements) {
                element.drawForeground(partialTicks);
            }
        }
    }

    public void onMouseClicked(int button) {
        if (getEnclosingRectangle().contains(manager.gui.mouse)) {
            if (isFullyOpen()) {
                currentDifference = -1;
            } else if (currentDifference == 1) {
                currentDifference = -1;
            } else {
                currentDifference = 1;
            }
        }
    }

    public void onMouseDragged(int button, long ticksSinceClick) {

    }

    public void onMouseReleased(int button) {

    }

    protected void drawIcon(int x, int y) {

    }

    /** @return The colour of this ledger, in ARGB */
    public abstract int getColour();

    public int getX() {
        return startX;
    }

    public int getY() {
        return startY;
    }

    public int getHeight(float partialTicks) {
        return (int) (lastHeight * (1 - partialTicks) + currentHeight * partialTicks);
    }

    public String getTitle() {
        return I18n.format(title);
    }

    public int getTitleColour() {
        return 0xFF_E1_C9_2F;
    }

    @Override
    public void addToolTips(List<ToolTip> tooltips) {
        for (IGuiElement element : closedElements) {
            element.addToolTips(tooltips);
        }
        if (isFullyOpen()) {
            for (IGuiElement element : openElements) {
                element.addToolTips(tooltips);
            }
        } else if (getEnclosingRectangle().contains(manager.gui.mouse)) {
            tooltips.add(new ToolTip(getTitle()));
        }
    }
}
