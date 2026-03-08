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
import buildcraft.lib.misc.RenderUtil;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Function;

/** Future rename: "GuiContainerBuildCraft" */
public abstract class GuiBC8<C extends ContainerBC_Neptune<?>> extends AbstractContainerScreen<C> {
    public final BuildCraftGui mainGui;
    public final C container;

    public GuiBC8(C container, Inventory inventory, Component component) {
        this(container, g -> new BuildCraftGui(g, BuildCraftGui.createWindowedArea(g)), inventory, component);
    }

    public GuiBC8(C container, Function<GuiBC8<?>, BuildCraftGui> constructor, Inventory inventory, Component component) {
        super(container, inventory, component);
        this.container = container;
        this.mainGui = constructor.apply(this);
        standardLedgerInit();
    }

    public GuiBC8(C container, ResourceLocation jsonGuiDef, Inventory inventory, Component component) {
        super(container, inventory, component);
        this.container = container;
        BuildCraftJsonGui jsonGui = new BuildCraftJsonGui(this, BuildCraftGui.createWindowedArea(this), jsonGuiDef);
        jsonGui.properties.put("player.inventory", new InventorySlotHolder(container, container.player.getInventory()));
        this.mainGui = jsonGui;
        standardLedgerInit();
        // Force subclasses to set this themselves after calling jsonGui.load
//        xSize = 10;
        imageWidth = 10;
//        ySize = 10;
        imageHeight = 10;
    }

    // Calen:
    // when the window size changed, init() will be called
    // without mainGui.shownElements.clear(), the elements (including the tooltip) will be added again and again
    private boolean firstCallInit = true;

    @Override
    protected final void init() {
        super.init();
        // Calen FIX: in 1.12.2 widgets will be copied after window resized
        if (firstCallInit) {
            initGui();
        }
        firstCallInit = false;

        this.initWhenOpenGuiOrResizeWindow();
    }

    // Calen: default: do nothing
    protected void initGui() {

    }

    protected void initWhenOpenGuiOrResizeWindow() {

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
//    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
//        super.drawScreen(mouseX, mouseY, partialTicks);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        if (mainGui.currentMenu == null || !mainGui.currentMenu.shouldFullyOverride()) {
//            this.renderHoveredToolTip(mouseX, mouseY);
            this.renderTooltip(poseStack, mouseX, mouseY);
        }
    }

    protected boolean shouldAddHelpLedger() {
        return true;
    }

    // Protected -> Public

    public void drawGradientRect(IGuiArea area, PoseStack poseStack, int startColor, int endColor) {
        int left = (int) area.getX();
        int right = (int) area.getEndX();
        int top = (int) area.getY();
        int bottom = (int) area.getEndY();
//        drawGradientRect(left, top, right, bottom, startColor, endColor);
        fillGradient(poseStack, left, top, right, bottom, startColor, endColor);
    }

    @Override
//    public void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor)
    public void fillGradient(PoseStack poseStack, int left, int top, int right, int bottom, int startColor, int endColor) {
//        super.drawGradientRect(left, top, right, bottom, startColor, endColor);
        super.fillGradient(poseStack, left, top, right, bottom, startColor, endColor);
    }

    // public List<Button> getButtonList()
    public List<Widget> getButtonList() {
//        return buttonList;
        return renderables;
    }

    public Font getFontRenderer() {
//        return fontRenderer;
        return font;
    }

    // Gui -- double -> int

    // Calen: never used
//    public void drawTexturedModalRect(PoseStack poseStack, double posX, double posY, double textureX, double textureY, double width, double height) {
//        int x = MathHelper.floor(posX);
//        int y = MathHelper.floor(posY);
//        int u = MathHelper.floor(textureX);
//        int v = MathHelper.floor(textureY);
//        int w = MathHelper.floor(width);
//        int h = MathHelper.floor(height);
////        super_drawTexturedModalRect(poseStack, x, y, u, v, w, h, zLevel);
//        blit(poseStack, x, y, u, v, w, h);
//    }

    public void drawTexturedModalRect(PoseStack poseStack, int xCoord, int yCoord, TextureAtlasSprite textureSprite, int widthIn, int heightIn, float zLevel) {
        RenderUtil.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        Matrix4f pose = poseStack.last().pose();
        RenderSystem.setShader(GameRenderer::getPositionTexShader); // Calen: without this, the texture will not appear
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(pose, (xCoord + 0.0F), (yCoord + heightIn), zLevel).uv((textureSprite.getU0()), ((float) textureSprite.getV1())).endVertex();
        bufferbuilder.vertex(pose, (xCoord + widthIn), (yCoord + heightIn), zLevel).uv((textureSprite.getU1()), ((float) textureSprite.getV1())).endVertex();
        bufferbuilder.vertex(pose, (xCoord + widthIn), (yCoord + 0), zLevel).uv((textureSprite.getU1()), ((float) textureSprite.getV0())).endVertex();
        bufferbuilder.vertex(pose, (xCoord + 0.0F), (yCoord + 0), zLevel).uv((textureSprite.getU0()), ((float) textureSprite.getV0())).endVertex();
        tessellator.end();
    }

    public void drawString(Font fontRenderer, PoseStack poseStack, String text, double x, double y, int colour) {
        drawString(fontRenderer, poseStack, text, x, y, colour, true);
    }

    public void drawString(Font fontRenderer, PoseStack poseStack, String text, double x, double y, int colour, boolean shadow) {
//        fontRenderer.drawString(text, (float) x, (float) y, colour, shadow);
        if (shadow) {
            fontRenderer.draw(poseStack, text, (float) x, (float) y, colour);
        } else {
            fontRenderer.drawShadow(poseStack, text, (float) x, (float) y, colour);
        }
    }

    // Other

    /** @deprecated Use {@link GuiUtil#drawItemStackAt(ItemStack, PoseStack, int, int)} instead */
    @Deprecated
    public static void drawItemStackAt(ItemStack stack, PoseStack poseStack, int x, int y) {
        GuiUtil.drawItemStackAt(stack, poseStack, x, y);
    }

    @Override
//    public void updateScreen()
    public void tick() {
//        super.updateScreen();
        super.tick();
        mainGui.tick();
    }

    @Override
//    protected final void drawGuiContainerBackgroundLayer(PoseStack poseStack, float partialTicks, int mouseX, int mouseY)
    protected final void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY) {
//        mainGui.drawBackgroundLayer(partialTicks, mouseX, mouseY, this::drawDefaultBackground);
        mainGui.drawBackgroundLayer(poseStack, partialTicks, mouseX, mouseY, () -> renderBackground(poseStack));
        drawBackgroundLayer(partialTicks, poseStack);
        mainGui.drawElementBackgrounds(poseStack);
    }

    @Override
//    protected final void drawGuiContainerForegroundLayer(PoseStack poseStack, int mouseX, int mouseY)
    protected final void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        mainGui.preDrawForeground(poseStack);

        drawForegroundLayer(poseStack);
//        mainGui.drawElementForegrounds(this::drawDefaultBackground, poseStack);
        mainGui.drawElementForegrounds(() -> renderBackground(poseStack), poseStack);
        drawForegroundLayerAboveElements();

        mainGui.postDrawForeground(poseStack);
    }

    public void drawProgress(GuiRectangle rect, GuiIcon icon, PoseStack poseStack, double widthPercent, double heightPercent) {
        double nWidth = rect.width * Math.abs(widthPercent);
        double nHeight = rect.height * Math.abs(heightPercent);
        ISprite sprite = GuiUtil.subRelative(icon.sprite, 0, 0, widthPercent, heightPercent);
        double x = rect.x + mainGui.rootElement.getX();
        double y = rect.y + mainGui.rootElement.getY();
        GuiIcon.draw(sprite, poseStack, x, y, x + nWidth, y + nHeight);
    }

    // Calen
    public void drawProgressRightToLeft(GuiRectangle rect, GuiIcon icon, PoseStack poseStack, double widthPercent, double heightPercent) {
        double nWidth = rect.width * Math.abs(widthPercent);
        double nHeight = rect.height * Math.abs(heightPercent);
        ISprite sprite = GuiUtil.subRelative(icon.sprite, 1 - widthPercent, 0, widthPercent, heightPercent);
        double x = rect.x + mainGui.rootElement.getX();
        double y = rect.y + mainGui.rootElement.getY();
        GuiIcon.draw(sprite, poseStack, x + rect.width - nWidth, y, x + rect.width, y + nHeight);
    }

    @Override
//    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
//        super.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);

        mainGui.onMouseClicked(mouseX, mouseY, mouseButton);
        return true;
    }

    @Override
//    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
    public boolean mouseDragged(double mouseX, double mouseY, int clickedMouseButton, double startX, double startY) {
//        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        super.mouseDragged(mouseX, mouseY, clickedMouseButton, startX, startY);

//        mainGui.onMouseDragged(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        mainGui.onMouseDragged(mouseX, mouseY, clickedMouseButton);

        return true;
    }

    // Calen 1.18.2
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        boolean ret = super.mouseScrolled(mouseX, mouseY, delta);
        mainGui.mouseScrolled(mouseX, mouseY, delta);
        return ret;
    }

    @Override
//    protected void mouseReleased(int mouseX, int mouseY, int state)
    public boolean mouseReleased(double mouseX, double mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);

        mainGui.onMouseReleased(mouseX, mouseY, state);
        return true;
    }

    @Override
//    protected void keyTyped(char typedChar, int keyCode) throws IOException
    public boolean keyPressed(int typedChar, int keyCode, int modifiers) {
//        if (!mainGui.onKeyTyped(typedChar, keyCode))
        if (!mainGui.onKeyTyped(typedChar, keyCode, modifiers)) {
//            super.keyTyped(typedChar, keyCode);
            return super.keyPressed(typedChar, keyCode, modifiers);
        } else {
            return true;
        }
    }

    public boolean charTyped(char typedChar, int keyCode) {
        if (!mainGui.charTyped(typedChar, keyCode)) {
            return super.charTyped(typedChar, keyCode);
        } else {
            return true;
        }
    }

    protected void drawBackgroundLayer(float partialTicks, PoseStack poseStack) {
    }

    protected void drawForegroundLayer(PoseStack poseStack) {
    }

    /** Like {@link #drawForegroundLayer(PoseStack)}, but is called after all {@link IGuiElement}'s have been drawn. */
    protected void drawForegroundLayerAboveElements() {
    }
}
