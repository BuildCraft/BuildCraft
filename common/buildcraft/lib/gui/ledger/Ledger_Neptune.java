/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.ledger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;

import buildcraft.api.core.render.ISprite;

import buildcraft.lib.BCLibSprites;
import buildcraft.lib.client.sprite.SpriteNineSliced;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.IContainingElement;
import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.IInteractionElement;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.gui.elem.GuiElementDrawable;
import buildcraft.lib.gui.elem.GuiElementText;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.misc.GuiUtil;
import buildcraft.lib.misc.RenderUtil;

// TODO: Json "parent" and "parent position" - useful for ledgers, and where ledgers begin and end.
// Or that could be done in-code, rather than in json.

public class Ledger_Neptune implements IInteractionElement, IContainingElement {
    public static final ISprite SPRITE_EXP_NEG = BCLibSprites.LEDGER_LEFT;
    public static final ISprite SPRITE_EXP_POS = BCLibSprites.LEDGER_RIGHT;

    public static final SpriteNineSliced SPRITE_SPLIT_NEG = new SpriteNineSliced(SPRITE_EXP_NEG, 4, 4, 12, 12, 16);
    public static final SpriteNineSliced SPRITE_SPLIT_POS = new SpriteNineSliced(SPRITE_EXP_POS, 4, 4, 12, 12, 16);

    public static final int LEDGER_CHANGE_DIFF = 20;
    public static final int LEDGER_GAP = 4;

    public static final int CLOSED_WIDTH = 2 + 16 + LEDGER_GAP;
    public static final int CLOSED_HEIGHT = LEDGER_GAP + 16 + LEDGER_GAP;

    public final GuiBC8<?> gui;
    public final int colour;
    public final boolean expandPositive;

    public final IGuiPosition positionLedgerStart;
    public final IGuiPosition positionLedgerIconStart;
    public final IGuiPosition positionLedgerInnerStart;

    protected int maxWidth = 96, maxHeight = 48;

    protected int currentWidth = CLOSED_WIDTH;
    protected int currentHeight = CLOSED_HEIGHT;
    protected int lastWidth = currentWidth;
    protected int lastHeight = currentHeight;
    protected int interpWidth = lastWidth;
    protected int interpHeight = lastHeight;

    protected final List<IGuiElement> closedElements = new ArrayList<>();
    protected final List<IGuiElement> openElements = new ArrayList<>();

    protected IGuiPosition positionAppending;
    protected String title = "unknown";

    /** -1 means shrinking, 0 no change, 1 expanding */
    private int currentDifference = 0;

    public Ledger_Neptune(GuiBC8<?> gui, int colour, boolean expandPositive) {
        this.gui = gui;
        this.colour = colour;
        this.expandPositive = expandPositive;
        if (expandPositive) {
            positionLedgerStart = gui.lowerRightLedgerPos;
            gui.lowerRightLedgerPos = getPosition(-1, 1).offset(0, 5);
            positionLedgerIconStart = positionLedgerStart.offset(2, LEDGER_GAP);
        } else {
            positionLedgerStart = gui.lowerLeftLedgerPos.offset(() -> -getWidth(), 0);
            gui.lowerLeftLedgerPos = getPosition(1, 1).offset(0, 5);
            positionLedgerIconStart = positionLedgerStart.offset(LEDGER_GAP, LEDGER_GAP);
        }
        positionLedgerInnerStart = positionLedgerIconStart.offset(16 + LEDGER_GAP, 0);
        positionAppending = positionLedgerInnerStart.offset(0, 3);

        GuiRectangle iconRect = new GuiRectangle(0, 0, 16, 16);
        ISimpleDrawable drawable = this::drawIcon;
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
        return append(new GuiElementText(gui, positionAppending, text, colour));
    }

    protected <T extends IGuiElement> T append(T element) {
        openElements.add(element);
        positionAppending = positionAppending.offset(() -> 0, () -> 3 + element.getHeight());
        return element;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /** The default implementation only works if all the elements are based around {@link #positionLedgerStart} */
    public void calculateMaxSize() {
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

    @Override
    public void tick() {
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

    @Deprecated
    public GuiRectangle getEnclosingRectangle() {
        return asImmutable();
    }

    public final boolean shouldDrawOpen() {
        return currentWidth > CLOSED_WIDTH || currentHeight > CLOSED_HEIGHT;
    }

    @Override
    public List<IGuiElement> getChildElements() {
        return openElements;
    }

    public List<IGuiElement> getClosedElements() {
        return closedElements;
    }

    @Override
    public void drawBackground(float partialTicks) {
        int startX = getX();
        int startY = getY();
        final SpriteNineSliced split;

        interpWidth = interp(lastWidth, currentWidth, partialTicks);
        interpHeight = interp(lastHeight, currentHeight, partialTicks);

        if (expandPositive) {
            split = SPRITE_SPLIT_POS;
        } else {
            split = SPRITE_SPLIT_NEG;
        }

        RenderUtil.setGLColorFromIntPlusAlpha(colour);
        split.draw(startX, startY, interpWidth, interpHeight);
        GlStateManager.color(1, 1, 1, 1);

        IGuiPosition pos2;

        if (expandPositive) {
            pos2 = positionLedgerIconStart;
        } else {
            pos2 = positionLedgerIconStart;
        }

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GuiUtil.scissor(pos2.getX(), pos2.getY(), interpWidth - 4, interpHeight - 8);

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

    @Override
    public void drawForeground(float partialTicks) {
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GuiUtil.scissor(positionLedgerIconStart.getX(), positionLedgerIconStart.getY(), interpWidth - 8,
            interpHeight - 8);

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

    @Override
    public void onMouseClicked(int button) {
        boolean childClicked = false;
        for (IGuiElement elem : openElements) {
            if (elem instanceof IInteractionElement) {
                ((IInteractionElement) elem).onMouseClicked(button);
                childClicked |= elem.contains(gui.mouse);
            }
        }
        for (IGuiElement elem : closedElements) {
            if (elem instanceof IInteractionElement) {
                ((IInteractionElement) elem).onMouseClicked(button);
                childClicked |= elem.contains(gui.mouse);
            }
        }
        if (!childClicked && contains(gui.mouse)) {
            if (currentDifference == 1) {
                currentDifference = -1;
            } else {
                currentDifference = 1;
            }
        }
    }

    @Override
    public void onMouseDragged(int button, long ticksSinceClick) {
        for (IGuiElement elem : openElements) {
            if (elem instanceof IInteractionElement) {
                ((IInteractionElement) elem).onMouseDragged(button, ticksSinceClick);
            }
        }
        for (IGuiElement elem : closedElements) {
            if (elem instanceof IInteractionElement) {
                ((IInteractionElement) elem).onMouseDragged(button, ticksSinceClick);
            }
        }
    }

    @Override
    public void onMouseReleased(int button) {
        for (IGuiElement elem : openElements) {
            if (elem instanceof IInteractionElement) {
                ((IInteractionElement) elem).onMouseReleased(button);
            }
        }
        for (IGuiElement elem : closedElements) {
            if (elem instanceof IInteractionElement) {
                ((IInteractionElement) elem).onMouseReleased(button);
            }
        }
    }

    protected void drawIcon(int x, int y) {

    }

    @Override
    public int getX() {
        return positionLedgerStart.getX();
    }

    @Override
    public int getY() {
        return positionLedgerStart.getY();
    }

    @Override
    public int getWidth() {
        float partialTicks = gui.getLastPartialTicks();
        if (lastWidth == currentWidth) return currentWidth;
        else if (partialTicks <= 0) return lastWidth;
        else if (partialTicks >= 1) return currentWidth;
        else return (int) (lastWidth * (1 - partialTicks) + currentWidth * partialTicks);
    }

    @Override
    public int getHeight() {
        float partialTicks = gui.getLastPartialTicks();
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
            if (getEnclosingRectangle().contains(gui.mouse)) {
                tooltips.add(new ToolTip(getTitle()));
            }
        }
    }
}
