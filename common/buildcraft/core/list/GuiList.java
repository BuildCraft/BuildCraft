/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.core.list;

import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.lists.ListMatchHandler;

import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.button.GuiImageButton;
import buildcraft.lib.gui.button.IButtonBehaviour;
import buildcraft.lib.gui.button.IButtonClickEventListener;
import buildcraft.lib.gui.button.IButtonClickEventTrigger;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.list.ListHandler;
import buildcraft.lib.misc.StackUtil;

import buildcraft.core.BCCoreItems;
import buildcraft.core.item.ItemList_BC8;
import buildcraft.core.list.ContainerList.WidgetListSlot;

public class GuiList extends GuiBC8<ContainerList> implements IButtonClickEventListener {
    private static final ResourceLocation TEXTURE_BASE = new ResourceLocation("buildcraftcore:textures/gui/list_new.png");
    private static final int SIZE_X = 176, SIZE_Y = 191;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_HIGHLIGHT = new GuiIcon(TEXTURE_BASE, 176, 0, 16, 16);
    private static final GuiIcon ICON_ONE_STACK = new GuiIcon(TEXTURE_BASE, 0, 191, 20, 20);
    private static final int BUTTON_COUNT = 3;

    private final Map<Integer, Map<ListMatchHandler.Type, NonNullList<ItemStack>>> exampleCache = new HashMap<>();
    private GuiTextField textField;

    public GuiList(EntityPlayer iPlayer) {
        super(new ContainerList(iPlayer));
        xSize = SIZE_X;
        ySize = SIZE_Y;
    }

    @Override
    public void initGui() {
        super.initGui();

        for (int line = 0; line < container.slots.length; line++) {
            WidgetListSlot[] arr = container.slots[line];
            for (int slot = 0; slot < arr.length; slot++) {
                final WidgetListSlot listSlot = arr[slot];
                GuiRectangle rectangle = new GuiRectangle(8 + slot * 18, 32 + line * 34, 16, 16);

                guiElements.add(listSlot.new GuiElementPhantomSlot<GuiList>(this, rootElement, rectangle) {
                    @Override
                    protected boolean shouldDrawHighlight() {
                        return listSlot.slotIndex == 0 || !gui.container.lines[listSlot.lineIndex].isOneStackMode();
                    }

                    @Override
                    public void drawBackground(float partialTicks) {
                        if (!shouldDrawHighlight()) {
                            ICON_HIGHLIGHT.drawAt(this);
                        }
                    }

                    @Nonnull
                    @Override
                    public ItemStack getStack() {
                        if (shouldDrawHighlight()) {
                            return super.getStack();
                        } else {
                            NonNullList<ItemStack> data = gui.getExamplesList(listSlot.lineIndex, container.lines[listSlot.lineIndex].getSortingType());
                            if (data.size() >= listSlot.slotIndex) {
                                return data.get(listSlot.slotIndex - 1);
                            } else {
                                return StackUtil.EMPTY;
                            }
                        }
                    }

                    @Override
                    public void onMouseClicked(int button) {
                        super.onMouseClicked(button);
                        if (contains(gui.mouse)) {
                            clearExamplesCache(listSlot.lineIndex);
                        }
                    }
                });
            }
        }

        buttonList.clear();

        for (int sy = 0; sy < ListHandler.HEIGHT; sy++) {
            int bOff = sy * BUTTON_COUNT;
            int bOffX = this.guiLeft + 8 + ListHandler.WIDTH * 18 - BUTTON_COUNT * 11;
            int bOffY = this.guiTop + 32 + sy * 34 + 18;

            GuiImageButton buttonPrecise = new GuiImageButton(this, bOff + 0, bOffX, bOffY, 11, TEXTURE_BASE, 176, 16, 176, 28);
            buttonPrecise.setToolTip(ToolTip.createLocalized("gui.list.nbt")).setBehaviour(IButtonBehaviour.TOGGLE);
            buttonList.add(buttonPrecise);

            GuiImageButton buttonType = new GuiImageButton(this, bOff + 1, bOffX + 11, bOffY, 11, TEXTURE_BASE, 176, 16, 185, 28);
            buttonType.setToolTip(ToolTip.createLocalized("gui.list.metadata")).setBehaviour(IButtonBehaviour.TOGGLE);
            buttonList.add(buttonType);

            GuiImageButton buttonMaterial = new GuiImageButton(this, bOff + 2, bOffX + 22, bOffY, 11, TEXTURE_BASE, 176, 16, 194, 28);
            buttonMaterial.setToolTip(ToolTip.createLocalized("gui.list.oredict")).setBehaviour(IButtonBehaviour.TOGGLE);
            buttonList.add(buttonMaterial);
        }

        for (GuiButton o : buttonList) {
            GuiImageButton b = (GuiImageButton) o;
            int lineId = b.id / BUTTON_COUNT;
            int buttonId = b.id % BUTTON_COUNT;
            if (container.lines[lineId].getOption(buttonId)) {
                b.activate();
            }

            b.registerListener(this);
        }

        textField = new GuiTextField(6, this.fontRenderer, guiLeft + 10, guiTop + 10, 156, 12);
        textField.setMaxStringLength(32);
        textField.setText(BCCoreItems.list.getName(container.getListItemStack()));
        textField.setFocused(false);
    }

    @Override
    protected void drawBackgroundLayer(float partialTicks) {
        ICON_GUI.drawAt(rootElement);

        for (int i = 0; i < 2; i++) {
            if (container.lines[i].isOneStackMode()) {
                ICON_ONE_STACK.drawAt(guiLeft + 6, guiTop + 30 + i * 34);
            }
        }
    }

    @Override
    protected void drawForegroundLayer() {
        textField.drawTextBox();
    }

    private boolean isCarryingNonEmptyList() {
        ItemStack stack = mc.player.inventory.getItemStack();
        return !stack.isEmpty() && stack.getItem() instanceof ItemList_BC8 && stack.getTagCompound() != null;
    }

    private boolean hasListEquipped() {
        return !container.getListItemStack().isEmpty();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (textField.isFocused() && keyCode != Keyboard.KEY_ESCAPE) {
            textField.textboxKeyTyped(typedChar, keyCode);
            container.setLabel(textField.getText());
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void mouseClicked(int x, int y, int b) throws IOException {
        super.mouseClicked(x, y, b);

        if (isCarryingNonEmptyList() || !hasListEquipped()) {
            return;
        }

        textField.mouseClicked(x, y, b);
    }

    @Override
    public void handleButtonClick(IButtonClickEventTrigger sender, int id, int buttonKey) {
        int buttonId = id % BUTTON_COUNT;
        int lineId = id / BUTTON_COUNT;

        container.switchButton(lineId, buttonId);
        clearExamplesCache(lineId);
    }

    private void clearExamplesCache(int lineId) {
        Map<ListMatchHandler.Type, NonNullList<ItemStack>> exampleList = exampleCache.get(lineId);
        if (exampleList != null) {
            exampleList.clear();
        }
    }

    private NonNullList<ItemStack> getExamplesList(int lineId, ListMatchHandler.Type type) {
        Map<ListMatchHandler.Type, NonNullList<ItemStack>> exampleList = exampleCache.computeIfAbsent(
            lineId,
            k -> new EnumMap<>(ListMatchHandler.Type.class)
        );

        if (!exampleList.containsKey(type)) {
            NonNullList<ItemStack> examples = container.lines[lineId].getExamples();
            ItemStack input = container.lines[lineId].stacks.get(0);
            if (!input.isEmpty()) {
                NonNullList<ItemStack> repetitions = NonNullList.create();
                for (ItemStack is : examples) {
                    if (StackUtil.isMatchingItem(input, is, true, false)) {
                        repetitions.add(is);
                    }
                }
                examples.removeAll(repetitions);
            }
            exampleList.put(type, examples);
        }
        return exampleList.get(type);
    }
}
