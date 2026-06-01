/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.gui;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.MinecraftClient;
import java.util.stream.Collectors;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;

import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.gui.pos.MousePosition;

/** Forge GuiContainer → Fabric HandledScreen. Manages BuildCraft GUI element trees. */
@Environment(EnvType.CLIENT)
@SuppressWarnings({ "rawtypes", "unchecked" })
public class BuildCraftGui extends HandledScreen {

    /** Area covering the full logical screen (scaled GUI coordinates). */
    public static IGuiArea createScreenArea() {
        return IGuiArea.create(
            () -> 0, () -> 0,
            () -> MinecraftClient.getInstance().getWindow().getScaledWidth(),
            () -> MinecraftClient.getInstance().getWindow().getScaledHeight());
    }

    /** Area matching the windowed background panel of this HandledScreen. */
    public static IGuiArea createWindowedArea(BuildCraftGui gui) {
        return IGuiArea.create(
            () -> (double) gui.x, () -> (double) gui.y,
            () -> (double) gui.backgroundWidth, () -> (double) gui.backgroundHeight);
    }

    public final IGuiArea screenElement = createScreenArea();
    public final IGuiArea rootElement;
    public final MousePosition mouse = new MousePosition();
    /** Self-reference used by legacy BC GUI subclasses that reference 'mainGui' instead of 'this'. */
    public final BuildCraftGui mainGui = this;
    /** Forge-compat: alias for {@link #textRenderer}. */
    protected net.minecraft.client.font.TextRenderer fontRenderer;
    /** Forge-compat: reference to MinecraftClient. */
    protected MinecraftClient mc;

    public final List<IGuiElement> shownElements = new ArrayList<>();
    public IMenuElement currentMenu;

    public IGuiPosition lowerLeftLedgerPos, lowerRightLedgerPos;
    private float lastPartialTicks;

    /** Forge-compat: maps to {@link #backgroundWidth}. Setting this also updates backgroundWidth. */
    protected int xSize = 176;
    /** Forge-compat: maps to {@link #backgroundHeight}. Setting this also updates backgroundHeight. */
    protected int ySize = 166;

    public BuildCraftGui(ScreenHandler handler, PlayerInventory inv, Text title) {
        super(handler, inv, title);
        this.rootElement = createWindowedArea(this);
        lowerLeftLedgerPos = rootElement.offset(0, 5);
        lowerRightLedgerPos = rootElement.getPosition(1, -1).offset(0, 5);
    }

    public final float getLastPartialTicks() { return lastPartialTicks; }

    @Override
    protected void init() {
        // Sync xSize/ySize (Forge compat) to HandledScreen's backgroundWidth/backgroundHeight
        backgroundWidth = xSize;
        backgroundHeight = ySize;
        // Sync compat fields
        mc = MinecraftClient.getInstance();
        fontRenderer = mc.textRenderer;
        super.init();
    }

    // ------------------------------------------------------------------ tick
    // Note: HandledScreen.tick() is final — BC element ticking happens inside render().

    protected void tickElements() {
        if (currentMenu != null) currentMenu.tick();
        for (IGuiElement element : shownElements) element.tick();
    }

    // ----------------------------------------------------------------- render

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.lastPartialTicks = delta;
        mouse.setMousePosition(mouseX, mouseY);
        tickElements();
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        drawElementBackgrounds(context, delta);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        drawElementForegrounds(context, lastPartialTicks);
    }

    public void drawElementBackgrounds(DrawContext context, float partialTicks) {
        for (IGuiElement element : shownElements) {
            if (element != currentMenu) element.drawBackground(context, partialTicks);
        }
    }

    public void drawElementForegrounds(DrawContext context, float partialTicks) {
        for (IGuiElement element : shownElements) {
            if (element != currentMenu) element.drawForeground(context, partialTicks);
        }
        IMenuElement m = currentMenu;
        if (m != null) {
            m.drawBackground(context, partialTicks);
            m.drawForeground(context, partialTicks);
        }
        drawTooltips(context);
    }

    private void drawTooltips(DrawContext context) {
        List<ToolTip> tooltips = new ArrayList<>();
        IMenuElement m = currentMenu;
        if (m != null) {
            m.addToolTips(tooltips);
            if (m.shouldFullyOverride()) {
                renderFirstTooltip(context, tooltips);
                return;
            }
        }
        if (this instanceof ITooltipElement) {
            ((ITooltipElement) this).addToolTips(tooltips);
        }
        for (IGuiElement elem : shownElements) elem.addToolTips(tooltips);
        renderFirstTooltip(context, tooltips);
    }

    private void renderFirstTooltip(DrawContext context, List<ToolTip> tooltips) {
        for (ToolTip tip : tooltips) {
            if (tip.isReady() && !tip.isEmpty()) {
                List<Text> textLines = tip.stream()
                    .map(Text::literal)
                    .collect(Collectors.toList());
                context.drawTooltip(textRenderer, textLines, (int) mouse.getX(), (int) mouse.getY());
                return;
            }
        }
    }

    // ---------------------------------------------------------------- inputs

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        mouse.setMousePosition(mouseX, mouseY);
        IMenuElement m = currentMenu;
        if (m != null) {
            m.onMouseClicked(button);
            if (m.shouldFullyOverride()) return true;
        }
        for (IGuiElement element : shownElements) {
            if (element instanceof IInteractionElement) {
                ((IInteractionElement) element).onMouseClicked(button);
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        mouse.setMousePosition(mouseX, mouseY);
        long ticksSinceClick = 0; // Fabric doesn't track this directly
        IMenuElement m = currentMenu;
        if (m != null) {
            m.onMouseDragged(button, ticksSinceClick);
            if (m.shouldFullyOverride()) return true;
        }
        for (IGuiElement element : shownElements) {
            if (element instanceof IInteractionElement) {
                ((IInteractionElement) element).onMouseDragged(button, ticksSinceClick);
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        mouse.setMousePosition(mouseX, mouseY);
        IMenuElement m = currentMenu;
        if (m != null) {
            m.onMouseReleased(button);
            if (m.shouldFullyOverride()) return true;
        }
        for (IGuiElement element : shownElements) {
            if (element instanceof IInteractionElement) {
                ((IInteractionElement) element).onMouseReleased(button);
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean action = false;
        IMenuElement m = currentMenu;
        if (m != null) {
            action = m.onKeyPress((char) 0, keyCode);
            if (action && m.shouldFullyOverride()) return true;
        }
        for (IGuiElement element : shownElements) {
            if (element instanceof IInteractionElement) {
                action |= ((IInteractionElement) element).onKeyPress((char) 0, keyCode);
            }
        }
        return action || super.keyPressed(keyCode, scanCode, modifiers);
    }

    public List<IGuiElement> getElementsAt(double x, double y) {
        List<IGuiElement> elements = new ArrayList<>();
        IMenuElement m = currentMenu;
        if (m != null) {
            elements.addAll(m.getThisAndChildrenAt(x, y));
            if (m.shouldFullyOverride()) return elements;
        }
        for (IGuiElement elem : shownElements) {
            elements.addAll(elem.getThisAndChildrenAt(x, y));
        }
        return elements;
    }
}
