/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.gui.elem;

import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import com.mojang.blaze3d.vertex.PoseStack;

import ct.buildcraft.lib.expression.node.value.NodeConstantDouble;
import ct.buildcraft.lib.expression.node.value.NodeConstantObject;
import ct.buildcraft.lib.gui.BuildCraftGui;
import ct.buildcraft.lib.gui.GuiElementSimple;
import ct.buildcraft.lib.gui.pos.GuiRectangle;
import ct.buildcraft.lib.gui.pos.IGuiPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

public class GuiElementText extends GuiElementSimple {
    public boolean dropShadow = false;
    public boolean foreground = false;
    public boolean centered = false;

    private final Supplier<Component> text;
    private final IntSupplier colour;
    private final DoubleSupplier scale;// TODO: Use this and then use this for the guide!

    public GuiElementText(BuildCraftGui gui, IGuiPosition parent, Supplier<Component> text, IntSupplier colour) {
        this(gui, parent, text, colour, NodeConstantDouble.ONE);
    }
    
    public static GuiElementText creat(BuildCraftGui gui, IGuiPosition parent, Supplier<String> text, IntSupplier colour) {
        return new GuiElementText(gui, parent, () -> Component.literal(text.get()), colour, NodeConstantDouble.ONE);
    }

    public GuiElementText(BuildCraftGui gui, IGuiPosition parent, Supplier<Component> text, IntSupplier colour,
        DoubleSupplier scale) {
        super(gui, GuiRectangle.ZERO.offset(parent));// TODO: link this up like in GuidePageContents!
        this.text = text;
        this.colour = colour;
        this.scale = scale;
    }

    public GuiElementText(BuildCraftGui gui, IGuiPosition parent, Supplier<Component> text, int colour) {
        this(gui, parent, text, () -> colour);
    }

    public GuiElementText(BuildCraftGui gui, IGuiPosition parent, Component text, int colour) {
        this(gui, parent, new NodeConstantObject<>(Component.class, text), () -> colour);
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
        Minecraft mc = Minecraft.getInstance();
		Font fr = mc.font;
        return fr.width(text.get());
    }

    @Override
    public double getHeight() {
        Minecraft mc = Minecraft.getInstance();
		Font fr = mc.font;
        return fr.lineHeight;
    }

    @Override
    public void drawBackground(PoseStack pose, float partialTicks) {
        if (!foreground) {
            draw(pose);
        }
    }

    @Override
    public void drawForeground(PoseStack pose, float partialTicks) {
        if (foreground) {
            draw(pose);
        }
    }

    private void draw(PoseStack pose) {
    	Minecraft mc = Minecraft.getInstance();
		Component content = text.get();
		if(dropShadow)
    		mc.font.drawShadow(pose, content, (int) getX() - (centered ? mc.font.width(content)/2 : 0), (int) getY(), colour.getAsInt()/*, dropShadow,
            centered, (float) scale.getAsDouble()*/);
    	else
    		mc.font.draw(pose, content, (int) getX() - (centered ? mc.font.width(content)/2 : 0), (int) getY(), colour.getAsInt()/*, dropShadow,
    	            centered, (float) scale.getAsDouble()*/);//TODO scale
        // final double s = scale.getAsDouble();
        // final boolean needsScaling = s != 1;
        // FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
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
