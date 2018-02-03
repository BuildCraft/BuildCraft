/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.elem;

import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import buildcraft.lib.expression.node.value.NodeConstantObject;
import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.gui.GuiElementSimple;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.misc.RenderUtil;

public class GuiElementText extends GuiElementSimple {
    public boolean dropShadow = false;
    public boolean foreground = false;
    public boolean centered = false;

    private final Supplier<String> text;
    private final IntSupplier colour;

    public GuiElementText(BuildCraftGui gui, IGuiPosition parent, Supplier<String> text, IntSupplier colour) {
        super(gui, GuiRectangle.ZERO.offset(parent));
        this.text = text;
        this.colour = colour;
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
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        return fr.getStringWidth(text.get());
    }

    @Override
    public double getHeight() {
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        return fr.FONT_HEIGHT;
    }

    @Override
    public void drawBackground(float partialTicks) {
        if (!foreground) {
            draw();
        }
    }

    @Override
    public void drawForeground(float partialTicks) {
        if (foreground) {
            draw();
        }
    }

    private void draw() {
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        if (centered) {
            String str = text.get();
            int width = fr.getStringWidth(str);
            double x = getX() - width / 2;
            fr.drawString(str, (float) x, (float) getY(), colour.getAsInt(), dropShadow);
        } else {
            fr.drawString(text.get(), (float) getX(), (float) getY(), colour.getAsInt(), dropShadow);
        }
        RenderUtil.setGLColorFromInt(-1);
    }

    @Override
    public String getDebugInfo(List<String> info) {
        info.add("text = " + text);
        return super.getDebugInfo(info);
    }
}
