package buildcraft.lib.gui.ledger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;

import buildcraft.lib.client.sprite.LibSprites;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraft.lib.client.sprite.SpriteNineSliced;
import buildcraft.lib.gui.*;
import buildcraft.lib.gui.elem.GuiElementDrawable;
import buildcraft.lib.gui.elem.GuiElementText;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.gui.pos.PositionCallable;
import buildcraft.lib.misc.GuiUtil;
import buildcraft.lib.misc.RenderUtil;

public abstract class Ledger_Neptune implements ITooltipElement {
    public static final SpriteHolder SPRITE_EXP_NEG = LibSprites.LEDGER_LEFT;
    public static final SpriteHolder SPRITE_EXP_POS = LibSprites.LEDGER_RIGHT;

    public static final SpriteNineSliced SPRITE_SPLIT_NEG = new SpriteNineSliced(SPRITE_EXP_NEG, 4, 4, 12, 12, 16);
    public static final SpriteNineSliced SPRITE_SPLIT_POS = new SpriteNineSliced(SPRITE_EXP_POS, 4, 4, 12, 12, 16);

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
    protected int interpWidth = lastWidth;
    protected int interpHeight = lastHeight;

    protected final List<IGuiElement> closedElements = new ArrayList<>();
    protected final List<IGuiElement> openElements = new ArrayList<>();

    protected IGuiPosition positionAppending = positionLedgerInnerStart.offset(0, 3);
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
        appendText(this::getTitle, this::getTitleColour).setDropShadow(true);
        calculateMaxSize();
    }

    protected GuiElementText appendText(String text, int colour) {
        return appendText(() -> text, colour);
    }

    protected GuiElementText appendText(Supplier<String> text, int colour) {
        return appendText(text, () -> colour);
    }

    protected GuiElementText appendText(Supplier<String> text, IntSupplier colour) {
        return append(new GuiElementText(manager.gui, positionAppending, text, colour));
    }

    protected <T extends IGuiElement> T append(T element) {
        openElements.add(element);
        positionAppending = positionAppending.offset(() -> 0, () -> 5 + element.getHeight());
        return element;
    }

    /** The default implementation only works if all the elements are based around {@link #positionLedgerStart} */
    protected void calculateMaxSize() {
        int w = CLOSED_WIDTH;
        int h = CLOSED_HEIGHT;

        for (IGuiElement element : openElements) {
            w = Math.max(w, element.getEndX());
            h = Math.max(h, element.getEndY());
        }
        w -= getX();
        h -= getY();

        maxWidth = w + LEDGER_GAP * 2;
        maxHeight = h + LEDGER_GAP * 2;
    }

    public void update() {
        lastWidth = currentWidth;
        lastHeight = currentHeight;

        int targetWidth = currentWidth;
        int targetHeight = currentHeight;
        if (currentDifference == 1) {
            targetWidth = maxWidth;
            targetHeight = maxHeight;
        } else if (currentDifference == -1) {
            targetWidth = CLOSED_WIDTH;
            targetHeight = CLOSED_HEIGHT;
        } else {
            return;
        }

        int maxDiff = Math.max(maxWidth - CLOSED_WIDTH, maxHeight - CLOSED_HEIGHT);
        int ldgDiff = MathHelper.clamp(maxDiff / 5, 1, 15);

        // TODO: extract a method
        if (currentWidth < targetWidth) {
            currentWidth += ldgDiff;
            if (currentWidth > targetWidth) {
                currentWidth = targetWidth;
            }
        } else if (currentWidth > targetWidth) {
            currentWidth -= ldgDiff;
            if (currentWidth < targetWidth) {
                currentWidth = targetWidth;
            }
        }

        // TODO: extract a method
        if (currentHeight < targetHeight) {
            currentHeight += ldgDiff;
            if (currentHeight > targetHeight) {
                currentHeight = targetHeight;
            }
        } else if (currentHeight > targetHeight) {
            currentHeight -= ldgDiff;
            if (currentHeight < targetHeight) {
                currentHeight = targetHeight;
            }
        }

        return;
    }

    private static int clamp(int val, int min, int max) {
        return MathHelper.clamp(val, min, max);
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

    public final boolean shouldDrawOpen() {
        return currentWidth > CLOSED_WIDTH || currentHeight > CLOSED_HEIGHT;
    }

    public void drawBackground(int x, int y, float partialTicks) {
        startY = y;
        final SpriteNineSliced split;

        interpWidth = interp(lastWidth, currentWidth, partialTicks);
        interpHeight = interp(lastHeight, currentHeight, partialTicks);

        if (manager.expandPositive) {
            startX = x;
            split = SPRITE_SPLIT_POS;
        } else {
            startX = x - interpWidth;
            split = SPRITE_SPLIT_NEG;
        }
        RenderUtil.setGLColorFromIntPlusAlpha(getColour());
        split.draw(startX, startY, interpWidth, interpHeight);
        GlStateManager.color(1, 1, 1, 1);

        IGuiPosition pos2;

        if (manager.expandPositive) {
            pos2 = positionLedgerIconStart;
        } else {
            pos2 = positionLedgerIconStart;
        }

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GuiUtil.scissor(pos2.getX(), pos2.getY(), interpWidth - 8, interpHeight - 8);

        for (IGuiElement element : closedElements) {
            element.drawBackground(partialTicks);
        }
        if (shouldDrawOpen()) {
            for (IGuiElement element : openElements) {
                element.drawBackground(partialTicks);
            }
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    public void drawForeground(int x, int y, float partialTicks) {
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GuiUtil.scissor(positionLedgerIconStart.getX(), positionLedgerIconStart.getY(), interpWidth - 8, interpHeight - 8);

        for (IGuiElement element : closedElements) {
            element.drawForeground(partialTicks);
        }
        if (shouldDrawOpen()) {
            for (IGuiElement element : openElements) {
                element.drawForeground(partialTicks);
            }
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    public void onMouseClicked(int button) {
        if (getEnclosingRectangle().contains(manager.gui.mouse)) {
            if (currentDifference == 1) {
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
        if (lastHeight == currentHeight) return currentHeight;
        else if (partialTicks <= 0) return lastHeight;
        else if (partialTicks >= 1) return currentHeight;
        else return (int) (lastHeight * (1 - partialTicks) + currentHeight * partialTicks);
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
        if (shouldDrawOpen()) {
            for (IGuiElement element : openElements) {
                element.addToolTips(tooltips);
            }
        }
        if (currentWidth != maxWidth || currentHeight != maxHeight) {
            if (getEnclosingRectangle().contains(manager.gui.mouse)) {
                tooltips.add(new ToolTip(getTitle()));
            }
        }
    }
}
