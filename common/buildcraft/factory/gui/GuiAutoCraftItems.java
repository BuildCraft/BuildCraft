/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.factory.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import buildcraft.api.core.BCLog;
import buildcraft.lib.gui.ledger.LedgerHelp;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiButtonImage;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;

import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.tile.item.ItemHandlerSimple;

import buildcraft.factory.container.ContainerAutoCraftItems;

public class GuiAutoCraftItems extends GuiBC8<ContainerAutoCraftItems> implements IRecipeShownListener {
    private static final ResourceLocation TEXTURE_BASE =
        new ResourceLocation("buildcraftfactory:textures/gui/autobench_item.png");
    private static final ResourceLocation TEXTURE_MISC =
        new ResourceLocation("buildcraftlib:textures/gui/misc_slots.png");
    private static final ResourceLocation VANILLA_CRAFTING_TABLE =
        new ResourceLocation("textures/gui/container/crafting_table.png");
    private static final int SIZE_X = 176, SIZE_Y = 197;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_FILTER_OVERLAY_SAME = new GuiIcon(TEXTURE_MISC, 54, 0, 18, 18);
    private static final GuiIcon ICON_FILTER_OVERLAY_DIFFERENT = new GuiIcon(TEXTURE_MISC, 72, 0, 18, 18);
    private static final GuiIcon ICON_PROGRESS = new GuiIcon(TEXTURE_BASE, SIZE_X, 0, 23, 10);
    private static final GuiRectangle RECT_PROGRESS = new GuiRectangle(90, 47, 23, 10);

    //TODO Port the 1.12 version

    public GuiAutoCraftItems(ContainerAutoCraftItems container) {
        super(container);
        xSize = SIZE_X;
        ySize = SIZE_Y;
        /*
        GuiRecipeBookPhantom book;
        try {
            book = new GuiRecipeBookPhantom(this::sendRecipe);
        } catch (ReflectiveOperationException e) {
            BCLog.logger.warn("[factory.gui] An exception was thrown while creating the recipe book gui!", e);
            book = null;
        }
        recipeBook = book;*/
        mainGui.shownElements.add(new LedgerHelp(mainGui, true));
    }

    private void sendRecipe(IRecipe recipe) {
        /*
        List<ItemStack> stacks = new ArrayList<>(9);

        int maxX = recipe instanceof IShapedRecipe ? ((IShapedRecipe) recipe).getRecipeWidth() : 3;
        int maxY = recipe instanceof IShapedRecipe ? ((IShapedRecipe) recipe).getRecipeHeight() : 3;
        int offsetX = maxX == 1 ? 1 : 0;
        int offsetY = maxY == 1 ? 1 : 0;
        List<Ingredient> ingredients = recipe.getIngredients();
        if (ingredients.isEmpty()) {
            return;
        }
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                if (x < offsetX || y < offsetY) {
                    stacks.add(ItemStack.EMPTY);
                    continue;
                }
                int i = x - offsetX + (y - offsetY) * maxX;
                if (i >= ingredients.size() || x - offsetX >= maxX) {
                    stacks.add(ItemStack.EMPTY);
                } else {
                    Ingredient ing = ingredients.get(i);
                    ItemStack[] matching = ing.getMatchingStacks();
                    if (matching.length >= 1) {
                        stacks.add(matching[0]);
                    } else {
                        stacks.add(ItemStack.EMPTY);
                    }
                }
            }
        }

        container.sendSetPhantomSlots(container.tile.invBlueprint, stacks);
        */
    }

    @Override
    protected boolean shouldAddHelpLedger() {
        // Don't add it on the left side because it clashes with the recipe book
        return false;
    }

    @Override
    public void initGui() {
        super.initGui();
        /*
        widthTooNarrow = this.width < SIZE_X + 176;
        if (recipeBook != null) {
            InventoryCrafting invCraft = container.tile.getWorkbenchCrafting();
            recipeBook.func_194303_a(width, height, mc, widthTooNarrow, invCraft);
            guiLeft = recipeBook.updateScreenPosition(widthTooNarrow, width, xSize);
            recipeButton =
                new GuiButtonImage(10, guiLeft + 5, height / 2 - 66, 20, 18, 0, 168, 19, VANILLA_CRAFTING_TABLE);
            buttonList.add(this.recipeButton);
        }
        */
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        /*
        if (recipeBook != null) {
            recipeBook.tick();
        }
        */
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        /*
        if (recipeBook == null) {
            super.drawScreen(mouseX, mouseY, partialTicks);
            return;
        }
        if (recipeBook.isVisible() && this.widthTooNarrow) {
            drawDefaultBackground();
            this.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
            recipeBook.render(mouseX, mouseY, partialTicks);
            renderHoveredToolTip(mouseX, mouseY);
        } else {
            super.drawScreen(mouseX, mouseY, partialTicks);
            recipeBook.render(mouseX, mouseY, partialTicks);
            recipeBook.renderGhostRecipe(this.guiLeft, this.guiTop, true, partialTicks);
        }

        recipeBook.renderTooltip(this.guiLeft, this.guiTop, mouseX, mouseY);
        */
    }

    @Override
    protected void drawBackgroundLayer(float partialTicks) {
        ICON_GUI.drawAt(mainGui.rootElement);

        double progress = container.tile.getProgress(partialTicks);

        drawProgress(RECT_PROGRESS, ICON_PROGRESS, progress, 1);

        if (hasFilters()) {
            RenderHelper.enableGUIStandardItemLighting();
            forEachFilter((slot, filterStack) -> {
                int x = slot.xPos + (int) mainGui.rootElement.getX();
                int y = slot.yPos + (int) mainGui.rootElement.getY();
                itemRender.renderItemAndEffectIntoGUI(mc.player, filterStack, x, y);
                itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, filterStack, x, y, null);
            });
            RenderHelper.disableStandardItemLighting();

            GlStateManager.disableDepth();
            forEachFilter((slot, filterStack) -> {
                ItemStack real = slot.getStack();
                final GuiIcon icon;
                if (real.isEmpty() || StackUtil.canMerge(real, filterStack)) {
                    icon = ICON_FILTER_OVERLAY_SAME;
                } else {
                    icon = ICON_FILTER_OVERLAY_DIFFERENT;
                }
                int x = slot.xPos + (int) mainGui.rootElement.getX();
                int y = slot.yPos + (int) mainGui.rootElement.getY();
                icon.drawAt(x - 1, y - 1);
            });
            GlStateManager.enableDepth();
        }
    }

    private boolean hasFilters() {
        ItemHandlerSimple filters = container.tile.invMaterialFilter;
        for (int s = 0; s < filters.getSlots(); s++) {
            ItemStack filter = filters.getStackInSlot(s);
            if (!filter.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void forEachFilter(IFilterSlotIterator iter) {
        ItemHandlerSimple filters = container.tile.invMaterialFilter;
        for (int s = 0; s < filters.getSlots(); s++) {
            ItemStack filter = filters.getStackInSlot(s);
            if (!filter.isEmpty()) {
                iter.iterate(container.materialSlots[s], filter);
            }
        }
    }

    @FunctionalInterface
    private interface IFilterSlotIterator {
        void iterate(SlotBase drawSlot, ItemStack filterStack);
    }
}