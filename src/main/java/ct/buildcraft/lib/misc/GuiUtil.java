/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.misc;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.api.core.render.ISprite;
import ct.buildcraft.lib.client.render.fluid.FluidRenderer;
import ct.buildcraft.lib.client.sprite.SpriteNineSliced;
import ct.buildcraft.lib.client.sprite.SubSprite;
import ct.buildcraft.lib.expression.api.IConstantNode;
import ct.buildcraft.lib.fluid.Tank;
import ct.buildcraft.lib.gui.elem.ToolTip;
import ct.buildcraft.lib.gui.pos.GuiRectangle;
import ct.buildcraft.lib.gui.pos.IGuiArea;
import ct.buildcraft.lib.gui.pos.IGuiPosition;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.client.gui.ScreenUtils;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

public class GuiUtil {

    public static final IGuiArea AREA_WHOLE_SCREEN;
    private static final Deque<GuiRectangle> scissorRegions = new ArrayDeque<>();
    private static final Minecraft mc = Minecraft.getInstance();

    static {
        AREA_WHOLE_SCREEN = IGuiArea.create(() -> 0, () -> 0, GuiUtil::getScreenWidth, GuiUtil::getScreenHeight);
    }

    /** @return The relative screen width. (Relative - changes with both the window size and the game setting "gui
     *         scale".) */
    public static int getScreenWidth() {
        return mc.screen.width;
    }

    /** @return The relative screen height. (Relative - changes with both the window size and the game setting "gui
     *         scale".) */
    public static int getScreenHeight() {
        return mc.screen.height;
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

    /** Draws multiple elements, one after each other. */
    public static <D> void drawVerticallyAppending(PoseStack pose, IGuiPosition element, Iterable<? extends D> iterable,
        IVerticalAppendingDrawer<D> drawer) {
        double x = element.getX();
        double y = element.getY();
        for (D drawable : iterable) {
            y += drawer.draw(pose, drawable, x, y);
        }
    }

    public static void drawItemStackAt(ItemStack stack, int x, int y) {
        Minecraft mc = Minecraft.getInstance();
        ItemRenderer itemRender = mc.getItemRenderer();
        itemRender.renderAndDecorateItem(mc.player, stack, x, y, 0);
        itemRender.renderGuiItemDecorations(mc.font, stack, x, y, null);
    }

    @FunctionalInterface
    public interface IVerticalAppendingDrawer<D> {
        double draw(PoseStack pose, D drawable, double x, double y);
    }

    /** Straight copy of {@link Screen#renderTooltipInternal(List, int, int, int, int, int, FontRenderer)}, except that we
     * return the height of the box that was drawn. Draws a tooltip box on the screen with text in it. Automatically
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
    public static int drawHoveringText(List<Component> textLines, final int mouseX, final int mouseY,
        final int screenWidth, final int screenHeight, final int maxTextWidth, Font font, PoseStack pose) {
        if (!textLines.isEmpty()) {//TODO
        	pose.translate(0, 0, 400);
        	Matrix4f matrix = pose.last().pose();
            //GlStateManager.disableRescaleNormal();
            //GlStateManager.disableLighting();
        	
            RenderSystem.disableDepthTest();
            int tooltipTextWidth = 0;
            int height = textLines.size() == 1 ? -2 : 0;

            for (Component textLine : textLines) {
                int textLineWidth = font.width(textLine);
                if (textLineWidth > tooltipTextWidth) {
                    tooltipTextWidth = textLineWidth;
                }
                
                height += 10;
            }

			int titleLinesCount = 1;
			int tooltipX = mouseX + 12;
			if (tooltipX + tooltipTextWidth > screenWidth) {
				tooltipX -= 28 + tooltipTextWidth;
			}

            int tooltipY = mouseY - 12;
            int tooltipHeight =  height;

            if (tooltipY + 1 > screenWidth) {
            	tooltipY = screenHeight - height - 6;
            }

            final int zLevel = 0;
            final int backgroundColor = 0xF0100010;
            ScreenUtils.drawGradientRect(matrix, zLevel, tooltipX - 3, tooltipY - 4, tooltipX + tooltipTextWidth + 3, tooltipY - 3,
                backgroundColor, backgroundColor);
            ScreenUtils.drawGradientRect(matrix, zLevel, tooltipX - 3, tooltipY + tooltipHeight + 3,
                tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 4, backgroundColor, backgroundColor);
            ScreenUtils.drawGradientRect(matrix, zLevel, tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3,
                tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
            ScreenUtils.drawGradientRect(matrix, zLevel, tooltipX - 4, tooltipY - 3, tooltipX - 3, tooltipY + tooltipHeight + 3,
                backgroundColor, backgroundColor);
            ScreenUtils.drawGradientRect(matrix, zLevel, tooltipX + tooltipTextWidth + 3, tooltipY - 3,
                tooltipX + tooltipTextWidth + 4, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
            final int borderColorStart = 0x505000FF;
            final int borderColorEnd = (borderColorStart & 0xFEFEFE) >> 1 | borderColorStart & 0xFF000000;
            ScreenUtils.drawGradientRect(matrix, zLevel, tooltipX - 3, tooltipY - 3 + 1, tooltipX - 3 + 1,
                tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd);
            ScreenUtils.drawGradientRect(matrix, zLevel, tooltipX + tooltipTextWidth + 2, tooltipY - 3 + 1,
                tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd);
            ScreenUtils.drawGradientRect(matrix, zLevel, tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3,
                tooltipY - 3 + 1, borderColorStart, borderColorStart);
            ScreenUtils.drawGradientRect(matrix, zLevel, tooltipX - 3, tooltipY + tooltipHeight + 2,
                tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3, borderColorEnd, borderColorEnd);

            for (int lineNumber = 0; lineNumber < textLines.size(); ++lineNumber) {
                Component line = textLines.get(lineNumber);
                font.drawShadow(pose, line, tooltipX, tooltipY, -1);

                if (lineNumber + 1 == titleLinesCount) {
                    tooltipY += 2;
                }

                tooltipY += 10;
            }

            //GlStateManager.enableLighting();
            RenderSystem.enableDepthTest();
//            GlStateManager.enableRescaleNormal();
            return tooltipHeight + 5;
        }
        return 0;
    }

    public static void drawHorizontalLine(PoseStack pose, int startX, int endX, int y, int color) {
        if (endX < startX) {
            int i = startX;
            startX = endX;
            endX = i;
        }
        RenderSystem.disableTexture();
        RenderSystem.setShaderColor(color&0xFF000000, color&0x00FF0000, color&0x0000FF00, color&0x000000FF);//TODO: check
        mc.gui.blit(pose, startX, y, endX-startX, 1, 0, 0);
        RenderSystem.enableTexture();
    }

    public static void drawVerticalLine(PoseStack pose, int x, int startY, int endY, int color) {
        if (endY < startY) {
            int i = startY;
            startY = endY;
            endY = i;
        }
        Screen.fill(pose, x, startY + 1, x + 1, endY, color);
    }

    public static void drawRect(PoseStack pose, IGuiArea area, int colour) {
        int xMin = (int) area.getX();
        int yMin = (int) area.getY();
        int xMax = (int) area.getEndX();
        int yMax = (int) area.getEndY();
        Screen.fill(pose, xMin, yMin, xMax, yMax, colour);
    }

    public static void drawTexturedModalRect(PoseStack pose, double posX, double posY, double textureX, double textureY, double width,
        double height) {
        int x = Mth.floor(posX);
        int y = Mth.floor(posY);
        int u = Mth.floor(textureX);
        int v = Mth.floor(textureY);
        int w = Mth.floor(width);
        int h = Mth.floor(height);
        Minecraft instance = Minecraft.getInstance();
		Screen gui = instance.screen;
        
        gui.blit(pose, x, y, u, v, w, h);
    }

    public static void drawFluid(PoseStack pose ,IGuiArea position, Tank tank) {
        drawFluid(pose, position, tank.getFluidForRender(), tank.getCapacity());
    }

    public static void drawFluid(PoseStack pose, IGuiArea position, FluidStack fluid, int capacity) {
        if (fluid == null || fluid.isEmpty()) return;
        drawFluid(pose, position, fluid, fluid.getAmount(), capacity);
    }

    public static void drawFluid(PoseStack pose, IGuiArea position, FluidStack fluid, int amount, int capacity) {
        if (fluid.isEmpty() || amount <= 0) return;

        double height = amount * position.getHeight() / capacity;

        double startX = position.getX();
        double startY;
        double endX = startX + position.getWidth();
        double endY;

        if (fluid.getFluid().getFluidType().isLighterThanAir()) {
            startY = position.getY() + height;
            endY = position.getY();
        } else {
            startY = position.getEndY();
            endY = startY - height;
        }

        FluidRenderer.drawFluidForGui(fluid, startX, startY, endX, endY, pose.last());
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
        Window win = mc.getWindow();
        double scaleW = win.getGuiScale();
        double scaleH = win.getGuiScale();
        int rx = (int) (x * scaleW);
        int ry = (int) (win.getScreenHeight() - (y + height) * scaleH);
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

    public static List<Component> getFormattedTooltip(ItemStack stack) {
        List<Component> list = getUnFormattedTooltip(stack);

        if (!list.isEmpty()) {
        	Component con = list.get(0);
            list.set(0, con.copy().setStyle(stack.getRarity().getStyleModifier().apply(con.getStyle())));
        }

        for (int i = 1; i < list.size(); ++i) {
        	Component con = list.get(i);
            list.set(i, con.copy().setStyle(con.getStyle().applyFormat(ChatFormatting.GRAY)));
        }

        return list;
    }

    public static List<Component> getUnFormattedTooltip(ItemStack stack) {
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
            String info = ForgeRegistries.ITEMS.getKey(item) + " " + item.getClass() + " (" + stack.serializeNBT() + ")";
            BCLog.logger.warn("[lib.guide] Found null display name! " + info);
            name = Component.literal("!!NULL stack.getDisplayName(): " + info);
        }
        return name;
    }

    private static TooltipFlag getTooltipFlags() {
        boolean adv = mc.options.advancedItemTooltips;
        return adv ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
    }

    @Deprecated
    public static WrappedTextData getWrappedTextData(String text, Font fontRenderer, int maxWidth,
        boolean shadow, float scale) {
        List<FormattedCharSequence> lines = fontRenderer.split(FormattedText.of(text), maxWidth);
        return null;/*new WrappedTextData(fontRenderer, lines, shadow, scale, maxWidth,
            (int) (lines.size() * fontRenderer.getFontHeight("Ly") * scale));*/
    }

    public static class WrappedTextData {
        public final Font renderer;
        public final List<FormattedCharSequence> lines;
        public final float scale;
        public final boolean shadow;
        public final int width, height;
        

        public WrappedTextData(Font renderer, List<FormattedCharSequence> lines, boolean shadow, float scale, int width,
            int height) {
            this.renderer = renderer;
            this.lines = lines;
            this.shadow = shadow;
            this.scale = scale;
            this.width = width;
            this.height = height;
        }

        public void drawAt(PoseStack pose, int x, int y, int colour, boolean centered) {
/*            for (FormattedCharSequence line : lines) {
            	if(shadow)
            		renderer.dr(pose, line, x, y, colour, , centered, scale);
                y += renderer.getFontHeight(line) * scale;
            }*/
        }
    }
}
