/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.factory.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiButtonImage;
import net.minecraft.client.gui.recipebook.GuiRecipeBook;
import net.minecraft.client.gui.recipebook.IRecipeShownListener;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.common.crafting.IShapedRecipe;

import buildcraft.api.core.BCLog;

import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.ledger.LedgerHelp;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.recipe.GuiRecipeBookPhantom;

import buildcraft.factory.container.ContainerAutoCraftItems;

public class GuiAutoCraftItems extends GuiBC8<ContainerAutoCraftItems> implements IRecipeShownListener {
    private static final ResourceLocation TEXTURE_BASE =
        new ResourceLocation("buildcraftfactory:textures/gui/autobench_item.png");
    private static final ResourceLocation VANILLA_CRAFTING_TABLE =
        new ResourceLocation("textures/gui/container/crafting_table.png");
    private static final int SIZE_X = 176, SIZE_Y = 197;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_PROGRESS = new GuiIcon(TEXTURE_BASE, SIZE_X, 0, 23, 10);
    private static final GuiRectangle RECT_PROGRESS = new GuiRectangle(90, 47, 23, 10);

    private final GuiRecipeBookPhantom recipeBook;
    /** If true then the recipe book will be drawn on top of this GUI, rather than beside it */
    private boolean widthTooNarrow;
    private GuiButtonImage recipeButton;

    public GuiAutoCraftItems(ContainerAutoCraftItems container) {
        super(container);
        xSize = SIZE_X;
        ySize = SIZE_Y;
        GuiRecipeBookPhantom book;
        try {
            book = new GuiRecipeBookPhantom(this::sendRecipe);
        } catch (ReflectiveOperationException e) {
            BCLog.logger.warn("[factory.gui] An exception was thrown while creating the recipe book gui!", e);
            book = null;
        }
        recipeBook = book;
        shownElements.add(new LedgerHelp(this, true));
    }

    private void sendRecipe(IRecipe recipe) {
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
    }

    @Override
    protected boolean shouldAddHelpLedger() {
        // Don't add it on the left side because it clashes with the recipe book
        return false;
    }

    @Override
    public void initGui() {
        super.initGui();
        widthTooNarrow = this.width < SIZE_X + 176;
        if (recipeBook != null) {
            InventoryCrafting invCraft = container.tile.getWorkbenchCrafting();
            recipeBook.func_194303_a(width, height, mc, widthTooNarrow, invCraft);
            guiLeft = recipeBook.updateScreenPosition(widthTooNarrow, width, xSize);
            recipeButton =
                new GuiButtonImage(10, guiLeft + 5, height / 2 - 66, 20, 18, 0, 168, 19, VANILLA_CRAFTING_TABLE);
            buttonList.add(this.recipeButton);
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (recipeBook != null) {
            recipeBook.tick();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
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
    }

    @Override
    protected void drawBackgroundLayer(float partialTicks) {
        ICON_GUI.drawAt(rootElement);

        double progress = container.tile.getProgress(partialTicks);

        drawProgress(RECT_PROGRESS, ICON_PROGRESS, progress, 1);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == recipeButton && recipeBook != null) {
            recipeBook.initVisuals(widthTooNarrow, container.tile.getWorkbenchCrafting());
            recipeBook.toggleVisibility();
            guiLeft = recipeBook.updateScreenPosition(widthTooNarrow, width, xSize);
            recipeButton.setPosition(this.guiLeft + 5, this.height / 2 - 66);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (recipeBook == null) {
            super.mouseClicked(mouseX, mouseY, mouseButton);
            return;
        }
        if (!recipeBook.mouseClicked(mouseX, mouseY, mouseButton)) {
            if (!widthTooNarrow || !recipeBook.isVisible()) {
                super.mouseClicked(mouseX, mouseY, mouseButton);
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (recipeBook == null) {
            super.keyTyped(typedChar, keyCode);
            return;
        }
        if (!recipeBook.keyPressed(typedChar, keyCode)) {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void handleMouseClick(Slot slot, int slotId, int mouseButton, ClickType type) {
        super.handleMouseClick(slot, slotId, mouseButton, type);
        if (recipeBook != null) {
            recipeBook.slotClicked(slot);
        }
    }

    @Override
    protected boolean isPointInRegion(int rectX, int rectY, int rectWidth, int rectHeight, int pointX, int pointY) {
        if (recipeBook == null) {
            return super.isPointInRegion(rectX, rectY, rectWidth, rectHeight, pointX, pointY);
        }
        return (!widthTooNarrow || !recipeBook.isVisible())
            && super.isPointInRegion(rectX, rectY, rectWidth, rectHeight, pointX, pointY);
    }

    @Override
    protected boolean hasClickedOutside(int mouseX, int mouseY, int _guiLeft, int _guiTop) {
        if (recipeBook == null) {
            return super.hasClickedOutside(mouseX, mouseY, _guiLeft, _guiTop);
        }
        boolean flag =
            mouseX < _guiLeft || mouseY < _guiTop || mouseX >= _guiLeft + xSize || mouseY >= _guiTop + this.ySize;
        return recipeBook.hasClickedOutside(mouseX, mouseY, guiLeft, guiTop, xSize, ySize) && flag;
    }

    @Override
    public void onGuiClosed() {
        if (recipeBook != null) {
            recipeBook.removed();
        }
        super.onGuiClosed();
    }

    // IRecipeShownListener

    @Override
    public void recipesUpdated() {
        if (recipeBook != null) {
            recipeBook.recipesUpdated();
        }
    }

    @Override
    public GuiRecipeBook func_194310_f() {
        return recipeBook;
    }
}
