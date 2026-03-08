/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.elem;

import buildcraft.lib.client.guide.font.MinecraftFont;
import buildcraft.lib.expression.node.value.NodeConstantDouble;
import buildcraft.lib.expression.node.value.NodeConstantObject;
import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.gui.GuiElementSimple;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiPosition;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class GuiElementText extends GuiElementSimple {
    public boolean dropShadow = false;
    public boolean foreground = false;
    public boolean centered = false;

    private final Supplier<String> text;
    private final IntSupplier colour;
    private final DoubleSupplier scale;// TODO: Use this and then use this for the guide!

    public GuiElementText(BuildCraftGui gui, IGuiPosition parent, Supplier<String> text, IntSupplier colour) {
        this(gui, parent, text, colour, NodeConstantDouble.ONE);
    }

    public GuiElementText(BuildCraftGui gui, IGuiPosition parent, Supplier<String> text, IntSupplier colour,
                          DoubleSupplier scale) {
        super(gui, GuiRectangle.ZERO.offset(parent));// TODO: link this up like in GuidePageContents!
        this.text = text;
        this.colour = colour;
        this.scale = scale;
    }

    public GuiElementText(BuildCraftGui gui, IGuiPosition parent, Supplier<String> text, int colour) {
        this(gui, parent, text, () -> colour);
    }

    public GuiElementText(BuildCraftGui gui, IGuiPosition parent, String text, int colour) {
        this(gui, parent, new NodeConstantObject<>(String.class, text), () -> colour);
    }

    public GuiElementText setDropShadow(boolean value) {
        dropShadow = value;
        return this;
    }

    public GuiElementText setForeground(boolean value) {
        foreground = value;
        return this;
    }

    public GuiElementText setCentered(boolean centered) {
        this.centered = centered;
        return this;
    }

    @Override
    public double getWidth() {
        FontRenderer fr = Minecraft.getInstance().font;
        return fr.width(text.get());
    }

    @Override
    public double getHeight() {
        FontRenderer fr = Minecraft.getInstance().font;
        return fr.lineHeight;
    }

    @Override
    public void drawBackground(float partialTicks, MatrixStack poseStack) {
        if (!foreground) {
            draw(poseStack);
        }
    }

    @Override
    public void drawForeground(MatrixStack poseStack, float partialTicks) {
        if (foreground) {
            draw(poseStack);
        }
    }

    private void draw(MatrixStack poseStack) {
        MinecraftFont.INSTANCE.drawString(poseStack, text.get(), (int) getX(), (int) getY(), colour.getAsInt(), dropShadow,
                centered, (float) scale.getAsDouble());
        // Calen: 原来就是注释
        // final double s = scale.getAsDouble();
        // final boolean needsScaling = s != 1;
        // Font fr = Minecraft.getMinecraft().fontRenderer;
        // if (needsScaling) {
        // GuiUtil.drawScaledText(fr, text.get(), getX(), getY(), colour.getAsInt(), dropShadow, centered, s);
        // return;
        // }
        // if (centered) {
        // String str = text.get();
        // int width = fr.getStringWidth(str);
        // double x = getX() - width / 2;
        // fr.drawString(str, (float) x, (float) getY(), colour.getAsInt(), dropShadow);
        // } else {
        // fr.drawString(text.get(), (float) getX(), (float) getY(), colour.getAsInt(), dropShadow);
        // }
        // RenderUtil.setGLColorFromInt(-1);
    }

    @Override
    public String getDebugInfo(List<String> info) {
        info.add("text = " + text);
        return super.getDebugInfo(info);
    }
}
