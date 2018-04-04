/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui;

import buildcraft.api.core.render.ISprite;
import buildcraft.lib.gui.json.BuildCraftJsonGui;
import buildcraft.lib.gui.json.InventorySlotHolder;
import buildcraft.lib.gui.ledger.LedgerHelp;
import buildcraft.lib.gui.ledger.LedgerOwnership;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.misc.GuiUtil;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

/** Future rename: "GuiContainerBuildCraft" */
public abstract class GuiBC8<C extends ContainerBC_Neptune> extends GuiContainer {
    public final BuildCraftGui mainGui;
    public final C container;

    public GuiBC8(C container) {
        this(container, g -> new BuildCraftGui(g, BuildCraftGui.createWindowedArea(g)));
    }

    public GuiBC8(C container, Function<GuiBC8<?>, BuildCraftGui> constructor) {
        super(container);
        this.container = container;
        this.mainGui = constructor.apply(this);
        standardLedgerInit();
    }

    public GuiBC8(C container, ResourceLocation jsonGuiDef) {
        super(container);
        this.container = container;
        BuildCraftJsonGui jsonGui = new BuildCraftJsonGui(this, BuildCraftGui.createWindowedArea(this), jsonGuiDef);
        jsonGui.properties.put("player.inventory", new InventorySlotHolder(container, container.player.inventory));
        this.mainGui = jsonGui;
        standardLedgerInit();
    }

    private final void standardLedgerInit() {
        if (container instanceof ContainerBCTile<?>) {
            mainGui.shownElements.add(new LedgerOwnership(mainGui, ((ContainerBCTile<?>) container).tile, true));
        }
        if (shouldAddHelpLedger()) {
            mainGui.shownElements.add(new LedgerHelp(mainGui, false));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        /*
        if (mainGui.currentMenu == null || !mainGui.currentMenu.shouldFullyOverride()) {
            this.renderHoveredToolTip(mouseX, mouseY);
        }*/
    }

    protected boolean shouldAddHelpLedger() {
        return true;
    }

    // Protected -> Public

    public void drawGradientRect(IGuiArea area, int startColor, int endColor) {
        int left = (int) area.getX();
        int right = (int) area.getEndX();
        int top = (int) area.getY();
        int bottom = (int) area.getEndY();
        drawGradientRect(left, top, right, bottom, startColor, endColor);
    }

    @Override
    public void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
        super.drawGradientRect(left, top, right, bottom, startColor, endColor);
    }

    public List<GuiButton> getButtonList() {
        return buttonList;
    }

    public FontRenderer getFontRenderer() {
        return fontRendererObj;
    }

    // Gui -- double -> int

    public void drawTexturedModalRect(double posX, double posY, double textureX, double textureY, double width,
        double height) {
        int x = MathHelper.floor(posX);
        int y = MathHelper.floor(posY);
        int u = MathHelper.floor(textureX);
        int v = MathHelper.floor(textureY);
        int w = MathHelper.floor(width);
        int h = MathHelper.floor(height);
        drawTexturedModalRect(x, y, u, v, w, h);
    }

    public void drawString(FontRenderer fontRenderer, String text, double x, double y, int colour) {
        drawString(fontRenderer, text, x, y, colour, true);
    }

    public void drawString(FontRenderer fontRenderer, String text, double x, double y, int colour, boolean shadow) {
        fontRenderer.drawString(text, (float) x, (float) y, colour, shadow);
    }

    // Other

    /** @deprecated Use {@link GuiUtil#drawItemStackAt(ItemStack,int,int)} instead */
    @Deprecated
    public static void drawItemStackAt(ItemStack stack, int x, int y) {
        GuiUtil.drawItemStackAt(stack, x, y);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        mainGui.tick();
    }

    @Override
    protected final void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        mainGui.drawBackgroundLayer(partialTicks, mouseX, mouseY, this::drawDefaultBackground);
        drawBackgroundLayer(partialTicks);
        mainGui.drawElementBackgrounds();
    }

    @Override
    protected final void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        mainGui.preDrawForeground();

        drawForegroundLayer();
        mainGui.drawElementForegrounds(this::drawDefaultBackground);
        drawForegroundLayerAboveElements();

        mainGui.postDrawForeground();
    }

    public void drawProgress(GuiRectangle rect, GuiIcon icon, double widthPercent, double heightPercent) {
        double nWidth = rect.width * Math.abs(widthPercent);
        double nHeight = rect.height * Math.abs(heightPercent);
        ISprite sprite = GuiUtil.subRelative(icon.sprite, 0, 0, widthPercent, heightPercent);
        double x = rect.x + mainGui.rootElement.getX();
        double y = rect.y + mainGui.rootElement.getY();
        GuiIcon.draw(sprite, x, y, x + nWidth, y + nHeight);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        mainGui.onMouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);

        mainGui.onMouseDragged(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);

        mainGui.onMouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {

        if (!mainGui.onKeyTyped(typedChar, keyCode)) {
            super.keyTyped(typedChar, keyCode);
        }
    }

    protected void drawBackgroundLayer(float partialTicks) {}

    protected void drawForegroundLayer() {}

    /** Like {@link #drawForegroundLayer()}, but is called after all {@link IGuiElement}'s have been drawn. */
    protected void drawForegroundLayerAboveElements() {}
}
