/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.gui.ledger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.gui.DrawContext;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.math.MathHelper;

import buildcraft.api.core.render.ISprite;

import buildcraft.lib.BCLibSprites;
import buildcraft.lib.client.sprite.SpriteNineSliced;
import buildcraft.lib.expression.api.IVariableNode.IVariableNodeBoolean;
import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.gui.IContainingElement;
import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.IInteractionElement;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.gui.elem.GuiElementDrawable;
import buildcraft.lib.gui.elem.GuiElementText;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.help.ElementHelpInfo.HelpPosition;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiPosition;

@Environment(EnvType.CLIENT)
public class Ledger_Neptune implements IInteractionElement, IContainingElement {

    public static final SpriteNineSliced SPRITE_SPLIT_NEG =
        new SpriteNineSliced(BCLibSprites.LEDGER_LEFT, 4, 4, 12, 12, 16);
    public static final SpriteNineSliced SPRITE_SPLIT_POS =
        new SpriteNineSliced(BCLibSprites.LEDGER_RIGHT, 4, 4, 12, 12, 16);

    public static final int LEDGER_GAP = 4;
    public static final int CLOSED_WIDTH = 2 + 16 + LEDGER_GAP;
    public static final int CLOSED_HEIGHT = LEDGER_GAP + 16 + LEDGER_GAP;

    public final BuildCraftGui gui;
    public final int colour;
    public final boolean expandPositive;

    public final IGuiPosition positionLedgerStart;
    public final IGuiPosition positionLedgerIconStart;
    public final IGuiPosition positionLedgerInnerStart;

    protected double maxWidth = 96, maxHeight = 48;
    protected double currentWidth = CLOSED_WIDTH, currentHeight = CLOSED_HEIGHT;
    protected double lastWidth = currentWidth, lastHeight = currentHeight;
    protected double interpWidth = lastWidth, interpHeight = lastHeight;

    protected final List<IGuiElement> closedElements = new ArrayList<>();
    protected final List<IGuiElement> openElements = new ArrayList<>();

    protected IGuiPosition positionAppending;
    protected String title = "unknown";

    /** -1 shrinking, 0 no change, 1 expanding */
    private int currentDifference = 0;

    @Nullable
    private IVariableNodeBoolean isOpenProperty;

    public Ledger_Neptune(BuildCraftGui gui, int colour, boolean expandPositive) {
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

        ISimpleDrawable drawable = this::drawIcon;
        closedElements.add(new GuiElementDrawable(gui,
            new GuiRectangle(0, 0, 16, 16).offset(positionLedgerIconStart), drawable, false));
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

    public void setTitle(String title) { this.title = title; }

    public void setOpenProperty(IVariableNodeBoolean prop) {
        this.isOpenProperty = prop;
        if (prop.evaluate()) {
            currentDifference = 1;
            lastWidth = currentWidth = maxWidth;
            lastHeight = currentHeight = maxHeight;
        } else {
            currentDifference = -1;
            lastWidth = currentWidth = CLOSED_WIDTH;
            lastHeight = currentHeight = CLOSED_HEIGHT;
        }
    }

    @Override
    public void calculateSizes() {
        calculateMaxSize();
        if (isOpenProperty != null) setOpenProperty(isOpenProperty);
    }

    public void calculateMaxSize() {
        double w = CLOSED_WIDTH, h = CLOSED_HEIGHT;
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

        double targetWidth, targetHeight;
        if (currentDifference == 1) { targetWidth = maxWidth; targetHeight = maxHeight; }
        else if (currentDifference == -1) { targetWidth = CLOSED_WIDTH; targetHeight = CLOSED_HEIGHT; }
        else return;

        double maxDiff = Math.max(maxWidth - CLOSED_WIDTH, maxHeight - CLOSED_HEIGHT);
        double ldgDiff = MathHelper.clamp(maxDiff / 5, 1, 15);

        currentWidth = approach(currentWidth, targetWidth, ldgDiff);
        currentHeight = approach(currentHeight, targetHeight, ldgDiff);
    }

    private static double approach(double current, double target, double step) {
        if (current < target) return Math.min(current + step, target);
        if (current > target) return Math.max(current - step, target);
        return current;
    }

    private static double interp(double past, double current, float partialTicks) {
        if (past == current) return current;
        if (partialTicks <= 0) return past;
        if (partialTicks >= 1) return current;
        return past * (1 - partialTicks) + current * partialTicks;
    }

    public final boolean shouldDrawOpen() {
        return currentWidth > CLOSED_WIDTH || currentHeight > CLOSED_HEIGHT;
    }

    @Override
    public List<IGuiElement> getChildElements() { return openElements; }

    @Override
    public IGuiPosition getChildElementPosition() { return positionLedgerInnerStart; }

    public List<IGuiElement> getClosedElements() { return closedElements; }

    @Override
    public void drawBackground(DrawContext context, float partialTicks) {
        interpWidth = interp(lastWidth, currentWidth, partialTicks);
        interpHeight = interp(lastHeight, currentHeight, partialTicks);

        SpriteNineSliced split = expandPositive ? SPRITE_SPLIT_POS : SPRITE_SPLIT_NEG;

        // tint with ledger colour, preserving alpha
        float a = ((colour >> 24) & 0xFF) / 255f;
        float r = ((colour >> 16) & 0xFF) / 255f;
        float g = ((colour >> 8) & 0xFF) / 255f;
        float b = (colour & 0xFF) / 255f;
        RenderSystem.setShaderColor(r, g, b, a);
        split.draw(getX(), getY(), interpWidth, interpHeight);
        RenderSystem.setShaderColor(1, 1, 1, 1);

        int x1 = (int) positionLedgerIconStart.getX();
        int y1 = (int) positionLedgerIconStart.getY();
        int x2 = (int) (getX() + interpWidth - 4);
        int y2 = (int) (getY() + interpHeight - 8);
        context.enableScissor(x1, y1, x2, y2);
        for (IGuiElement element : closedElements) element.drawBackground(context, partialTicks);
        if (shouldDrawOpen()) {
            for (IGuiElement element : openElements) element.drawBackground(context, partialTicks);
        }
        context.disableScissor();
    }

    @Override
    public void drawForeground(DrawContext context, float partialTicks) {
        int x1 = (int) positionLedgerIconStart.getX();
        int y1 = (int) positionLedgerIconStart.getY();
        int x2 = (int) (getX() + interpWidth - 8);
        int y2 = (int) (getY() + interpHeight - 8);
        context.enableScissor(x1, y1, x2, y2);
        for (IGuiElement element : closedElements) element.drawForeground(context, partialTicks);
        if (shouldDrawOpen()) {
            for (IGuiElement element : openElements) element.drawForeground(context, partialTicks);
        }
        context.disableScissor();
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
            boolean nowOpen = (currentDifference != 1);
            currentDifference = nowOpen ? 1 : -1;
            if (isOpenProperty != null) isOpenProperty.set(nowOpen);
        }
    }

    @Override
    public void onMouseDragged(int button, long ticksSinceClick) {
        for (IGuiElement elem : openElements)
            if (elem instanceof IInteractionElement) ((IInteractionElement) elem).onMouseDragged(button, ticksSinceClick);
        for (IGuiElement elem : closedElements)
            if (elem instanceof IInteractionElement) ((IInteractionElement) elem).onMouseDragged(button, ticksSinceClick);
    }

    @Override
    public void onMouseReleased(int button) {
        for (IGuiElement elem : openElements)
            if (elem instanceof IInteractionElement) ((IInteractionElement) elem).onMouseReleased(button);
        for (IGuiElement elem : closedElements)
            if (elem instanceof IInteractionElement) ((IInteractionElement) elem).onMouseReleased(button);
    }

    protected void drawIcon(double x, double y) {}

    @Override
    public double getX() { return positionLedgerStart.getX(); }

    @Override
    public double getY() { return positionLedgerStart.getY(); }

    @Override
    public double getWidth() {
        float pt = gui.getLastPartialTicks();
        if (lastWidth == currentWidth) return currentWidth;
        else if (pt <= 0) return lastWidth;
        else if (pt >= 1) return currentWidth;
        else return lastWidth * (1 - pt) + currentWidth * pt;
    }

    @Override
    public double getHeight() {
        float pt = gui.getLastPartialTicks();
        if (lastHeight == currentHeight) return currentHeight;
        else if (pt <= 0) return lastHeight;
        else if (pt >= 1) return currentHeight;
        else return lastHeight * (1 - pt) + currentHeight * pt;
    }

    public String getTitle() { return title; }

    public int getTitleColour() { return 0xFF_E1_C9_2F; }

    @Override
    public void addToolTips(List<ToolTip> tooltips) {
        for (IGuiElement element : closedElements) element.addToolTips(tooltips);
        if (shouldDrawOpen()) {
            for (IGuiElement element : openElements) element.addToolTips(tooltips);
        }
        if (currentWidth != maxWidth || currentHeight != maxHeight) {
            if (contains(gui.mouse)) tooltips.add(new ToolTip(getTitle()));
        }
    }

    @Override
    public void addHelpElements(List<HelpPosition> elements) {
        for (IGuiElement element : closedElements) element.addHelpElements(elements);
        if (currentWidth == maxWidth && currentHeight == maxHeight) {
            for (IGuiElement element : openElements) element.addHelpElements(elements);
        }
    }
}
