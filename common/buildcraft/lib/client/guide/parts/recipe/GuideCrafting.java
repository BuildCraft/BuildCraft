/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts.recipe;

import javax.annotation.Nonnull;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.parts.GuidePartItem;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.misc.data.NonNullMatrix;
import buildcraft.lib.recipe.ChangingItemStack;

public class GuideCrafting extends GuidePartItem {
    public static final GuiIcon CRAFTING_GRID = new GuiIcon(GuiGuide.ICONS_2, 119, 0, 116, 54);
    public static final GuiRectangle[][] ITEM_POSITION = new GuiRectangle[3][3];
    public static final GuiRectangle OUT_POSITION = new GuiRectangle(95, 19, 16, 16);
    public static final GuiRectangle OFFSET = new GuiRectangle((GuiGuide.PAGE_LEFT_TEXT.width - CRAFTING_GRID.width) / 2, 0, CRAFTING_GRID.width, CRAFTING_GRID.height);
    public static final int PIXEL_HEIGHT = 60;

    static {
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                ITEM_POSITION[x][y] = new GuiRectangle(1 + x * 18, 1 + y * 18, 16, 16);
            }
        }
    }

    private final ChangingItemStack[][] input;
    private final ChangingItemStack output;

    GuideCrafting(GuiGuide gui, NonNullMatrix<Ingredient> input, @Nonnull ItemStack output) {
        super(gui);
        this.input = new ChangingItemStack[input.getWidth()][input.getHeight()];
        for (int x = 0; x < input.getWidth(); x++) {
            for (int y = 0; y < input.getHeight(); y++) {
                this.input[x][y] = new ChangingItemStack(input.get(x, y));
            }
        }
        this.output = new ChangingItemStack(output);
    }

    GuideCrafting(GuiGuide gui, ChangingItemStack[][] input, ChangingItemStack output) {
        super(gui);
        this.input = input;
        this.output = output;
    }

    @Override
    public PagePosition renderIntoArea(int x, int y, int width, int height, PagePosition current, int index) {
        if (current.pixel + PIXEL_HEIGHT > height) {
            current = current.newPage();
        }
        x += OFFSET.x;
        y += OFFSET.y + current.pixel;
        if (current.page == index) {
            CRAFTING_GRID.drawAt(x, y);
            // Render the item
            GlStateManager.enableRescaleNormal();
            RenderHelper.enableGUIStandardItemLighting();
            for (int itemX = 0; itemX < input.length; itemX++) {
                for (int itemY = 0; itemY < input[itemX].length; itemY++) {
                    GuiRectangle rect = ITEM_POSITION[itemX][itemY];
                    ItemStack stack = input[itemX][itemY].get();
                    drawItemStack(stack, x + (int) rect.x, y + (int) rect.y);
                }
            }

            drawItemStack(output.get(), x + (int) OUT_POSITION.x, y + (int) OUT_POSITION.y);

            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableRescaleNormal();
        }
        current = current.nextLine(PIXEL_HEIGHT, height);
        return current;
    }

    @Override
    public PagePosition handleMouseClick(int x, int y, int width, int height, PagePosition current, int index, int mouseX, int mouseY) {
        if (current.pixel + PIXEL_HEIGHT > height) {
            current = current.newPage();
        }
        x += OFFSET.x;
        y += OFFSET.y + current.pixel;
        if (current.page == index) {
            for (int itemX = 0; itemX < input.length; itemX++) {
                for (int itemY = 0; itemY < input[itemX].length; itemY++) {
                    GuiRectangle rect = ITEM_POSITION[itemX][itemY];
                    ItemStack stack = input[itemX][itemY].get();
                    testClickItemStack(stack, x + (int) rect.x, y + (int) rect.y);
                }
            }

            testClickItemStack(output.get(), x + (int) OUT_POSITION.x, y + (int) OUT_POSITION.y);
        }
        current = current.nextLine(PIXEL_HEIGHT, height);
        return current;
    }
}
