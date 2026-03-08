/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts.recipe;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.parts.GuidePart;
import buildcraft.lib.client.guide.parts.GuidePartItem;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.misc.RenderUtil;
import buildcraft.lib.recipe.ChangingItemStack;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;

import javax.annotation.Nonnull;
import java.util.Objects;

public class GuideSmelting extends GuidePartItem {
    public static final GuiIcon SMELTING_ICON = new GuiIcon(GuiGuide.ICONS_2, 119, 54, 80, 54);
    public static final GuiRectangle OFFSET = new GuiRectangle(
            (GuiGuide.PAGE_LEFT_TEXT.width - SMELTING_ICON.width) / 2, 0, SMELTING_ICON.width, SMELTING_ICON.height);
    public static final GuiRectangle IN_POS = new GuiRectangle(1, 1, 16, 16);
    public static final GuiRectangle OUT_POS = new GuiRectangle(59, 19, 16, 16);
    public static final GuiRectangle FURNACE_POS = new GuiRectangle(1, 37, 16, 16);
    public static final int PIXEL_HEIGHT = 60;

    private final ChangingItemStack input, output;
    private final ItemStack furnace;
    private final int hash;

    // public GuideSmelting(GuiGuide gui, @Nonnull ItemStack input, @Nonnull ItemStack output)
    public GuideSmelting(GuiGuide gui, @Nonnull NonNullList<Ingredient> input, @Nonnull ItemStack output) {
        super(gui);
        this.input = new ChangingItemStack(input);
        this.output = new ChangingItemStack(output);
        furnace = new ItemStack(Blocks.FURNACE);
        this.hash = Objects.hash(input, output);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != getClass()) return false;
        GuideSmelting other = (GuideSmelting) obj;
        // Shortcut out of this full itemstack comparison as its really expensive
        if (hash != other.hash) return false;
        return input.equals(other.input) && output.equals(other.output);
    }

    @Override
//    public GuidePart.PagePosition renderIntoArea(int x, int y, int width, int height, GuidePart.PagePosition current, int index)
    public GuidePart.PagePosition renderIntoArea(MatrixStack poseStack, int x, int y, int width, int height, GuidePart.PagePosition current, int index) {
        if (current.pixel + PIXEL_HEIGHT > height) {
            current = current.newPage();
        }
        x += OFFSET.x;
        y += OFFSET.y + current.pixel;
        if (current.page == index) {
//            SMELTING_ICON.drawAt(x, y);
            SMELTING_ICON.drawAt(poseStack, x, y);
            // Render the item
//            GlStateManager.enableRescaleNormal();
//            RenderHelper.enableGUIStandardItemLighting();
            RenderUtil.enableGUIStandardItemLighting();

            drawItemStack(input.get(), x + (int) IN_POS.x, y + (int) IN_POS.y);
            drawItemStack(output.get(), x + (int) OUT_POS.x, y + (int) OUT_POS.y);
            drawItemStack(furnace, x + (int) FURNACE_POS.x, y + (int) FURNACE_POS.y);

//            RenderHelper.disableStandardItemLighting();
            RenderUtil.disableStandardItemLighting();
//            GlStateManager.disableRescaleNormal();
        }
        current = current.nextLine(PIXEL_HEIGHT, height);
        return current;
    }

    @Override
//    public GuidePart.PagePosition handleMouseClick(int x, int y, int width, int height, GuidePart.PagePosition current, int index, int mouseX, int mouseY)
    public PagePosition handleMouseClick(MatrixStack poseStack, int x, int y, int width, int height, PagePosition current, int index, double mouseX, double mouseY) {
        if (current.pixel + PIXEL_HEIGHT > height) {
            current = current.newPage();
        }
        x += OFFSET.x;
        y += OFFSET.y + current.pixel;
        if (current.page == index) {

            testClickItemStack(input.get(), x + (int) IN_POS.x, y + (int) IN_POS.y);
            testClickItemStack(output.get(), x + (int) OUT_POS.x, y + (int) OUT_POS.y);
            testClickItemStack(furnace, x + (int) FURNACE_POS.x, y + (int) FURNACE_POS.y);

        }
        current = current.nextLine(PIXEL_HEIGHT, height);
        return current;
    }
}
