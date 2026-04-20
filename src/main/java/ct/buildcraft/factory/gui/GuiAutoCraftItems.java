/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package ct.buildcraft.factory.gui;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.factory.container.ContainerAutoCraftItems;
import ct.buildcraft.lib.gui.GuiBC8;
import ct.buildcraft.lib.gui.GuiIcon;
import ct.buildcraft.lib.gui.ledger.LedgerHelp;
import ct.buildcraft.lib.gui.pos.GuiRectangle;
import ct.buildcraft.lib.gui.recipe.GuiRecipeBookPhantom;
import ct.buildcraft.lib.gui.slot.SlotBase;
import ct.buildcraft.lib.misc.StackUtil;
import ct.buildcraft.lib.tile.craft.WorkbenchCrafting;
import ct.buildcraft.lib.tile.item.ItemHandlerSimple;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.common.crafting.IShapedRecipe;

public class GuiAutoCraftItems extends GuiBC8<ContainerAutoCraftItems> implements RecipeUpdateListener {
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

    private final GuiRecipeBookPhantom recipeBook;
    /** If true then the recipe book will be drawn on top of this GUI, rather than beside it */
    private boolean widthTooNarrow;
    private ImageButton recipeButton;

    public GuiAutoCraftItems(ContainerAutoCraftItems container, Inventory inv, Component title) {
        super(container, inv, title);
        imageWidth = SIZE_X;
        imageHeight = SIZE_Y;
        GuiRecipeBookPhantom book;
        try {
            book = new GuiRecipeBookPhantom(this::sendRecipe);
        } catch (ReflectiveOperationException e) {
            BCLog.logger.warn("[factory.gui] An exception was thrown while creating the recipe book gui!", e);
            book = null;
        }
        recipeBook = book;
        mainGui.shownElements.add(new LedgerHelp(mainGui, true));
    }

    private void sendRecipe(Recipe<?> recipe) {
        List<ItemStack> stacks = new ArrayList<>(9);

        int maxX = recipe instanceof IShapedRecipe ? ((IShapedRecipe<?>) recipe).getRecipeWidth() : 3;
        int maxY = recipe instanceof IShapedRecipe ? ((IShapedRecipe<?>) recipe).getRecipeHeight() : 3;
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
    public void init() {
        super.init();
        widthTooNarrow = this.width < SIZE_X + 176;
        if (recipeBook != null) {
        	WorkbenchCrafting invCraft = container.tile.getWorkbenchCrafting();
            recipeBook.init(width, height, minecraft, widthTooNarrow, invCraft.getCraftingMenu(menu));
            leftPos = recipeBook.updateScreenPosition(width, imageWidth);
            recipeButton =
                new ImageButton(leftPos + 5, height / 2 - 66, 20, 18, 0, 168, 19, VANILLA_CRAFTING_TABLE, this::onPress);
            addRenderableWidget(recipeButton);
        }
    }

    @Override
    public void containerTick() {
        super.containerTick();
        if (recipeBook != null) {
            recipeBook.tick();
        }
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        if (recipeBook == null) {
            super.render(pose, mouseX, mouseY, partialTicks);
            return;
        }
        if (recipeBook.isVisible() && this.widthTooNarrow) {
            renderBackground(pose);
            this.drawBackgroundLayer(pose, mouseX, mouseY, partialTicks);
            recipeBook.render(pose, mouseX, mouseY, partialTicks);
            renderTooltip(pose, mouseX, mouseY);
        } else {
            super.render(pose, mouseX, mouseY, partialTicks);
            recipeBook.render(pose, mouseX, mouseY, partialTicks);
            recipeBook.renderGhostRecipe(pose, this.leftPos, this.topPos, true, partialTicks);
        }

        recipeBook.renderTooltip(pose, this.leftPos, this.topPos, mouseX, mouseY);
    }

    @Override
    protected void drawBackgroundLayer(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        ICON_GUI.drawAt(pose, mainGui.rootElement);

        double progress = container.tile.getProgress(partialTicks);

        drawProgress(pose, RECT_PROGRESS, ICON_PROGRESS, progress, 1);

        if (hasFilters()) {
//            RenderSystem.enableGUIStandardItemLighting();
            forEachFilter((slot, filterStack) -> {
                int x = slot.x + (int) mainGui.rootElement.getX();
                int y = slot.y + (int) mainGui.rootElement.getY();
                itemRenderer.renderAndDecorateItem(minecraft.player, filterStack, x, y, 0);
                itemRenderer.renderGuiItemDecorations(font, filterStack, x, y, null);
            });
//            RenderHelper.disableStandardItemLighting();

            RenderSystem.disableDepthTest();
            forEachFilter((slot, filterStack) -> {
                ItemStack real = slot.getItem();
                final GuiIcon icon;
                if (real.isEmpty() || StackUtil.canMerge(real, filterStack)) {
                    icon = ICON_FILTER_OVERLAY_SAME;
                } else {
                    icon = ICON_FILTER_OVERLAY_DIFFERENT;
                }
                int x = slot.x + (int) mainGui.rootElement.getX();
                int y = slot.y + (int) mainGui.rootElement.getY();
                icon.drawAt(pose, x - 1, y - 1);
            });
            RenderSystem.enableDepthTest();
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

    protected void onPress(Button button){
        if (button == recipeButton && recipeBook != null) {
            recipeBook.initVisuals();
            recipeBook.toggleVisibility();
            leftPos = recipeBook.updateScreenPosition(width, imageWidth);
            recipeButton.setPosition(this.leftPos + 5, this.height / 2 - 66);
        }
    }

    @Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton){
        if (recipeBook == null) {
            return super.mouseClicked(mouseX, mouseY, mouseButton);
        }
        if (!recipeBook.mouseClicked(mouseX, mouseY, mouseButton)) {
            if (!widthTooNarrow || !recipeBook.isVisible()) {
                return super.mouseClicked(mouseX, mouseY, mouseButton);
            }
            return false;
        }
        return true;
    }

    @Override
	public boolean keyPressed(int a, int b, int c) {
        if (recipeBook == null) {
        	return super.keyPressed(a, b, c);
        }
        if (!recipeBook.keyPressed(a, b, c)) {
        	return super.keyPressed(a, b, c);
        }
        return true;
	}

	@Override
    protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type) {
        super.slotClicked(slot, slotId, mouseButton, type);
        if (recipeBook != null) {
            recipeBook.slotClicked(slot);
        }
    }
	
	@Override
	protected boolean isHovering(int rectX, int rectY, int rectWidth, int rectHeight, double pointX, double pointY) {
        if (recipeBook == null) {
            return super.isHovering(rectX, rectY, rectWidth, rectHeight, pointX, pointY);
        }
        return (!widthTooNarrow || !recipeBook.isVisible())
            && super.isHovering(rectX, rectY, rectWidth, rectHeight, pointX, pointY);
	}

    @Override
	protected boolean hasClickedOutside(double mouseX, double mouseY, int _guiLeft, int _guiTop, int p_97761_) {
        if (recipeBook == null) {
            return super.hasClickedOutside(mouseX, mouseY, _guiLeft, _guiTop, p_97761_);
        }
        boolean flag =
            mouseX < _guiLeft || mouseY < _guiTop || mouseX >= _guiLeft + imageWidth || mouseY >= _guiTop + imageHeight;
        return recipeBook.hasClickedOutside(mouseX, mouseY, leftPos, topPos, imageWidth, imageHeight, p_97761_) && flag;
	}

    @Override
    public void onClose() {
        if (recipeBook != null) {
            recipeBook.removed();
        }
        super.onClose();
    }

    // IRecipeShownListener

    @Override
    public void recipesUpdated() {
        if (recipeBook != null) {
            recipeBook.recipesUpdated();
        }
    }

    @Override
    public RecipeBookComponent getRecipeBookComponent() {
        return recipeBook;
    }
}
