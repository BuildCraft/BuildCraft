/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.silicon.gui;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.registry.TagManager;

import buildcraft.silicon.container.ContainerAdvancedCraftingTable;

public class GuiAdvancedCraftingTable extends GuiBC8<ContainerAdvancedCraftingTable> {
    private static final ResourceLocation TEXTURE_BASE = new ResourceLocation("buildcraftsilicon:textures/gui/advanced_crafting_table.png");
    private static final int SIZE_X = 176, SIZE_Y = 241;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_PROGRESS = new GuiIcon(TEXTURE_BASE, SIZE_X, 0, 4, 70);
    private static final GuiRectangle RECT_PROGRESS = new GuiRectangle(164, 7, 4, 70);

    public GuiAdvancedCraftingTable(ContainerAdvancedCraftingTable container) {
        super(container);
        xSize = SIZE_X;
        ySize = SIZE_Y;
        /*
        GuiRecipeBookPhantom book;
        try {
            book = new GuiRecipeBookPhantom(this::sendRecipe);
        } catch (ReflectiveOperationException e) {
            BCLog.logger.warn("[silicon.gui] An exception was thrown while creating the recipe book gui!", e);
            book = null;
        }
        recipeBook = book;
        mainGui.shownElements.add(new LedgerHelp(mainGui, true));
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
                new GuiButtonImage(10, guiLeft + 5, height / 2 - 90, 20, 18, 0, 168, 19, VANILLA_CRAFTING_TABLE);
            buttonList.add(this.recipeButton);
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (recipeBook != null) {
            recipeBook.tick();
        }
        */
    }

    @Override
    protected void drawBackgroundLayer(float partialTicks) {
        ICON_GUI.drawAt(mainGui.rootElement);

        long target = container.tile.getTarget();
        if (target != 0) {
            double v = (double) container.tile.power / target;
            ICON_PROGRESS.drawCutInside(
                    new GuiRectangle(
                            RECT_PROGRESS.x,
                            (int) (RECT_PROGRESS.y + RECT_PROGRESS.height * Math.max(1 - v, 0)),
                            RECT_PROGRESS.width,
                            (int) Math.ceil(RECT_PROGRESS.height * Math.min(v, 1))
                    ).offset(mainGui.rootElement)
            );
        }
    }

    @Override
    protected void drawForegroundLayer() {
        String title = I18n.format("tile." + TagManager.getTag("block.advanced_crafting_table", TagManager.EnumTagType.UNLOCALIZED_NAME) + ".name");
        fontRendererObj.drawString(title, guiLeft + (xSize - fontRendererObj.getStringWidth(title)) / 2, guiTop + 5, 0x404040);
    }
}
