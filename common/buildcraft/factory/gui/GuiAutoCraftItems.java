/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.factory.gui;

import buildcraft.factory.container.ContainerAutoCraftItems;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.ledger.LedgerHelp;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.misc.RenderUtil;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.recipebook.RecipeShownListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.common.crafting.IShapedRecipe;

import java.util.ArrayList;
import java.util.List;

// TODO Calen RecipeBook???
public class GuiAutoCraftItems extends GuiBC8<ContainerAutoCraftItems> implements RecipeShownListener
//public class GuiAutoCraftItems extends GuiWithRecipeBookBC8<ContainerAutoCraftItems> implements RecipeShownListener
{
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
    private static final GuiIcon ICON_FILTER_OVERLAY_SIMILAR = new GuiIcon(TEXTURE_MISC, 90, 0, 18, 18);
    private static final GuiIcon ICON_PROGRESS = new GuiIcon(TEXTURE_BASE, SIZE_X, 0, 23, 10);
    private static final GuiRectangle RECT_PROGRESS = new GuiRectangle(90, 47, 23, 10);

//    private final GuiRecipeBookPhantom recipeBook;
//    /** If true then the recipe book will be drawn on top of this GUI, rather than beside it */
//    private boolean widthTooNarrow;
//    private GuiButtonImage recipeButton;
//    private ImageButton recipeButton;

    public GuiAutoCraftItems(ContainerAutoCraftItems container, Inventory inventory, Component component) {
        super(container, inventory, component);
//        xSize = SIZE_X;
        imageWidth = SIZE_X;
//        ySize = SIZE_Y;
        imageHeight = SIZE_Y;
//        GuiRecipeBookPhantom book;
//        widthTooNarrow = this.width < SIZE_X + 176; // Calen: moved from initGui
//        try {
//            book = new GuiRecipeBookPhantom(this::sendRecipe);
////            book = new GuiRecipeBookPhantom(this::sendRecipe, width, height, minecraft, widthTooNarrow, container);
//        }
//        catch (ReflectiveOperationException e) {
//            BCLog.logger.warn("[factory.gui] An exception was thrown while creating the recipe book gui!", e);
//            book = null;
//        }
//        recipeBook = book;
        mainGui.shownElements.add(new LedgerHelp(mainGui, true));
    }

    // private void sendRecipe(Recipe recipe)
    private void sendRecipe(CraftingRecipe recipe) {
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
//                    ItemStack[] matching = ing.getMatchingStacks();
                    ItemStack[] matching = ing.getItems();
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
//        widthTooNarrow = this.width < SIZE_X + 176; // Calen: moved to <init>
//        if (recipeBook != null) {
////            InventoryCrafting invCraft = container.tile.getWorkbenchCrafting();
//            CraftingContainer invCraft = container.tile.getWorkbenchCrafting();
//
////            recipeBook.func_194303_a(width, height, mc, widthTooNarrow, invCraft);
//            recipeBook.init(width, height, minecraft, widthTooNarrow, this.menu);
////            guiLeft = recipeBook.updateScreenPosition(widthTooNarrow, width, xSize);
//            leftPos = recipeBook.updateScreenPosition(width, imageWidth);
////            recipeButton = new GuiButtonImage(10, guiLeft + 5, height / 2 - 66, 20, 18, 0, 168, 19, VANILLA_CRAFTING_TABLE);
//            recipeButton = new ImageButton(
////                    10,
//                    leftPos + 5,
//                    height / 2 - 66,
//                    20,
//                    18,
//                    0,
//                    168,
//                    19,
//                    VANILLA_CRAFTING_TABLE,
//                    (button) ->
//                    {
//                        if (button == recipeButton && recipeBook != null) {
//                            recipeBook.initVisuals(widthTooNarrow, container.tile.getWorkbenchCrafting());
//                            recipeBook.toggleVisibility();

////                             guiLeft = recipeBook.updateScreenPosition(widthTooNarrow, width, xSize);
//                            leftPos = recipeBook.updateScreenPosition(width, imageWidth);
////                               recipeButton.setPosition(this.guiLeft + 5, this.height / 2 - 90);
//                            recipeButton.setPosition(this.leftPos + 5, this.height / 2 - 66);
//                        }
//                    }
//            );
////            buttonList.add(this.recipeButton);
//            renderables.add(this.recipeButton);
//        }
    }

    @Override
//    public void updateScreen()
    public void tick() {
//        super.updateScreen();
        super.tick();
//        if (recipeBook != null) {
//            recipeBook.tick();
//        }
    }

    @Override
//    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        // Calen
        super.render(poseStack, mouseX, mouseY, partialTicks);

//        if (recipeBook == null) {
////            super.drawScreen(mouseX, mouseY, partialTicks);
//            super.render(poseStack, mouseX, mouseY, partialTicks);
//            return;
//        }
//        if (recipeBook.isVisible() && this.widthTooNarrow) {
////            drawDefaultBackground();
//            renderBackground(poseStack);
////            this.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
//            this.renderBg(poseStack, partialTicks, mouseX, mouseY);
//            recipeBook.render(poseStack, mouseX, mouseY, partialTicks);
////            renderHoveredToolTip(mouseX, mouseY);
//            renderTooltip(poseStack, mouseX, mouseY);
//        } else {
////            super.drawScreen(mouseX, mouseY, partialTicks);
//            super.render(poseStack, mouseX, mouseY, partialTicks);
//            recipeBook.render(poseStack, mouseX, mouseY, partialTicks);
////            recipeBook.renderGhostRecipe(this.guiLeft, this.guiTop, true, partialTicks);
//            recipeBook.renderGhostRecipe(poseStack, this.leftPos, this.topPos, true, partialTicks);
//        }
//
////        recipeBook.renderTooltip(this.guiLeft, this.guiTop, mouseX, mouseY);
//        recipeBook.renderTooltip(poseStack, this.leftPos, this.topPos, mouseX, mouseY);

//        recipeBook.render(poseStack, mouseX, mouseY, partialTicks);
//        recipeBook.renderGhostRecipe(poseStack, this.leftPos, this.topPos, true, partialTicks);
//        recipeBook.renderTooltip(poseStack, this.leftPos, this.topPos, mouseX, mouseY);
    }

    @Override
    protected void drawBackgroundLayer(float partialTicks, PoseStack poseStack) {
        ICON_GUI.drawAt(mainGui.rootElement, poseStack);

        double progress = container.tile.getProgress(partialTicks);

        drawProgress(RECT_PROGRESS, ICON_PROGRESS, poseStack, progress, 1);

        if (hasFilters()) {
//            RenderHelper.enableGUIStandardItemLighting();
            Lighting.setupForFlatItems();
            forEachFilter((slot, filterStack) ->
            {
//                int x = slot.xPos + (int) mainGui.rootElement.getX();
                int x = slot.x + (int) mainGui.rootElement.getX();
//                int y = slot.yPos + (int) mainGui.rootElement.getY();
                int y = slot.y + (int) mainGui.rootElement.getY();
//                itemRender.renderItemAndEffectIntoGUI(mc.player, filterStack, x, y);
                itemRenderer.renderAndDecorateItem(minecraft.player, filterStack, x, y, 0);
//                itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, filterStack, x, y, null);
                itemRenderer.renderGuiItemDecorations(minecraft.font, filterStack, x, y, null);
            });
//            RenderHelper.disableStandardItemLighting();

//            GlStateManager.disableDepth();
            RenderUtil.disableDepth();
            // Calen
            RenderUtil.enableBlend();
            RenderSystem.defaultBlendFunc();

            forEachFilter((slot, filterStack) ->
            {
//                ItemStack real = slot.getStack();
                ItemStack real = slot.getItem();
                final GuiIcon icon;
                if (real.isEmpty() || StackUtil.canMerge(real, filterStack)) {
                    icon = ICON_FILTER_OVERLAY_SAME;
                } else {
                    icon = ICON_FILTER_OVERLAY_DIFFERENT;
                }
//                int x = slot.xPos + (int) mainGui.rootElement.getX();
                int x = slot.x + (int) mainGui.rootElement.getX();
//                int y = slot.yPos + (int) mainGui.rootElement.getY();
                int y = slot.y + (int) mainGui.rootElement.getY();
                icon.drawAt(poseStack, x - 1, y - 1);
            });
//            GlStateManager.enableDepth();
            RenderUtil.enableDepth();
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

    // Calen: moved to ImageButton#<init> p_169018_
//    @Override
//    protected void actionPerformed(GuiButton button) throws IOException {
//        if (button == recipeButton && recipeBook != null) {
//            recipeBook.initVisuals(widthTooNarrow, container.tile.getWorkbenchCrafting());
//            recipeBook.toggleVisibility();
////            guiLeft = recipeBook.updateScreenPosition(widthTooNarrow, width, xSize);
//            leftPos = recipeBook.updateScreenPosition(width, imageWidth);
//            recipeButton.setPosition(this.guiLeft + 5, this.height / 2 - 66);
//        }
//    }

    @Override
//    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
//        if (recipeBook == null) {
//            return super.mouseClicked(mouseX, mouseY, mouseButton);
//        }
//        if (!recipeBook.mouseClicked(mouseX, mouseY, mouseButton)) {
//            if (!widthTooNarrow || !recipeBook.isVisible()) {
//                return super.mouseClicked(mouseX, mouseY, mouseButton);
//            }
//        }
//        return true;

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
//    protected void keyTyped(char typedChar, int keyCode) throws IOException
    public boolean keyPressed(int typedChar, int keyCode, int modifiers) {
//        if (recipeBook == null) {
////            super.keyTyped(typedChar, keyCode);
//            return super.keyPressed(typedChar, keyCode, modifiers);
////            return true;
//        }
////        if (!recipeBook.keyPressed(typedChar, keyCode, 0))
//        if (!recipeBook.keyPressed(typedChar, keyCode, modifiers)) {
////            super.keyTyped(typedChar, keyCode);
//            return super.keyPressed(typedChar, keyCode, modifiers);
//        }
//        return true;

        return super.keyPressed(typedChar, keyCode, modifiers);
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
//        if (recipeBook == null) {
////            super.keyTyped(typedChar, keyCode);
//            return super.charTyped(typedChar, keyCode);
//        }
//        if (!recipeBook.charTyped(typedChar, keyCode)) {
////            super.keyTyped(typedChar, keyCode);
//            return super.charTyped(typedChar, keyCode);
//        }
//        return true;

        return super.charTyped(typedChar, keyCode);
    }

    @Override
//    protected void handleMouseClick(Slot slot, int slotId, int mouseButton, ClickType type)
    protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type) {
//        super.handleMouseClick(slot, slotId, mouseButton, type);
        super.slotClicked(slot, slotId, mouseButton, type);
//        if (recipeBook != null) {
//            recipeBook.slotClicked(slot);
//        }
    }

    @Override
//    protected boolean isPointInRegion(int rectX, int rectY, int rectWidth, int rectHeight, int pointX, int pointY)
    protected boolean isHovering(int rectX, int rectY, int rectWidth, int rectHeight, double pointX, double pointY) {
//        if (recipeBook == null) {
////            return super.isPointInRegion(rectX, rectY, rectWidth, rectHeight, pointX, pointY);
//            return super.isHovering(rectX, rectY, rectWidth, rectHeight, pointX, pointY);
//        }
//        return (!widthTooNarrow || !recipeBook.isVisible())
////                && super.isPointInRegion(rectX, rectY, rectWidth, rectHeight, pointX, pointY);
//                && super.isHovering(rectX, rectY, rectWidth, rectHeight, pointX, pointY);


        return super.isHovering(rectX, rectY, rectWidth, rectHeight, pointX, pointY);
    }

    @Override
//    protected boolean hasClickedOutside(int mouseX, int mouseY, int _guiLeft, int _guiTop)
    protected boolean hasClickedOutside(double mouseX, double mouseY, int _guiLeft, int _guiTop, int p_97761_) {
//        if (recipeBook == null) {
////            return super.hasClickedOutside(mouseX, mouseY, _guiLeft, _guiTop);
//            return super.hasClickedOutside(mouseX, mouseY, _guiLeft, _guiTop, p_97761_);
//        }
//        boolean flag =
////                mouseX < _guiLeft || mouseY < _guiTop || mouseX >= _guiLeft + xSize || mouseY >= _guiTop + this.ySize;
//                mouseX < _guiLeft || mouseY < _guiTop || mouseX >= _guiLeft + imageWidth || mouseY >= _guiTop + this.imageHeight;
////        return recipeBook.hasClickedOutside(mouseX, mouseY, guiLeft, guiTop, xSize, ySize) && flag;
//        return recipeBook.hasClickedOutside(mouseX, mouseY, leftPos, topPos, imageWidth, imageHeight, p_97761_) && flag;

        return super.hasClickedOutside(mouseX, mouseY, _guiLeft, _guiTop, p_97761_);
    }

    @Override
//    public void onGuiClosed()
    public void onClose() {
//        if (recipeBook != null) {
//            recipeBook.removed();
//        }
//        super.onGuiClosed();
        super.onClose();
    }

    // IRecipeShownListener

//    @Override
//    public void recipesUpdated() {
//        if (recipeBook != null) {
//            recipeBook.recipesUpdated();
//        }
//    }

    @Override
//    public GuiRecipeBook func_194310_f()
    public void recipesShown(List<Recipe<?>> p_100518_) {
//        return recipeBook;
        // Calen: From RecipeBookComponent
        for (Recipe<?> recipe : p_100518_) {
            this.minecraft.player.removeRecipeHighlight(recipe);
        }
    }
}
