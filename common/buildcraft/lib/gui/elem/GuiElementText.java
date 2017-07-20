/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.elem;

import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiElementSimple;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class GuiElementText extends GuiElementSimple<GuiBC8<?>> {
    public boolean dropShadow = false;
    public boolean foreground = true;

    private final Supplier<String> text;
    private final IntSupplier colour;

    public GuiElementText(GuiBC8<?> gui, IGuiPosition parent, Supplier<String> text, IntSupplier colour) {
        super(gui, parent, GuiRectangle.ZERO);
        this.text = text;
        this.colour = colour;
    }

    public GuiElementText(GuiBC8<?> gui, IGuiPosition parent, String text, int colour) {
        this(gui, parent, () -> text, () -> colour);
    }

    public GuiElementText setDropShadow(boolean value) {
        dropShadow = value;
        return this;
    }

    public GuiElementText setForeground(boolean value) {
        foreground = value;
        return this;
    }

    @Override
    public int getWidth() {
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        return fr.getStringWidth(text.get());
    }

    @Override
    public int getHeight() {
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
        fr.drawString(text.get(), getX(), getY(), colour.getAsInt(), dropShadow);
    }
}
