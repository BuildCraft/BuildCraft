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
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

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
    }

    // Calen: default: do nothing
    protected void initGui() {

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
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
//        super.drawScreen(mouseX, mouseY, partialTicks);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        if (mainGui.currentMenu == null || !mainGui.currentMenu.shouldFullyOverride()) {
//            this.renderHoveredToolTip(mouseX, mouseY);
            this.renderTooltip(guiGraphics, mouseX, mouseY);
        }
    }

    protected boolean shouldAddHelpLedger() {
        return true;
    }

    // Protected -> Public

    public void drawGradientRect(IGuiArea area, GuiGraphics guiGraphics, int startColor, int endColor) {
        int left = (int) area.getX();
        int right = (int) area.getEndX();
        int top = (int) area.getY();
        int bottom = (int) area.getEndY();
//        drawGradientRect(left, top, right, bottom, startColor, endColor);
        guiGraphics.fillGradient(left, top, right, bottom, startColor, endColor);
    }

//    @Override
//    public void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor){
//        super.drawGradientRect(left, top, right, bottom, startColor, endColor);
//    }

    // public List<Button> getButtonList()
    public List<Renderable> getButtonList() {
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

    public void drawTexturedModalRect(GuiGraphics guiGraphics, int xCoord, int yCoord, TextureAtlasSprite textureSprite, int widthIn, int heightIn, float zLevel) {
        PoseStack poseStack = guiGraphics.pose();

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

    public void drawString(GuiGraphics guiGraphics, Font fontRenderer, String text, double x, double y, int colour) {
        drawString(guiGraphics, fontRenderer, text, x, y, colour, true);
    }

    public void drawString(GuiGraphics guiGraphics, Font fontRenderer, String text, double x, double y, int colour, boolean shadow) {
//        fontRenderer.drawString(text, (float) x, (float) y, colour, shadow);
        guiGraphics.drawString(fontRenderer, text, (float) x, (float) y, colour, shadow);
    }

    // Other

    /** @deprecated Use {@link GuiUtil#drawItemStackAt(ItemStack, GuiGraphics, int, int)} instead */
    @Deprecated
    public static void drawItemStackAt(ItemStack stack, GuiGraphics guiGraphics, int x, int y) {
        GuiUtil.drawItemStackAt(stack, guiGraphics, x, y);
    }

    @Override
//    public void updateScreen()
    public void containerTick() {
//        super.updateScreen();
        super.containerTick();
        mainGui.tick();
    }

    @Override
//    protected final void drawGuiContainerBackgroundLayer(PoseStack poseStack, float partialTicks, int mouseX, int mouseY)
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
//        mainGui.drawBackgroundLayer(partialTicks, mouseX, mouseY, this::drawDefaultBackground);
        mainGui.drawBackgroundLayer(guiGraphics, partialTicks, mouseX, mouseY, () -> renderBackground(guiGraphics));
        drawBackgroundLayer(partialTicks, guiGraphics);
        mainGui.drawElementBackgrounds(guiGraphics);
    }

    @Override
//    protected final void drawGuiContainerForegroundLayer(PoseStack poseStack, int mouseX, int mouseY)
    protected final void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        mainGui.preDrawForeground(guiGraphics.pose());

        drawForegroundLayer(guiGraphics);
//        mainGui.drawElementForegrounds(this::drawDefaultBackground, poseStack);
        mainGui.drawElementForegrounds(() -> renderBackground(guiGraphics), guiGraphics);
        drawForegroundLayerAboveElements();

        mainGui.postDrawForeground(guiGraphics.pose());
    }

    public void drawProgress(GuiRectangle rect, GuiIcon icon, GuiGraphics guiGraphics, double widthPercent, double heightPercent) {
        double nWidth = rect.width * Math.abs(widthPercent);
        double nHeight = rect.height * Math.abs(heightPercent);
        ISprite sprite = GuiUtil.subRelative(icon.sprite, 0, 0, widthPercent, heightPercent);
        double x = rect.x + mainGui.rootElement.getX();
        double y = rect.y + mainGui.rootElement.getY();
        GuiIcon.draw(sprite, guiGraphics, x, y, x + nWidth, y + nHeight);
    }

    // Calen
    public void drawProgressRightToLeft(GuiRectangle rect, GuiIcon icon, GuiGraphics guiGraphics, double widthPercent, double heightPercent) {
        double nWidth = rect.width * Math.abs(widthPercent);
        double nHeight = rect.height * Math.abs(heightPercent);
        ISprite sprite = GuiUtil.subRelative(icon.sprite, 1 - widthPercent, 0, widthPercent, heightPercent);
        double x = rect.x + mainGui.rootElement.getX();
        double y = rect.y + mainGui.rootElement.getY();
        GuiIcon.draw(sprite, guiGraphics, x + rect.width - nWidth, y, x + rect.width, y + nHeight);
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

    protected void drawBackgroundLayer(float partialTicks, GuiGraphics guiGraphics) {
    }

    protected void drawForegroundLayer(GuiGraphics guiGraphics) {
    }

    /** Like {@link #drawForegroundLayer(GuiGraphics)}, but is called after all {@link IGuiElement}'s have been drawn. */
    protected void drawForegroundLayerAboveElements() {
    }
}
