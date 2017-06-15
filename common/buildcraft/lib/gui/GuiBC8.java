/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

import buildcraft.lib.BCLibSprites;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.ledger.LedgerHelp;
import buildcraft.lib.gui.ledger.LedgerManager_Neptune;
import buildcraft.lib.gui.ledger.LedgerOwnership;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.gui.pos.MousePosition;
import buildcraft.lib.gui.pos.PositionCallable;
import buildcraft.lib.misc.GuiUtil;

public abstract class GuiBC8<C extends ContainerBC_Neptune> extends GuiContainer {
    /** Used to control if this gui should show debugging lines, and other oddities that help development. */
    public static boolean debugging = false;

    public static final GuiSpriteScaled SPRITE_DEBUG = new GuiSpriteScaled(BCLibSprites.DEBUG, 16, 16);

    public final C container;
    public final MousePosition mouse = new MousePosition();
    public final RootPosition rootElement = new RootPosition(this);

    /** All of the {@link IGuiElement} which will be drawn by this gui. */
    public final List<IGuiElement> shownElements = new ArrayList<>();
    /** @deprecated As this can be done through json with parents, rather than with a specialised manager */
    public final LedgerManager_Neptune ledgersLeft, ledgersRight;
    protected final LedgerHelp ledgerHelp;
    private float lastPartialTicks;

    public GuiBC8(C container) {
        super(container);
        this.container = container;
        ledgersLeft = new LedgerManager_Neptune(this, rootElement.offset(0, 5), false);
        IGuiPosition rightPos = rootElement.getPosition(1, -1).offset(0, 5);
        ledgersRight = new LedgerManager_Neptune(this, rightPos, true);

        if (container instanceof ContainerBCTile<?>) {
            ledgersRight.ledgers.add(new LedgerOwnership(ledgersRight, (ContainerBCTile<?>) container));
        }
        if (shouldAddHelpLedger()) {
            ledgersLeft.ledgers.add(ledgerHelp = new LedgerHelp(ledgersLeft));
        } else {
            ledgerHelp = null;
        }
    }

    /** Checks to see if the main */
    protected boolean shouldAddHelpLedger() {
        return true;
    }

    @Override
    public void initGui() {
        super.initGui();
    }

    // Protected -> Public

    @Override
    public void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
        super.drawGradientRect(left, top, right, bottom, startColor, endColor);
    }

    public List<GuiButton> getButtonList() {
        return buttonList;
    }

    public FontRenderer getFontRenderer() {
        return fontRenderer;
    }

    public float getLastPartialTicks() {
        return lastPartialTicks;
    }

    // Other

    public List<IGuiElement> getElementsAt(int x, int y) {
        List<IGuiElement> elements = new ArrayList<>();
        for (IGuiElement elem : shownElements) {
            elements.addAll(elem.getThisAndChildrenAt(x, y));
        }
        return elements;
    }

    public void drawItemStackAt(ItemStack stack, int x, int y) {
        RenderHelper.enableGUIStandardItemLighting();
        itemRender.renderItemAndEffectIntoGUI(mc.player, stack, x, y);
        itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, stack, x, y, null);
        RenderHelper.disableStandardItemLighting();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        ledgersLeft.update();
        ledgersRight.update();
    }

    @Override
    protected final void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1, 1, 1, 1);
        SPRITE_DEBUG.drawAt(0, 0);
        if (debugging) {
            drawRect(0, 0, 16, 16, 0x33_FF_FF_FF);

            // draw the outer resizing edges
            int w = 320;
            int h = 240;

            int sx = (width - w) / 2;
            int sy = (height - h) / 2;
            int ex = sx + w + 1;
            int ey = sy + h + 1;
            sx--;
            sy--;

            drawRect(sx, sy, ex + 1, sy + 1, -1);
            drawRect(sx, ey, ex + 1, ey + 1, -1);

            drawRect(sx, sy, sx + 1, ey + 1, -1);
            drawRect(ex, sy, ex + 1, ey + 1, -1);
        }

        RenderHelper.disableStandardItemLighting();
        this.lastPartialTicks = partialTicks;
        mouse.setMousePosition(mouseX, mouseY);

        drawBackgroundLayer(partialTicks);

        for (IGuiElement element : shownElements) {
            element.drawBackground(partialTicks);
        }

        ledgersLeft.drawBackground(partialTicks);
        ledgersRight.drawBackground(partialTicks);
    }

    @Override
    protected final void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        GlStateManager.translate(-guiLeft, -guiTop, 0);
        mouse.setMousePosition(mouseX, mouseY);

        drawForegroundLayer();

        for (IGuiElement element : shownElements) {
            element.drawForeground(lastPartialTicks);
        }

        ledgersLeft.drawForeground(lastPartialTicks);
        ledgersRight.drawForeground(lastPartialTicks);

        drawTooltips();

        GlStateManager.translate(guiLeft, guiTop, 0);
    }

    private void drawTooltips() {
        List<ToolTip> tooltips = new ArrayList<>();

        // iterateAllElements(e -> e.addToolTips(tooltips));

        if (this instanceof ITooltipElement) {
            ((ITooltipElement) this).addToolTips(tooltips);
        }
        for (IGuiElement elem : shownElements) {
            elem.addToolTips(tooltips);
        }
        ledgersLeft.addToolTips(tooltips);
        ledgersRight.addToolTips(tooltips);
        for (GuiButton button : getButtonList()) {
            if (button instanceof ITooltipElement) {
                ((ITooltipElement) button).addToolTips(tooltips);
            }
        }
        GuiUtil.drawVerticallyAppending(mouse, tooltips, this::drawTooltip);
    }

    private int drawTooltip(ToolTip tooltip, int x, int y) {
        return 4 + GuiUtil.drawHoveringText(tooltip, x, y, width, height, -1, mc.fontRenderer);
    }

    public void drawProgress(GuiRectangle rect, GuiIcon icon, double widthPercent, double heightPercent) {
        int nWidth = MathHelper.ceil(rect.width * Math.abs(widthPercent));
        int nHeight = MathHelper.ceil(rect.height * Math.abs(heightPercent));
        icon.offset(widthPercent > 0 ? 0 : rect.width - nWidth, heightPercent > 0 ? 0 : rect.height - nHeight)
            .drawCutInside(new GuiRectangle(widthPercent > 0 ? rect.x : rect.x + (rect.width - nWidth),
                heightPercent > 0 ? rect.y : rect.y + (rect.height - nHeight), nWidth, nHeight).offset(rootElement));
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        mouse.setMousePosition(mouseX, mouseY);

        GuiRectangle debugRect = new GuiRectangle(0, 0, 16, 16);
        if (debugRect.contains(mouse)) {
            debugging = !debugging;
        }

        for (IGuiElement element : shownElements) {
            if (element instanceof IInteractionElement) {
                ((IInteractionElement) element).onMouseClicked(mouseButton);
            }
        }

        ledgersLeft.onMouseClicked(mouseButton);
        ledgersRight.onMouseClicked(mouseButton);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);

        mouse.setMousePosition(mouseX, mouseY);

        for (IGuiElement element : shownElements) {
            if (element instanceof IInteractionElement) {
                ((IInteractionElement) element).onMouseDragged(clickedMouseButton, timeSinceLastClick);
            }
        }

        ledgersLeft.onMouseDragged(clickedMouseButton, timeSinceLastClick);
        ledgersRight.onMouseDragged(clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);

        mouse.setMousePosition(mouseX, mouseY);

        for (IGuiElement element : shownElements) {
            if (element instanceof IInteractionElement) {
                ((IInteractionElement) element).onMouseReleased(state);
            }
        }

        ledgersLeft.onMouseReleased(state);
        ledgersRight.onMouseReleased(state);
    }

    protected void drawBackgroundLayer(float partialTicks) {}

    protected void drawForegroundLayer() {}

    public static final class RootPosition implements IGuiArea {
        public final GuiBC8<?> gui;

        public RootPosition(GuiBC8<?> gui) {
            this.gui = gui;
        }

        @Override
        public int getX() {
            return gui.guiLeft;
        }

        @Override
        public int getY() {
            return gui.guiTop;
        }

        @Override
        public int getWidth() {
            return gui.xSize;
        }

        @Override
        public int getHeight() {
            return gui.ySize;
        }
    }

    /** The layer of a GUI defines which part of the gu we are working in. If the current layer is NORMAL then nothing
     * is different, however for every menu that opens the previous layers are drawn with a darker overlay */
    public enum Layer {
        NORMAL,
        MENU_1,
        MENU_2;
    }
}
