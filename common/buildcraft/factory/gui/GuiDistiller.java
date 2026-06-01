/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.RenderSystem.DestFactor;
import com.mojang.blaze3d.systems.RenderSystem.SourceFactor;
import net.minecraft.util.Identifier;

import buildcraft.lib.compat.FluidStackBC;

import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.recipes.IRefineryRecipeManager.IDistillationRecipe;

import buildcraft.lib.client.render.fluid.FluidRenderer;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.MathUtil;
import buildcraft.lib.misc.RenderUtil;

import buildcraft.factory.container.ContainerDistiller;
import com.mojang.blaze3d.platform.GlStateManager;

public class GuiDistiller extends GuiBC8<ContainerDistiller> {
    private static final Identifier TEXTURE_BASE
        = new Identifier("buildcraftfactory:textures/gui/distiller.png");
    private static final int SIZE_X = 176, SIZE_Y = 161;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_TANK_VERTICAL_OVERLAY = new GuiIcon(TEXTURE_BASE, 0, 161, 16, 38);
    private static final GuiIcon ICON_TANK_HORIZONTAL_OVERLAY = new GuiIcon(TEXTURE_BASE, 17, 161, 34, 17);
    private static final GuiIcon ICON_OFF_VALID_INPUT = new GuiIcon(TEXTURE_BASE, 176, 14, 17, 29);
    private static final GuiRectangle RECT_OFF_VALID_INPUT = new GuiRectangle(61, 26, 17, 29);
    private static final GuiIcon ICON_OFF_OUTPUT_GAS = new GuiIcon(TEXTURE_BASE, 192, 0, 20, 13);
    private static final GuiRectangle RECT_OFF_OUTPUT_GAS = new GuiRectangle(77, 12, 20, 13);
    private static final GuiIcon ICON_OFF_OUTPUT_LIQUID = new GuiIcon(TEXTURE_BASE, 192, 44, 20, 13);
    private static final GuiRectangle RECT_OFF_OUTPUT_LIQUID = new GuiRectangle(77, 56, 20, 13);

    private static final GuiIcon ICON_ACTIVE_BACKGROUND = new GuiIcon(TEXTURE_BASE, 176, 57, 36, 57);
    private static final GuiRectangle RECT_ACTIVE_BACKGROUND = new GuiRectangle(61, 12, 36, 57);
    private static final GuiIcon ICON_ACTIVE_ANIM_1 = new GuiIcon(TEXTURE_BASE, 212, 26, 26, 5);
    private static final GuiRectangle RECT_ACTIVE_ANIM_1 = new GuiRectangle(61, 38, 26, 5);
    private static final GuiIcon ICON_ACTIVE_ANIM_2A = new GuiIcon(TEXTURE_BASE, 225, 13, 13, 16);
    private static final GuiRectangle RECT_ACTIVE_ANIM_2A = new GuiRectangle(74, 25, 13, 16);
    private static final GuiIcon ICON_ACTIVE_ANIM_2B = new GuiIcon(TEXTURE_BASE, 225, 28, 13, 16);
    private static final GuiRectangle RECT_ACTIVE_ANIM_2B = new GuiRectangle(74, 40, 13, 16);
    private static final GuiIcon ICON_ACTIVE_ANIM_3A = new GuiIcon(TEXTURE_BASE, 230, 5, 3, 8);
    private static final GuiRectangle RECT_ACTIVE_ANIM_3A = new GuiRectangle(79, 17, 3, 8);
    private static final GuiIcon ICON_ACTIVE_ANIM_3B = new GuiIcon(TEXTURE_BASE, 230, 44, 3, 8);
    private static final GuiRectangle RECT_ACTIVE_ANIM_3B = new GuiRectangle(79, 56, 3, 8);
    private static final GuiIcon ICON_ACTIVE_ANIM_4A = new GuiIcon(TEXTURE_BASE, 230, 5, 18, 3);
    private static final GuiRectangle RECT_ACTIVE_ANIM_4A = new GuiRectangle(79, 17, 18, 3);
    private static final GuiIcon ICON_ACTIVE_ANIM_4B = new GuiIcon(TEXTURE_BASE, 230, 49, 18, 3);
    private static final GuiRectangle RECT_ACTIVE_ANIM_4B = new GuiRectangle(79, 61, 18, 3);

    /** Pixels per second */
    private static final double ACTIVE_SPEED = 300;

    private static final double ACTIVE_PIXEL_WIDTH = 150;

    long lastActiveTime = -1;
    double activeStart = -ACTIVE_PIXEL_WIDTH;
    double activeEnd = 0;

    public GuiDistiller(ContainerDistiller container) {
        super(container);
        xSize = SIZE_X;
        ySize = SIZE_Y;

        mainGui.shownElements.add(
            container.widgetInputTank.createGuiElement(
                mainGui, new GuiRectangle(44, 23, 16, 38).offset(mainGui.rootElement), ICON_TANK_VERTICAL_OVERLAY
            )
        );
        mainGui.shownElements.add(
            container.widgetOutputGasTank.createGuiElement(
                mainGui, new GuiRectangle(98, 10, 34, 17).offset(mainGui.rootElement), ICON_TANK_HORIZONTAL_OVERLAY
            )
        );
        mainGui.shownElements.add(
            container.widgetOutputLiquidTank.createGuiElement(
                mainGui, new GuiRectangle(98, 54, 34, 17).offset(mainGui.rootElement), ICON_TANK_HORIZONTAL_OVERLAY
            )
        );
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    protected void drawBackgroundLayer(float partialTicks) {
        ICON_GUI.drawAt(mainGui.rootElement);

        boolean isOn = container.tile.isActive();

        FluidStackBC currentInput = container.tile.tankIn.getFluidForRender();
        FluidStackBC currentGas = container.tile.tankGasOut.getFluidForRender();
        FluidStackBC currentLiquid = container.tile.tankLiquidOut.getFluidForRender();

        if (isOn) {

            ICON_ACTIVE_BACKGROUND.drawAt(RECT_ACTIVE_BACKGROUND.offset(mainGui.rootElement));

            long now = System.currentTimeMillis();
            if (lastActiveTime != -1) {
                double change = ACTIVE_SPEED * (now - lastActiveTime) / 1000.0;
                activeStart += change;
                activeEnd += change;
            }
            lastActiveTime = now;

            double distance = 0;

            double startStore = activeStart;
            double endStore = activeEnd;

            int inputColour = currentInput != null ? FluidRenderer.getAverageFluidColour(currentInput.getFluid()) : -1;
            int liquidColour = currentLiquid != null ? FluidRenderer.getAverageFluidColour(currentLiquid.getFluid()) : -1;
            int gasColour = currentGas != null ? FluidRenderer.getAverageFluidColour(currentGas.getFluid()) : -1;

            RenderSystem.enableBlend();
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            for (int i = 0; i < 10; i++) {
                distance = drawAnimation(inputColour, gasColour, liquidColour);

                activeStart -= distance * 3 / 4;
                activeEnd -= distance * 3 / 4;
            }

            activeStart = startStore;
            activeEnd = endStore;

            if (activeStart >= distance * 3) {
                activeStart -= distance * 3 / 4;
                activeEnd -= distance * 3 / 4;
            }

        } else {
            activeStart = -ACTIVE_PIXEL_WIDTH;
            activeEnd = 0;
            lastActiveTime = -1;

            IDistillationRecipe recipe
                = BuildcraftRecipeRegistry.refineryRecipes.getDistillationRegistry().getRecipeForInput(currentInput);
            boolean validInput = recipe != null;

            if (validInput) {
                ICON_OFF_VALID_INPUT.drawAt(RECT_OFF_VALID_INPUT.offset(mainGui.rootElement));
            }

            boolean gasBlocking = currentGas != null && currentGas.amount >= container.tile.tankGasOut.getCapacity();

            if (!gasBlocking) {
                if (currentGas == null || currentGas.amount <= 0) {
                    gasBlocking = false;
                } else if (recipe != null && !recipe.outGas().isFluidEqual(currentGas)) {
                    gasBlocking = true;
                }
            }

            if (gasBlocking) {
                ICON_OFF_OUTPUT_GAS.drawAt(RECT_OFF_OUTPUT_GAS.offset(mainGui.rootElement));
            }

            boolean liquidBlocking = currentLiquid != null && currentLiquid.amount >= container.tile.tankLiquidOut.getCapacity();

            if (!liquidBlocking) {
                if (currentLiquid == null || currentLiquid.amount <= 0) {
                    gasBlocking = false;
                } else if (recipe != null && !recipe.outLiquid().isFluidEqual(currentLiquid)) {
                    liquidBlocking = true;
                }
            }

            if (liquidBlocking) {
                ICON_OFF_OUTPUT_LIQUID.drawAt(RECT_OFF_OUTPUT_LIQUID.offset(mainGui.rootElement));
            }
        }
    }

    private double drawAnimation(int colourInput, int colourGas, int colourLiquid) {

        double distance = 0;

        distance += RECT_ACTIVE_ANIM_1.width * RECT_ACTIVE_ANIM_1.height;
        drawAnimationBottomToTop(colourGas, ICON_ACTIVE_ANIM_2A.offset(0, 114), RECT_ACTIVE_ANIM_2A, distance);
        drawAnimationTopToBottom(colourLiquid, ICON_ACTIVE_ANIM_2B.offset(0, 114), RECT_ACTIVE_ANIM_2B, distance);
        distance += RECT_ACTIVE_ANIM_2A.height * RECT_ACTIVE_ANIM_2A.width;
        drawAnimationBottomToTop(colourGas, ICON_ACTIVE_ANIM_3A.offset(0, 114), RECT_ACTIVE_ANIM_3A, distance);
        drawAnimationTopToBottom(colourLiquid, ICON_ACTIVE_ANIM_3B.offset(0, 114), RECT_ACTIVE_ANIM_3B, distance);
        distance += RECT_ACTIVE_ANIM_3A.height * RECT_ACTIVE_ANIM_3A.width;
        drawAnimationLeftToRight(colourGas, ICON_ACTIVE_ANIM_4A.offset(0, 114), RECT_ACTIVE_ANIM_4A, distance);
        drawAnimationLeftToRight(colourLiquid, ICON_ACTIVE_ANIM_4B.offset(0, 114), RECT_ACTIVE_ANIM_4B, distance);
        distance += RECT_ACTIVE_ANIM_4A.width * RECT_ACTIVE_ANIM_4A.height;

        double d2 = 0;

        drawAnimationLeftToRight(colourInput, ICON_ACTIVE_ANIM_1.offset(-36, 114), RECT_ACTIVE_ANIM_1, d2);
        d2 += RECT_ACTIVE_ANIM_1.width * RECT_ACTIVE_ANIM_1.height;
        drawAnimationBottomToTop(colourInput, ICON_ACTIVE_ANIM_2A.offset(-36, 114), RECT_ACTIVE_ANIM_2A, d2);
        drawAnimationTopToBottom(colourInput, ICON_ACTIVE_ANIM_2B.offset(-36, 114), RECT_ACTIVE_ANIM_2B, d2);

        return distance;
    }

    private void drawAnimationLeftToRight(int colour, GuiIcon icon, GuiRectangle rectangle, double startPoint) {
        double start = activeStart - startPoint;
        double end = activeEnd - startPoint;

        double size = rectangle.width * rectangle.height;

        if (end < 0 || start >= size) {
            return;
        }

        drawAnimationPart(colour, icon, rectangle, Math.max(0, start / size), 0, Math.min(1, end / size), 1);
    }

    private void drawAnimationBottomToTop(int colour, GuiIcon icon, GuiRectangle rectangle, double startPoint) {
        double start = activeStart - startPoint;
        double end = activeEnd - startPoint;

        double size = rectangle.width * rectangle.height;

        if (end < 0 || start >= size) {
            return;
        }

        drawAnimationPart(colour, icon, rectangle, 0, 1 - MathUtil.clamp(end / size, 0.0, 1.0), 1, 1 - MathUtil.clamp(start / size, 0.0, 1.0));
    }

    private void drawAnimationTopToBottom(int colour, GuiIcon icon, GuiRectangle rectangle, double startPoint) {
        double start = activeStart - startPoint;
        double end = activeEnd - startPoint;

        double size = rectangle.width * rectangle.height;

        if (end < 0 || start >= size) {
            return;
        }

        drawAnimationPart(colour, icon, rectangle, 0, Math.max(0, start / size), 1, Math.min(1, end / size));
    }

    private void drawAnimationPart(int colour, GuiIcon icon, GuiRectangle rectangle, double u0, double v0, double u1, double v1) {

        double x0 = rectangle.x + mainGui.rootElement.getX();
        double y0 = rectangle.y + mainGui.rootElement.getY();
        double x1 = x0 + rectangle.width;
        double y1 = y0 + rectangle.height;

        RenderUtil.setGLColorFromInt(colour);
        icon.drawCustomScaledAt(
            MathUtil.interp(u0, x0, x1), //
            MathUtil.interp(v0, y0, y1), //
            MathUtil.interp(u1, x0, x1), //
            MathUtil.interp(v1, y0, y1), //
            u0, v0, u1, v1
        );
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    protected void drawForegroundLayer() {
        String str = LocaleUtil.localize("tile.distiller.name");
        double titleX = mainGui.rootElement.getX() + 6;
        double titleY = mainGui.rootElement.getY() + 6;
        fontRenderer.draw(new net.minecraft.client.util.math.MatrixStack(), str, (int) titleX, (int) titleY, 0x404040);

        double invX = mainGui.rootElement.getX() + 8;
        double invY = mainGui.rootElement.getY() + SIZE_Y - 96;
        fontRenderer.draw(new net.minecraft.client.util.math.MatrixStack(), LocaleUtil.localize("gui.getInventory()"), (int) invX, (int) invY, 0x404040);
    }
}
