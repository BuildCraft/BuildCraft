/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.gui.ledger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import ct.buildcraft.api.core.render.ISprite;
import ct.buildcraft.lib.BCLibSprites;
import ct.buildcraft.lib.client.sprite.SpriteNineSliced;
import ct.buildcraft.lib.expression.api.IVariableNode.IVariableNodeBoolean;
import ct.buildcraft.lib.gui.BuildCraftGui;
import ct.buildcraft.lib.gui.IContainingElement;
import ct.buildcraft.lib.gui.IGuiElement;
import ct.buildcraft.lib.gui.IInteractionElement;
import ct.buildcraft.lib.gui.ISimpleDrawable;
import ct.buildcraft.lib.gui.elem.GuiElementDrawable;
import ct.buildcraft.lib.gui.elem.GuiElementText;
import ct.buildcraft.lib.gui.elem.ToolTip;
import ct.buildcraft.lib.gui.help.ElementHelpInfo.HelpPosition;
import ct.buildcraft.lib.gui.pos.GuiRectangle;
import ct.buildcraft.lib.gui.pos.IGuiPosition;
import ct.buildcraft.lib.misc.GuiUtil;
import ct.buildcraft.lib.misc.GuiUtil.AutoGlScissor;
import ct.buildcraft.lib.misc.RenderUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class Ledger_Neptune implements IInteractionElement, IContainingElement {
    public static final ISprite SPRITE_EXP_NEG = BCLibSprites.LEDGER_LEFT;
    public static final ISprite SPRITE_EXP_POS = BCLibSprites.LEDGER_RIGHT;

    public static final SpriteNineSliced SPRITE_SPLIT_NEG = new SpriteNineSliced(SPRITE_EXP_NEG, 4, 4, 12, 12, 16);
    public static final SpriteNineSliced SPRITE_SPLIT_POS = new SpriteNineSliced(SPRITE_EXP_POS, 4, 4, 12, 12, 16);

    public static final int LEDGER_CHANGE_DIFF = 2;
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

    protected double currentWidth = CLOSED_WIDTH;
    protected double currentHeight = CLOSED_HEIGHT;
    protected double lastWidth = currentWidth;
    protected double lastHeight = currentHeight;
    protected double interpWidth = lastWidth;
    protected double interpHeight = lastHeight;

    protected final List<IGuiElement> closedElements = new ArrayList<>();
    protected final List<IGuiElement> openElements = new ArrayList<>();

    protected IGuiPosition positionAppending;
    protected Component title = Component.literal("unknown");

    /** -1 means shrinking, 0 no change, 1 expanding */
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

        GuiRectangle iconRect = new GuiRectangle(0, 0, 16, 16);
        ISimpleDrawable drawable = this::drawIcon;
        closedElements.add(new GuiElementDrawable(gui, iconRect.offset(positionLedgerIconStart), drawable, false));
        appendText(this::getTitle, this::getTitleColour).setDropShadow(true);
        calculateMaxSize();
    }

    protected GuiElementText appendText(Component text, int colour) {
        return appendText(() -> text, colour);
    }

    protected GuiElementText appendText(Supplier<Component> text, int colour) {
        return appendText(text, () -> colour);
    }

    protected GuiElementText appendText(Supplier<Component> text, IntSupplier colour) {
        return append(new GuiElementText(gui, positionAppending, text, colour));
    }

    protected <T extends IGuiElement> T append(T element) {
        openElements.add(element);
        positionAppending = positionAppending.offset(() -> 0, () -> 3 + element.getHeight());
        return element;
    }
    
    public void setTitle(String title) {
        this.title = Component.translatable(title);
    }

    public void setTitle(Component title) {
        this.title = title;
    }

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
        if (isOpenProperty != null) {
            setOpenProperty(isOpenProperty);
        }
    }

    /** The default implementation only works if all the elements are based around {@link #positionLedgerStart} */
    public void calculateMaxSize() {
        double w = CLOSED_WIDTH;
        double h = CLOSED_HEIGHT;

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

        double targetWidth = currentWidth;
        double targetHeight = currentHeight;
        if (currentDifference == 1) {
            targetWidth = maxWidth;
            targetHeight = maxHeight;
        } else if (currentDifference == -1) {
            targetWidth = CLOSED_WIDTH;
            targetHeight = CLOSED_HEIGHT;
        } else {
            return;
        }

        double maxDiff = Math.max(maxWidth - CLOSED_WIDTH, maxHeight - CLOSED_HEIGHT);
        double ldgDiff = Mth.clamp(maxDiff / 5, 1, 15);

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

    private static double interp(double past, double current, float partialTicks) {
        if (past == current) {
            return current;
        }
        if (partialTicks <= 0) {
            return past;
        }
        if (partialTicks >= 1) {
            return current;
        }
        return past * (1 - partialTicks) + current * partialTicks;
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

    @Override
    public IGuiPosition getChildElementPosition() {
        return positionLedgerInnerStart;
    }

    public List<IGuiElement> getClosedElements() {
        return closedElements;
    }

    @Override
    public void drawBackground(PoseStack pose, float partialTicks) {
        double startX = getX();
        double startY = getY();
        final SpriteNineSliced split;

        interpWidth = interp(lastWidth, currentWidth, partialTicks);
        interpHeight = interp(lastHeight, currentHeight, partialTicks);

        if (expandPositive) {
            split = SPRITE_SPLIT_POS;
        } else {
            split = SPRITE_SPLIT_NEG;
        }

        RenderUtil.setGLColorFromIntPlusAlpha(colour);
        split.draw(pose, startX, startY, interpWidth, interpHeight);
        RenderSystem.setShaderColor(1, 1, 1, 1);

        IGuiPosition pos2;

        if (expandPositive) {
            pos2 = positionLedgerIconStart;
        } else {
            pos2 = positionLedgerIconStart;
        }

        
        try (AutoGlScissor a = GuiUtil.scissor(pos2.getX(), pos2.getY(), interpWidth - 4, interpHeight - 8)) {

            for (IGuiElement element : closedElements) {
                element.drawBackground(pose, partialTicks);
            }
            if (shouldDrawOpen()) {
                for (IGuiElement element : openElements) {
                    element.drawBackground(pose, partialTicks);
                }
            }
        }
    }

    @Override
    public void drawForeground(PoseStack pose, float partialTicks) {
        double scissorX = positionLedgerIconStart.getX();
        double scissorY = positionLedgerIconStart.getY();
        double scissorWidth = interpWidth - 8;
        double scissorHeight = interpHeight - 8;
        try (AutoGlScissor a = GuiUtil.scissor(scissorX, scissorY, scissorWidth, scissorHeight)) {

            for (IGuiElement element : closedElements) {
                element.drawForeground(pose, partialTicks);
            }
            if (shouldDrawOpen()) {
                for (IGuiElement element : openElements) {
                    element.drawForeground(pose, partialTicks);
                }
            }
        }
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
            boolean nowOpen = false;
            if (currentDifference == 1) {
                currentDifference = -1;
            } else {
                currentDifference = 1;
                nowOpen = true;
            }
            if (isOpenProperty != null) {
                isOpenProperty.set(nowOpen);
            }
        }
    }

    @Override
    public void onMouseDragged(int button, double finalX, double finalY) {
        for (IGuiElement elem : openElements) {
            if (elem instanceof IInteractionElement) {
                ((IInteractionElement) elem).onMouseDragged(button, finalX, finalY);
            }
        }
        for (IGuiElement elem : closedElements) {
            if (elem instanceof IInteractionElement) {
                ((IInteractionElement) elem).onMouseDragged(button, finalX, finalY);
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

    protected void drawIcon(PoseStack pose, double x, double y) {

    }

    @Override
    public double getX() {
        return positionLedgerStart.getX();
    }

    @Override
    public double getY() {
        return positionLedgerStart.getY();
    }

    @Override
    public double getWidth() {
        float partialTicks = gui.getLastPartialTicks();
        if (lastWidth == currentWidth) return currentWidth;
        else if (partialTicks <= 0) return lastWidth;
        else if (partialTicks >= 1) return currentWidth;
        else return lastWidth * (1 - partialTicks) + currentWidth * partialTicks;
    }

    @Override
    public double getHeight() {
        float partialTicks = gui.getLastPartialTicks();
        if (lastHeight == currentHeight) return currentHeight;
        else if (partialTicks <= 0) return lastHeight;
        else if (partialTicks >= 1) return currentHeight;
        else return lastHeight * (1 - partialTicks) + currentHeight * partialTicks;
    }

    public Component getTitle() {
        return title;
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
            if (contains(gui.mouse)) {
                tooltips.add(new ToolTip(getTitle()));
            }
        }
    }

    @Override
    public void addHelpElements(List<HelpPosition> elements) {
        for (IGuiElement element : closedElements) {
            element.addHelpElements(elements);
        }
        if (currentWidth == maxWidth && currentHeight == maxHeight) {
            for (IGuiElement element : openElements) {
                element.addHelpElements(elements);
            }
        }
    }
}
