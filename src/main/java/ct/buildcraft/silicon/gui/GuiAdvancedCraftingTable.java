/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package ct.buildcraft.silicon.gui;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.lib.gui.GuiBC8;
import ct.buildcraft.lib.gui.GuiIcon;
import ct.buildcraft.lib.gui.ledger.LedgerHelp;
import ct.buildcraft.lib.gui.pos.GuiRectangle;
import ct.buildcraft.lib.gui.recipe.GuiRecipeBookPhantom;
import ct.buildcraft.silicon.container.ContainerAdvancedCraftingTable;
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

public class GuiAdvancedCraftingTable extends GuiBC8<ContainerAdvancedCraftingTable> implements RecipeUpdateListener {
    private static final ResourceLocation TEXTURE_BASE = new ResourceLocation("buildcraftsilicon:textures/gui/advanced_crafting_table.png");
    private static final ResourceLocation VANILLA_CRAFTING_TABLE = new ResourceLocation("textures/gui/container/crafting_table.png");
    private static final int SIZE_X = 176, SIZE_Y = 241;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_PROGRESS = new GuiIcon(TEXTURE_BASE, SIZE_X, 0, 4, 70);
    private static final GuiRectangle RECT_PROGRESS = new GuiRectangle(164, 7, 4, 70);

    private final GuiRecipeBookPhantom recipeBook;
    /** If true then the recipe book will be drawn on top of this GUI, rather than beside it */
    private boolean widthTooNarrow;
    private ImageButton recipeButton;

    public GuiAdvancedCraftingTable(ContainerAdvancedCraftingTable container, Inventory inv, Component title) {
        super(container, inv, title);
        imageWidth = SIZE_X;
        imageHeight = SIZE_Y;
        GuiRecipeBookPhantom book;
        try {
            book = new GuiRecipeBookPhantom(this::sendRecipe);
        } catch (ReflectiveOperationException e) {
            BCLog.logger.warn("[silicon.gui] An exception was thrown while creating the recipe book gui!", e);
            book = null;
        }
        recipeBook = null;//= book; TODO
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
            recipeBook.init(width, height, minecraft, widthTooNarrow, container.tile.getWorkbenchCrafting().getCraftingMenu(menu));
            leftPos = recipeBook.updateScreenPosition(width, imageWidth);
            recipeButton =
                new ImageButton(leftPos + 5, height / 2 - 90, 20, 18, 0, 168, 19, VANILLA_CRAFTING_TABLE, this::onPress);
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

        long target = container.tile.getTarget();
        if (target != 0) {
            double v = (double) container.tile.power / target;
            ICON_PROGRESS.drawCutInside(
                    pose, new GuiRectangle(
                            RECT_PROGRESS.x,
                            (int) (RECT_PROGRESS.y + RECT_PROGRESS.height * Math.max(1 - v, 0)),
                            RECT_PROGRESS.width,
                            (int) Math.ceil(RECT_PROGRESS.height * Math.min(v, 1))
                    ).offset(mainGui.rootElement)
            );
        }
	}

    @Override
	protected void drawForegroundLayer(PoseStack pose, int mouseX, int mouseY) {
        //String title = I18n.format("tile." + TagManager.getTag("block.advanced_crafting_table", TagManager.EnumTagType.UNLOCALIZED_NAME) + ".name");
        //font.drawString(title, titleLabelX + (imageWidth - font.getStringWidth(title)) / 2, titleLabelY + 5, 0x404040);
	}

    protected void onPress(Button button){
        if (button == recipeButton && recipeBook != null) {
            recipeBook.initVisuals();
            recipeBook.toggleVisibility();
            leftPos = recipeBook.updateScreenPosition(width, imageWidth);
            recipeButton.setPosition(this.leftPos + 5, this.height / 2 - 90);
        }
    }
    
    @Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
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

    // RecipeUpdateListener

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
