/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.render.ISprite;
import buildcraft.lib.client.guide.font.IFontRenderer;
import buildcraft.lib.client.render.fluid.FluidRenderer;
import buildcraft.lib.client.sprite.SpriteNineSliced;
import buildcraft.lib.client.sprite.SubSprite;
import buildcraft.lib.expression.api.IConstantNode;
import buildcraft.lib.fluid.Tank;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.pos.IGuiPosition;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

public class GuiUtil {

    public static final IGuiArea AREA_WHOLE_SCREEN;
    private static final Deque<GuiRectangle> scissorRegions = new ArrayDeque<>();

    static {
        AREA_WHOLE_SCREEN = IGuiArea.create(() -> 0, () -> 0, GuiUtil::getScreenWidth, GuiUtil::getScreenHeight);
    }

    /**
     * @return The relative screen width. (Relative - changes with both the window size and the game setting "gui
     * scale".)
     */
    public static int getScreenWidth() {
        return Minecraft.getInstance().screen.width;
    }

    /**
     * @return The relative screen height. (Relative - changes with both the window size and the game setting "gui
     * scale".)
     */
    public static int getScreenHeight() {
//        return Minecraft.getInstance().currentScreen.height;
        return Minecraft.getInstance().screen.height;
    }

    public static IGuiArea moveRectangleToCentre(GuiRectangle area) {
        final double w = area.width;
        final double h = area.height;

        DoubleSupplier posX = () -> (AREA_WHOLE_SCREEN.getWidth() - w) / 2;
        DoubleSupplier posY = () -> (AREA_WHOLE_SCREEN.getHeight() - h) / 2;

        IGuiPosition position = IGuiPosition.create(posX, posY);
        return IGuiArea.create(position, area.width, area.height);
    }

    public static IGuiArea moveAreaToCentre(IGuiArea area) {
        if (area instanceof GuiRectangle || area instanceof IConstantNode) {
            return moveRectangleToCentre(area.asImmutable());
        }

        DoubleSupplier posX = () -> (AREA_WHOLE_SCREEN.getWidth() - area.getWidth()) / 2;
        DoubleSupplier posY = () -> (AREA_WHOLE_SCREEN.getHeight() - area.getHeight()) / 2;

        return IGuiArea.create(posX, posY, area::getWidth, area::getHeight);
    }

    public static ToolTip createToolTip(Supplier<ItemStack> stackRef) {
        return new ToolTip() {
            @Override
            public void refresh() {
                delegate().clear();
                ItemStack stack = stackRef.get();
                if (!stack.isEmpty()) {
                    delegate().addAll(GuiUtil.getFormattedTooltip(stack));
                }
            }
        };
    }

    /**
     * Draws multiple elements, one after each other.
     */
    public static <D> void drawVerticallyAppending(IGuiPosition element, Iterable<? extends D> iterable, IVerticalAppendingDrawer<D> drawer, GuiGraphics guiGraphics) {
        double x = element.getX();
        double y = element.getY();
        for (D drawable : iterable) {
            y += drawer.draw(drawable, guiGraphics, x, y);
        }
    }

    public static void drawItemStackAt(ItemStack stack, GuiGraphics guiGraphics, int x, int y) {
//        RenderHelper.enableGUIStandardItemLighting();
        RenderUtil.enableGUIStandardItemLighting();
        Minecraft mc = Minecraft.getInstance();
//        ItemRenderer itemRender = mc.getItemRenderer();
//        itemRender.renderItemAndEffectIntoGUI(mc.player, stack, x, y);
        guiGraphics.renderFakeItem(stack, x, y);
//        itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, stack, x, y, null);
        guiGraphics.renderItemDecorations(mc.font, stack, x, y, null);
//        RenderHelper.disableStandardItemLighting();
        RenderUtil.disableStandardItemLighting();
    }

    @FunctionalInterface
    public interface IVerticalAppendingDrawer<D> {
        // double draw(D drawable, double x, double y);
        double draw(D drawable, GuiGraphics guiGraphics, double x, double y);
    }

    /**
     * Straight copy of 1.12.2 {@link GuiUtils#drawHoveringText(List, int, int, int, int, int, Font)}, except that we
     * return the height of the box that was drawn. Draws a tooltip box on the screen with text in it. Automatically
     * positions the box relative to the mouse to match Mojang's implementation. Automatically wraps text when there is
     * not enough space on the screen to display the text without wrapping. Can have a maximum width set to avoid
     * creating very wide tooltips.
     *
     * @param textLines    the lines of text to be drawn in a hovering tooltip box.
     * @param mouseX       the mouse X position
     * @param mouseY       the mouse Y position
     * @param screenWidth  the available screen width for the tooltip to drawn in
     * @param screenHeight the available screen height for the tooltip to drawn in
     * @param maxTextWidth the maximum width of the text in the tooltip box. Set to a negative number to have no max
     *                     width.
     * @param font         the font for drawing the text in the tooltip box
     */
//    public static int drawHoveringText(List<String> textLines, final int mouseX, final int mouseY,
    public static int drawHoveringText(GuiGraphics guiGraphics, List<Component> textLines, final int mouseX, final int mouseY, final int screenWidth, final int screenHeight, final int maxTextWidth, Font font) {
        PoseStack poseStack = guiGraphics.pose();
        if (!textLines.isEmpty()) {
//            GlStateManager.disableRescaleNormal();
//            RenderHelper.disableStandardItemLighting();
//            GlStateManager.disableLighting();
            // Calen: not need to disableDepth, ScreenUtils.drawGradientRect will enableDepthTest
//            GlStateManager.disableDepth();
            int tooltipTextWidth = 0;

            for (Component textLine : textLines) {
                int textLineWidth = font.width(textLine);

                if (textLineWidth > tooltipTextWidth) {
                    tooltipTextWidth = textLineWidth;
                }
            }

            boolean needsWrap = false;

            int titleLinesCount = 1;
            int tooltipX = mouseX + 12;
            if (tooltipX + tooltipTextWidth + 4 > screenWidth) {
                tooltipX = mouseX - 16 - tooltipTextWidth;
                if (tooltipX < 4) // if the tooltip doesn't fit on the screen
                {
                    if (mouseX > screenWidth / 2) {
                        tooltipTextWidth = mouseX - 12 - 8;
                    } else {
                        tooltipTextWidth = screenWidth - 16 - mouseX;
                    }
                    needsWrap = true;
                }
            }

            if (maxTextWidth > 0 && tooltipTextWidth > maxTextWidth) {
                tooltipTextWidth = maxTextWidth;
                needsWrap = true;
            }

            if (needsWrap) {
                int wrappedTooltipWidth = 0;
                List<Component> wrappedTextLines = new ArrayList<>();
                for (int i = 0; i < textLines.size(); i++) {
                    Component textLine = textLines.get(i);
//                    List<String> wrappedLine = font.listFormattedStringToWidth(textLine, tooltipTextWidth);
                    List<String> wrappedLine = FontUtil.listFormattedStringToWidth(textLine.getString(), tooltipTextWidth);
                    if (i == 0) {
                        titleLinesCount = wrappedLine.size();
                    }

                    for (String line : wrappedLine) {
                        int lineWidth = font.width(line);
                        if (lineWidth > wrappedTooltipWidth) {
                            wrappedTooltipWidth = lineWidth;
                        }
                        wrappedTextLines.add(Component.literal(line));
                    }
                }
                tooltipTextWidth = wrappedTooltipWidth;
                textLines = wrappedTextLines;

                if (mouseX > screenWidth / 2) {
                    tooltipX = mouseX - 16 - tooltipTextWidth;
                } else {
                    tooltipX = mouseX + 12;
                }
            }

            int tooltipY = mouseY - 12;
            int tooltipHeight = 8;

            if (textLines.size() > 1) {
                tooltipHeight += (textLines.size() - 1) * 10;
                if (textLines.size() > titleLinesCount) {
                    tooltipHeight += 2; // gap between title lines and next lines
                }
            }

            if (tooltipY + tooltipHeight + 6 > screenHeight) {
                tooltipY = screenHeight - tooltipHeight - 6;
            }

            // Calen: to render above number text of itemStacks and other gui elements
            // the GuiElementStatementVariant background height is 1000 in GuiElementStatementVariant#drawBackground
            // the mc default tooltip height is 400
//            final int zLevel = 300;
//            final int zLevel = 400;
            final int zLevel = 1400;
            final int backgroundColor = 0xF0100010;
            // 1.18.2 GuiUtils.drawGradientRect(Matrix4f mat, int zLevel, int left, int top, int right, int bottom, int startColor, int endColor)
            // 1.20.1 GuiGraphics.fillGradient(int x1, int y1, int x2, int y2, int z, int colorFrom, int colorTo)
            guiGraphics.fillGradient(tooltipX - 3, tooltipY - 4,
                    tooltipX + tooltipTextWidth + 3, tooltipY - 3,
                    zLevel,
                    backgroundColor, backgroundColor);
            guiGraphics.fillGradient(tooltipX - 3, tooltipY + tooltipHeight + 3,
                    tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 4,
                    zLevel,
                    backgroundColor, backgroundColor);
            guiGraphics.fillGradient(tooltipX - 3, tooltipY - 3,
                    tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3,
                    zLevel,
                    backgroundColor, backgroundColor);
            guiGraphics.fillGradient(tooltipX - 4, tooltipY - 3,
                    tooltipX - 3, tooltipY + tooltipHeight + 3,
                    zLevel,
                    backgroundColor, backgroundColor);
            guiGraphics.fillGradient(tooltipX + tooltipTextWidth + 3, tooltipY - 3,
                    tooltipX + tooltipTextWidth + 4, tooltipY + tooltipHeight + 3,
                    zLevel,
                    backgroundColor, backgroundColor);
            final int borderColorStart = 0x505000FF;
            final int borderColorEnd = (borderColorStart & 0xFEFEFE) >> 1 | borderColorStart & 0xFF000000;
            guiGraphics.fillGradient(tooltipX - 3, tooltipY - 3 + 1,
                    tooltipX - 3 + 1, tooltipY + tooltipHeight + 3 - 1,
                    zLevel,
                    borderColorStart, borderColorEnd);
            guiGraphics.fillGradient(tooltipX + tooltipTextWidth + 2, tooltipY - 3 + 1,
                    tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3 - 1,
                    zLevel,
                    borderColorStart, borderColorEnd);
            guiGraphics.fillGradient(tooltipX - 3, tooltipY - 3,
                    tooltipX + tooltipTextWidth + 3, tooltipY - 3 + 1,
                    zLevel,
                    borderColorStart, borderColorStart);
            guiGraphics.fillGradient(tooltipX - 3, tooltipY + tooltipHeight + 2,
                    tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3,
                    zLevel,
                    borderColorEnd, borderColorEnd);

            RenderSystem.disableBlend();
//            RenderSystem.enableTexture();
            for (int lineNumber = 0; lineNumber < textLines.size(); ++lineNumber) {
                Component line = textLines.get(lineNumber);
//                font.drawStringWithShadow(line, tooltipX, tooltipY, -1);
                poseStack.pushPose();
                poseStack.translate(0, 0, zLevel); // Calen: Screen.class:288
                guiGraphics.drawString(font, line, tooltipX, tooltipY, -1);
                poseStack.popPose();

                if (lineNumber + 1 == titleLinesCount) {
                    tooltipY += 2;
                }

                tooltipY += 10;
            }

//            GlStateManager.enableLighting();
            // Calen: disableDepth not called before
//            GlStateManager.enableDepth();
//            RenderHelper.enableStandardItemLighting();
//            GlStateManager.enableRescaleNormal();
            return tooltipHeight + 5;
        }
        return 0;
    }

    public static void drawHorizontalLine(GuiGraphics guiGraphics, int startX, int endX, int y, int color) {
        if (endX < startX) {
            int i = startX;
            startX = endX;
            endX = i;
        }
        guiGraphics.fill(startX, y, endX + 1, y + 1, color);
    }

    public static void drawVerticalLine(GuiGraphics guiGraphics, int x, int startY, int endY, int color) {
        if (endY < startY) {
            int i = startY;
            startY = endY;
            endY = i;
        }
        guiGraphics.fill(x, startY + 1, x + 1, endY, color);
    }

    public static void drawRect(GuiGraphics guiGraphics, IGuiArea area, int colour) {
        int xMin = (int) area.getX();
        int yMin = (int) area.getY();
        int xMax = (int) area.getEndX();
        int yMax = (int) area.getEndY();
        guiGraphics.fill(xMin, yMin, xMax, yMax, colour);
    }

    public static void drawTexturedModalRect(GuiGraphics guiGraphics, ResourceLocation texture, double posX, double posY, double textureX, double textureY, double width, double height) {
//        int x = MathHelper.floor(posX);
        int x = Mth.floor(posX);
//        int y = MathHelper.floor(posY);
        int y = Mth.floor(posY);
//        int u = MathHelper.floor(textureX);
        int u = Mth.floor(textureX);
//        int v = MathHelper.floor(textureY);
        int v = Mth.floor(textureY);
//        int w = MathHelper.floor(width);
        int w = Mth.floor(width);
//        int h = MathHelper.floor(height);
        int h = Mth.floor(height);
        Gui gui = Minecraft.getInstance().gui;
//        gui.drawTexturedModalRect(x, y, u, v, w, h);
        guiGraphics.blit(texture, x, y, u, v, w, h);
    }

    public static void drawFluid(IGuiArea position, Tank tank, GuiGraphics guiGraphics) {
        drawFluid(position, tank.getFluidForRender(), tank.getCapacity(), guiGraphics);
    }

    public static void drawFluid(IGuiArea position, FluidStack fluid, int capacity, GuiGraphics guiGraphics) {
        if (fluid == null || fluid.getAmount() <= 0) return;
        drawFluid(position, fluid, fluid.getAmount(), capacity, guiGraphics);
    }

    public static void drawFluid(IGuiArea position, FluidStack fluid, int amount, int capacity, GuiGraphics guiGraphics) {
        if (fluid == null || amount <= 0) return;

        double height = amount * position.getHeight() / capacity;

        double startX = position.getX();
        double startY;
        double endX = startX + position.getWidth();
        double endY;

//        if (fluid.getFluid().isGaseous(fluid))
        if (fluid.getRawFluid().getFluidType().isLighterThanAir()) {
            startY = position.getY() + height;
            endY = position.getY();
        } else {
            startY = position.getEndY();
            endY = startY - height;
        }

        FluidRenderer.drawFluidForGui(fluid, startX, startY, endX, endY, guiGraphics);
    }

    public static AutoGlScissor scissor(double x, double y, double width, double height) {
        return scissor(new GuiRectangle(x, y, width, height));
    }

    public static AutoGlScissor scissor(IGuiArea area) {
        GuiRectangle rect = area.asImmutable();
        // Calen: RenderSystem.enableScissor() contains _enableScissorTest()
//        if (scissorRegions.isEmpty()) {
//            GL11.glEnable(GL11.GL_SCISSOR_TEST);
//        }
        scissorRegions.push(rect);
        scissor0();
        return new AutoGlScissor() {
            @Override
            public void close() {
                GuiRectangle last = scissorRegions.pop();
                if (last != rect) {
                    throw new IllegalStateException("Popped rectangles in the wrong order!");
                }
                GuiRectangle next = scissorRegions.peek();
                if (next == null) {
//                    GL11.glDisable(GL11.GL_SCISSOR_TEST);
//                    GlStateManager._disableScissorTest();
                    RenderSystem.disableScissor();
                } else {
                    scissor0();
                }
            }
        };
    }

    private static void scissor0() {
        GuiRectangle total = null;
        for (GuiRectangle rect2 : scissorRegions) {
            if (total == null) {
                total = rect2;
                continue;
            }
            double minX = Math.max(total.x, rect2.x);
            double minY = Math.max(total.y, rect2.y);
            double maxX = Math.min(total.getEndX(), rect2.getEndX());
            double maxY = Math.min(total.getEndY(), rect2.getEndY());
            total = new GuiRectangle(minX, minY, maxX - minX, maxY - minY);
        }
        if (total == null) {
            throw new IllegalStateException("Cannot call scissor0 when there are no more regions!");
        }
        scissor0(total);
    }

    private static void scissor0(IGuiArea area) {
        scissor0(area.getX(), area.getY(), area.getWidth(), area.getHeight());
    }

    private static void scissor0(double x, double y, double width, double height) {
//        Minecraft mc = Minecraft.getMinecraft();
//        ScaledResolution res = new ScaledResolution(mc);
//        double scaleW = mc.displayWidth / res.getScaledWidth_double();
//        double scaleH = mc.displayHeight / res.getScaledHeight_double();
        Window window = Minecraft.getInstance().getWindow();
        double scaleFactor = window.getGuiScale();
//        int rx = (int) (x * scaleW);
        int rx = (int) (x * scaleFactor);
//        int ry = (int) (mc.displayHeight - (y + height) * scaleH);
        int ry = (int) (window.getHeight() - (y + height) * scaleFactor);
//        GL11.glScissor(rx, ry, (int) (width * scaleW), (int) (height * scaleH));
        RenderSystem.enableScissor(rx, ry, (int) (width * scaleFactor), (int) (height * scaleFactor));
    }

    public static ISprite subRelative(ISprite sprite, double u, double v, double width, double height, double size) {
        return GuiUtil.subRelative(sprite, u / size, v / size, width / size, height / size);
    }

    public static ISprite subAbsolute(ISprite sprite, double uMin, double vMin, double uMax, double vMax, double spriteSize) {
        double size = spriteSize;
        return GuiUtil.subAbsolute(sprite, uMin / size, vMin / size, uMax / size, vMax / size);
    }

    public static ISprite subRelative(ISprite sprite, double u, double v, double width, double height) {
        return GuiUtil.subAbsolute(sprite, u, v, u + width, v + height);
    }

    public static ISprite subAbsolute(ISprite sprite, double uMin, double vMin, double uMax, double vMax) {
        if (uMin == 0 && vMin == 0 && uMax == 1 && vMax == 1) {
            return sprite;
        }
        return new SubSprite(sprite, uMin, vMin, uMax, vMax);
    }

    public static SpriteNineSliced slice(ISprite sprite, int uMin, int vMin, int uMax, int vMax, int textureSize) {
        return new SpriteNineSliced(sprite, uMin, vMin, uMax, vMax, textureSize);
    }

    public static SpriteNineSliced slice(ISprite sprite, double uMin, double vMin, double uMax, double vMax, double scale) {
        return new SpriteNineSliced(sprite, uMin, vMin, uMax, vMax, scale);
    }

    /**
     * A type of {@link AutoCloseable} that will pop off the current {@link GL11#glScissor(int, int, int, int)}.
     */
    public interface AutoGlScissor extends AutoCloseable {
        @Override
        void close();
    }

    public static List<Component> getFormattedTooltip(ItemStack stack) {
        List<Component> list = getUnFormattedTooltip(stack);

        if (!list.isEmpty()) {
//            list.set(0, Component.literal(stack.getRarity().color.toString() + list.get(0).getString()));
            list.set(0, Component.literal(stack.getRarity().color.toString()).append(list.get(0)));
        }

        for (int i = 1; i < list.size(); ++i) {
//            list.set(i, Component.literal(ChatFormatting.GRAY.toString() + list.get(i).getString()));
            list.set(i, Component.literal(ChatFormatting.GRAY.toString()).append(list.get(i)));
        }

        return list;
    }

    public static List<Component> getUnFormattedTooltip(ItemStack stack) {
        Minecraft mc = Minecraft.getInstance();
        List<Component> list = stack.getTooltipLines(mc.player, getTooltipFlags());
        if (list.isEmpty()) {
            return Collections.singletonList(getStackDisplayName(stack));
        }
        return list;
    }

    public static Component getStackDisplayName(ItemStack stack) {
        Component name = stack.getDisplayName();
        if (name == null) {
            // Temp workaround for headcrumbs
            // TODO: Remove this after https://github.com/BuildCraft/BuildCraft/issues/4268 is fixed from their side! */
            Item item = stack.getItem();
            String info = ItemUtil.getRegistryName(item) + " " + item.getClass() + " (" + stack.serializeNBT() + ")";
            BCLog.logger.warn("[lib.guide] Found null display name! " + info);
            name = Component.literal("!!NULL stack.getDisplayName(): " + info);
        }
        return name;
    }

    private static TooltipFlag getTooltipFlags() {
        boolean adv = Minecraft.getInstance().options.advancedItemTooltips;
        return adv ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
    }

    public static WrappedTextData getWrappedTextData(String text, IFontRenderer fontRenderer, int maxWidth, boolean shadow, float scale) {
        List<String> lines = fontRenderer.wrapString(text, maxWidth, shadow, scale);
        return new WrappedTextData(fontRenderer, lines.toArray(new String[0]), shadow, scale, maxWidth,
                (int) (lines.size() * fontRenderer.getFontHeight("Ly") * scale));
    }

    public static class WrappedTextData {
        public final IFontRenderer renderer;
        public final String[] lines;
        public final float scale;
        public final boolean shadow;
        public final int width, height;

        public WrappedTextData(IFontRenderer renderer, String[] lines, boolean shadow, float scale, int width, int height) {
            this.renderer = renderer;
            this.lines = lines;
            this.shadow = shadow;
            this.scale = scale;
            this.width = width;
            this.height = height;
        }

        public void drawAt(GuiGraphics guiGraphics, int x, int y, int colour, boolean centered) {
            for (String line : lines) {
                renderer.drawString(guiGraphics, line, x, y, colour, shadow, centered, scale);
                y += renderer.getFontHeight(line) * scale;
            }
        }
    }
}
