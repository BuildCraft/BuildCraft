/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.gui.elem;

import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import buildcraft.lib.expression.node.value.NodeConstantObject;
import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.gui.GuiElementSimple;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiPosition;

@Environment(EnvType.CLIENT)
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

    private TextRenderer getFr() {
        return MinecraftClient.getInstance().textRenderer;
    }

    @Override
    public double getWidth() {
        return getFr().getWidth(text.get());
    }

    @Override
    public double getHeight() {
        return getFr().fontHeight;
    }

    @Override
    public void drawBackground(DrawContext context, float partialTicks) {
        if (!foreground) draw(context);
    }

    @Override
    public void drawForeground(DrawContext context, float partialTicks) {
        if (foreground) draw(context);
    }

    private void draw(DrawContext context) {
        String str = text.get();
        int x = centered ? (int) getX() - getFr().getWidth(str) / 2 : (int) getX();
        context.drawText(getFr(), str, x, (int) getY(), colour.getAsInt(), dropShadow);
    }

    @Override
    public String getDebugInfo(List<String> info) {
        info.add("text = " + text);
        return super.getDebugInfo(info);
    }
}
