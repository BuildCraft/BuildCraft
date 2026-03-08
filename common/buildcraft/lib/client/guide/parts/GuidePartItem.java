/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.misc.ItemStackKey;
import buildcraft.lib.misc.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;

public abstract class GuidePartItem extends GuidePart {
    public static final GuiRectangle STACK_RECT = new GuiRectangle(0, 0, 16, 16);

    public GuidePartItem(GuiGuide gui) {
        super(gui);
    }

    // protected void drawItemStack(ItemStackKey stack, int x, int y)
    protected void drawItemStack(ItemStackKey stack, int x, int y) {
//        drawItemStack(stack.baseStack, x, y);
        drawItemStack(stack.baseStack, x, y);
    }

    // protected void drawItemStack(ItemStack stack, int x, int y)
    protected void drawItemStack(ItemStack stack, int x, int y) {
        if (stack != null && !stack.isEmpty()) {
//            GlStateManager.color(1, 1, 1);
            RenderUtil.color(1, 1, 1);
//            Font fr = Minecraft.getInstance().fontRenderer;
            FontRenderer fr = Minecraft.getInstance().font;
//            gui.mc.getRenderItem().renderItemIntoGUI(stack, x, y);
            gui.getMinecraft().getItemRenderer().renderGuiItem(stack, x, y);
//            gui.mc.getRenderItem().renderItemOverlays(fr, stack, x, y);
            gui.getMinecraft().getItemRenderer().renderGuiItemDecorations(fr, stack, x, y);
            if (STACK_RECT.offset(x, y).contains(gui.mouse)) {
                gui.tooltipStack = stack;
            }
//            GlStateManager.color(1, 1, 1);
            RenderUtil.color(1, 1, 1);
        }
    }

    protected void testClickItemStack(ItemStackKey stack, int x, int y) {
        testClickItemStack(stack.baseStack, x, y);
    }

    protected void testClickItemStack(ItemStack stack, int x, int y) {
        if (stack != null && !stack.isEmpty() && STACK_RECT.offset(x, y).contains(gui.mouse)) {
            gui.openPage(GuideManager.INSTANCE.getPageFor(stack).createNew(gui));
        }
    }
}
