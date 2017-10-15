/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Supplier;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.client.config.GuiUtils;

import buildcraft.api.core.render.ISprite;

import buildcraft.lib.client.render.fluid.FluidRenderer;
import buildcraft.lib.client.sprite.SpriteNineSliced;
import buildcraft.lib.client.sprite.SubSprite;
import buildcraft.lib.fluid.Tank;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.pos.IGuiPosition;

public class GuiUtil {

    private static final Deque<GuiRectangle> scissorRegions = new ArrayDeque<>();

    public static ToolTip createToolTip(GuiBC8<?> gui, Supplier<ItemStack> stackRef) {
        return new ToolTip() {
            @Override
            public void refresh() {
                delegate().clear();
                ItemStack stack = stackRef.get();
                if (!stack.isEmpty()) {
                    EntityPlayer player = gui.container.player;
                    boolean advanced = gui.mc.gameSettings.advancedItemTooltips;
                    delegate().addAll(stack.getTooltip(player, advanced));
                }
            }
        };
    }

    /** Draws multiple elements, one after each other. */
    public static <D> void drawVerticallyAppending(IGuiPosition element, Iterable<? extends D> iterable,
        IVerticalAppendingDrawer<D> drawer) {
        double x = element.getX();
        double y = element.getY();
        for (D drawable : iterable) {
            y += drawer.draw(drawable, x, y);
        }
    }

    public static void drawItemStackAt(ItemStack stack, int x, int y) {
        RenderHelper.enableGUIStandardItemLighting();
        Minecraft mc = Minecraft.getMinecraft();
        RenderItem itemRender = mc.getRenderItem();
        itemRender.renderItemAndEffectIntoGUI(mc.player, stack, x, y);
        itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, stack, x, y, null);
        RenderHelper.disableStandardItemLighting();
    }

    @FunctionalInterface
    public interface IVerticalAppendingDrawer<D> {
        double draw(D drawable, double x, double y);
    }

    /** Straight copy of {@link GuiUtils#drawHoveringText(List, int, int, int, int, int, FontRenderer)}, except that we
     * return the size of the box that was drawn. Draws a tooltip box on the screen with text in it. Automatically
     * positions the box relative to the mouse to match Mojang's implementation. Automatically wraps text when there is
     * not enough space on the screen to display the text without wrapping. Can have a maximum width set to avoid
     * creating very wide tooltips.
     *
     * @param textLines the lines of text to be drawn in a hovering tooltip box.
     * @param mouseX the mouse X position
     * @param mouseY the mouse Y position
     * @param screenWidth the available screen width for the tooltip to drawn in
     * @param screenHeight the available screen height for the tooltip to drawn in
     * @param maxTextWidth the maximum width of the text in the tooltip box. Set to a negative number to have no max
     *            width.
     * @param font the font for drawing the text in the tooltip box */
    public static int drawHoveringText(List<String> textLines, final int mouseX, final int mouseY,
        final int screenWidth, final int screenHeight, final int maxTextWidth, FontRenderer font) {
        if (!textLines.isEmpty()) {
            GlStateManager.disableRescaleNormal();
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            int tooltipTextWidth = 0;

            for (String textLine : textLines) {
                int textLineWidth = font.getStringWidth(textLine);

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
                List<String> wrappedTextLines = new ArrayList<>();
                for (int i = 0; i < textLines.size(); i++) {
                    String textLine = textLines.get(i);
                    List<String> wrappedLine = font.listFormattedStringToWidth(textLine, tooltipTextWidth);
                    if (i == 0) {
                        titleLinesCount = wrappedLine.size();
                    }

                    for (String line : wrappedLine) {
                        int lineWidth = font.getStringWidth(line);
                        if (lineWidth > wrappedTooltipWidth) {
                            wrappedTooltipWidth = lineWidth;
                        }
                        wrappedTextLines.add(line);
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

            final int zLevel = 300;
            final int backgroundColor = 0xF0100010;
            GuiUtils.drawGradientRect(zLevel, tooltipX - 3, tooltipY - 4, tooltipX + tooltipTextWidth + 3, tooltipY - 3,
                backgroundColor, backgroundColor);
            GuiUtils.drawGradientRect(zLevel, tooltipX - 3, tooltipY + tooltipHeight + 3,
                tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 4, backgroundColor, backgroundColor);
            GuiUtils.drawGradientRect(zLevel, tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3,
                tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
            GuiUtils.drawGradientRect(zLevel, tooltipX - 4, tooltipY - 3, tooltipX - 3, tooltipY + tooltipHeight + 3,
                backgroundColor, backgroundColor);
            GuiUtils.drawGradientRect(zLevel, tooltipX + tooltipTextWidth + 3, tooltipY - 3,
                tooltipX + tooltipTextWidth + 4, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
            final int borderColorStart = 0x505000FF;
            final int borderColorEnd = (borderColorStart & 0xFEFEFE) >> 1 | borderColorStart & 0xFF000000;
            GuiUtils.drawGradientRect(zLevel, tooltipX - 3, tooltipY - 3 + 1, tooltipX - 3 + 1,
                tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd);
            GuiUtils.drawGradientRect(zLevel, tooltipX + tooltipTextWidth + 2, tooltipY - 3 + 1,
                tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd);
            GuiUtils.drawGradientRect(zLevel, tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3,
                tooltipY - 3 + 1, borderColorStart, borderColorStart);
            GuiUtils.drawGradientRect(zLevel, tooltipX - 3, tooltipY + tooltipHeight + 2,
                tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3, borderColorEnd, borderColorEnd);

            for (int lineNumber = 0; lineNumber < textLines.size(); ++lineNumber) {
                String line = textLines.get(lineNumber);
                font.drawStringWithShadow(line, tooltipX, tooltipY, -1);

                if (lineNumber + 1 == titleLinesCount) {
                    tooltipY += 2;
                }

                tooltipY += 10;
            }

            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
            RenderHelper.enableStandardItemLighting();
            GlStateManager.enableRescaleNormal();
            return tooltipHeight + 5;
        }
        return 0;
    }

    public static void drawFluid(IGuiArea position, Tank tank) {
        drawFluid(position, tank.getFluidForRender(), tank.getCapacity());
    }

    public static void drawFluid(IGuiArea position, FluidStack fluid, int capacity) {
        if (fluid == null || fluid.amount <= 0) return;
        drawFluid(position, fluid, fluid.amount, capacity);
    }

    public static void drawFluid(IGuiArea position, FluidStack fluid, int amount, int capacity) {
        if (fluid == null || amount <= 0) return;

        double height = amount * position.getHeight() / capacity;

        double startX = position.getX();
        double startY;
        double endX = startX + position.getWidth();
        double endY;

        if (fluid.getFluid().isGaseous(fluid)) {
            startY = position.getY() + height;
            endY = position.getY();
        } else {
            startY = position.getEndY();
            endY = startY - height;
        }

        FluidRenderer.drawFluidForGui(fluid, startX, startY, endX, endY);
    }

    public static AutoGlScissor scissor(double x, double y, double width, double height) {
        return scissor(new GuiRectangle(x, y, width, height));
    }

    public static AutoGlScissor scissor(IGuiArea area) {
        GuiRectangle rect = area.asImmutable();
        if (scissorRegions.isEmpty()) {
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
        }
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
                    GL11.glDisable(GL11.GL_SCISSOR_TEST);
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
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution res = new ScaledResolution(mc);
        double scaleW = mc.displayWidth / res.getScaledWidth_double();
        double scaleH = mc.displayHeight / res.getScaledHeight_double();
        int rx = (int) (x * scaleW);
        int ry = (int) (mc.displayHeight - (y + height) * scaleH);
        GL11.glScissor(rx, ry, (int) (width * scaleW), (int) (height * scaleH));
    }

    public static ISprite subRelative(ISprite sprite, double u, double v, double width, double height, double size) {
        return GuiUtil.subRelative(sprite, u / size, v / size, width / size, height / size);
    }

    public static ISprite subAbsolute(ISprite sprite, double uMin, double vMin, double uMax, double vMax,
        double spriteSize) {
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

    public static SpriteNineSliced slice(ISprite sprite, double uMin, double vMin, double uMax, double vMax,
        double scale) {
        return new SpriteNineSliced(sprite, uMin, vMin, uMax, vMax, scale);
    }

    /** A type of {@link AutoCloseable} that will pop off the current {@link GL11#glScissor(int, int, int, int)}. */
    public interface AutoGlScissor extends AutoCloseable {
        @Override
        void close();
    }
}
