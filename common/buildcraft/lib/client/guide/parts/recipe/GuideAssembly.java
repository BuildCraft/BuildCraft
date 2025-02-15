/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts.recipe;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.parts.GuidePartItem;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.RenderUtil;
import buildcraft.lib.recipe.ChangingItemStack;
import buildcraft.lib.recipe.ChangingObject;
import net.minecraft.client.gui.GuiGraphics;

import java.util.Arrays;
import java.util.Collections;

public class GuideAssembly extends GuidePartItem {
    public static final GuiIcon INPUT_LIST = new GuiIcon(GuiGuide.ICONS_2, 119, 108, 98, 54);
    public static final GuiRectangle[] ITEM_POSITION = new GuiRectangle[6];
    public static final GuiRectangle OUT_POSITION = new GuiRectangle(77, 19, 16, 16);
    public static final GuiRectangle MJ_POSITION = new GuiRectangle(50, 4, 6, 46);
    public static final GuiRectangle OFFSET = new GuiRectangle((GuiGuide.PAGE_LEFT_TEXT.width - INPUT_LIST.width) / 2,
            0, INPUT_LIST.width, INPUT_LIST.height);
    public static final int PIXEL_HEIGHT = 60;

    static {
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 3; y++) {
                ITEM_POSITION[x + y * 2] = new GuiRectangle(1 + x * 18, 1 + y * 18, 16, 16);
            }
        }
    }

    private final ChangingItemStack[] input;
    private final ChangingItemStack output;
    private final ChangingObject<Long> mjCost;
    private final int hash;

    GuideAssembly(GuiGuide gui, ChangingItemStack[] input, ChangingItemStack output, ChangingObject<Long> mjCost) {
        super(gui);
        this.input = input;
        this.output = output;
        this.mjCost = mjCost;
        this.hash = Arrays.deepHashCode(new Object[]{input, output, mjCost});
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) {
            return false;
        }
        GuideAssembly other = (GuideAssembly) obj;
        return Arrays.equals(input, other.input) && output.equals(other.output) && mjCost.equals(other.mjCost);
    }

    @Override
//    public PagePosition renderIntoArea(int x, int y, int width, int height, PagePosition current, int index)
    public PagePosition renderIntoArea(GuiGraphics guiGraphics, int x, int y, int width, int height, PagePosition current, int index) {
        if (current.pixel + PIXEL_HEIGHT > height) {
            current = current.newPage();
        }
        x += OFFSET.x;
        y += OFFSET.y + current.pixel;
        if (current.page == index) {
//            INPUT_LIST.drawAt(x, y);
            INPUT_LIST.drawAt(guiGraphics, x, y);
            // Render the item
//            GlStateManager.enableRescaleNormal();
//            RenderHelper.enableGUIStandardItemLighting();
            RenderUtil.enableGUIStandardItemLighting();
            for (int i = 0; i < input.length; i++) {
                GuiRectangle rect = ITEM_POSITION[i];
                drawItemStack(guiGraphics, input[i].get(), x + (int) rect.x, y + (int) rect.y);
            }

            drawItemStack(guiGraphics, output.get(), x + (int) OUT_POSITION.x, y + (int) OUT_POSITION.y);

            if (MJ_POSITION.offset(x, y).contains(gui.mouse)) {
//                gui.tooltips.add(Collections.singletonList(LocaleUtil.localizeMj(mjCost.get())));
                gui.tooltips.add(Collections.singletonList(LocaleUtil.localizeMjComponent(mjCost.get())));
            }

//            RenderHelper.disableStandardItemLighting();
            RenderUtil.disableStandardItemLighting();
//            GlStateManager.disableRescaleNormal();
        }
        current = current.nextLine(PIXEL_HEIGHT, height);
        return current;
    }

    @Override
//    public PagePosition handleMouseClick(int x, int y, int width, int height, PagePosition current, int index, int mouseX, int mouseY)
    public PagePosition handleMouseClick(GuiGraphics guiGraphics, int x, int y, int width, int height, PagePosition current, int index, double mouseX, double mouseY) {
        if (current.pixel + PIXEL_HEIGHT > height) {
            current = current.newPage();
        }
        x += OFFSET.x;
        y += OFFSET.y + current.pixel;
        if (current.page == index) {
            for (int i = 0; i < input.length; i++) {
                GuiRectangle rect = ITEM_POSITION[i];
                testClickItemStack(input[i].get(), x + (int) rect.x, y + (int) rect.y);
            }

            testClickItemStack(output.get(), x + (int) OUT_POSITION.x, y + (int) OUT_POSITION.y);
        }
        current = current.nextLine(PIXEL_HEIGHT, height);
        return current;
    }
}
