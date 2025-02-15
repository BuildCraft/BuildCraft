/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.list;


import buildcraft.api.lists.ListMatchHandler;
import buildcraft.core.BCCoreItems;
import buildcraft.core.item.ItemList_BC8;
import buildcraft.core.list.ContainerList.WidgetListSlot;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.button.GuiImageButton;
import buildcraft.lib.gui.button.IButtonBehaviour;
import buildcraft.lib.gui.button.IButtonClickEventListener;
import buildcraft.lib.gui.button.IButtonClickEventTrigger;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.list.ListHandler;
import buildcraft.lib.misc.StackUtil;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleSupplier;

public class GuiList extends GuiBC8<ContainerList> implements IButtonClickEventListener {
    private static final ResourceLocation TEXTURE_BASE =
            new ResourceLocation("buildcraftcore:textures/gui/list_new.png");
    private static final int SIZE_X = 176, SIZE_Y = 191;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_HIGHLIGHT = new GuiIcon(TEXTURE_BASE, 176, 0, 16, 16);
    private static final GuiIcon ICON_ONE_STACK = new GuiIcon(TEXTURE_BASE, 0, 191, 20, 20);
    private static final int BUTTON_COUNT = 3;

    private final Map<Integer, Map<ListMatchHandler.Type, NonNullList<ItemStack>>> exampleCache = new HashMap<>();
    // private GuiTextField textField;
    private EditBox textField;

    public GuiList(ContainerList container, Inventory inventory, Component component) {
//        super(new ContainerList(iPlayer));
        super(container, inventory, component);
//        xSize = SIZE_X;
        imageWidth = SIZE_X;
//        ySize = SIZE_Y;
        imageHeight = SIZE_Y;
    }

    @Override
    public void initGui() {
        super.initGui();

        for (int line = 0; line < container.slots.length; line++) {
            WidgetListSlot[] arr = container.slots[line];
            for (int slot = 0; slot < arr.length; slot++) {
                final WidgetListSlot listSlot = arr[slot];
                GuiRectangle rectangle = new GuiRectangle(8 + slot * 18, 32 + line * 34, 16, 16);

                IGuiArea phantomSlotArea = rectangle.offset(mainGui.rootElement);
                mainGui.shownElements.add(listSlot.new GuiElementPhantomSlot(mainGui, phantomSlotArea) {
                    @Override
                    protected boolean shouldDrawHighlight() {
                        if (listSlot.slotIndex == 0) {
                            return true;
                        }
                        return !GuiList.this.container.lines[listSlot.lineIndex].isOneStackMode();
                    }

                    @Override
                    public void drawBackground(float partialTicks, GuiGraphics guiGraphics) {
                        if (!shouldDrawHighlight()) {
                            ICON_HIGHLIGHT.drawAt(this, guiGraphics);
                        }
                    }

                    @Nonnull
                    @Override
                    public ItemStack getStack() {
                        if (shouldDrawHighlight()) {
                            return super.getStack();
                        } else {
                            NonNullList<ItemStack> data = GuiList.this.getExamplesList(listSlot.lineIndex,
                                    container.lines[listSlot.lineIndex].getSortingType());
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

//        buttonList.clear();
        renderables.clear();

        for (int sy = 0; sy < ListHandler.HEIGHT; sy++) {
            int bOff = sy * BUTTON_COUNT;
//            int bOffX = this.guiLeft + 8 + ListHandler.WIDTH * 18 - BUTTON_COUNT * 11;
            DoubleSupplier bOffX = () -> this.leftPos + 8 + ListHandler.WIDTH * 18 - BUTTON_COUNT * 11;
//            int bOffY = this.guiTop + 32 + sy * 34 + 18;
            int sy_final = sy;
            DoubleSupplier bOffY = () -> this.topPos + 32 + sy_final * 34 + 18;

            GuiImageButton buttonPrecise =
//                    new GuiImageButton(mainGui, bOff + 0, bOffX, bOffY, 11, TEXTURE_BASE, 176, 16, 176, 28);
                    new GuiImageButton(mainGui, bOff + 0, bOffX, bOffY, () -> 11, TEXTURE_BASE, 176, 16, 176, 28);
            buttonPrecise.setToolTip(ToolTip.createLocalized("gui.list.nbt"));
            buttonPrecise.setBehaviour(IButtonBehaviour.TOGGLE);
            mainGui.shownElements.add(buttonPrecise);

            GuiImageButton buttonType =
//                    new GuiImageButton(mainGui, bOff + 1, bOffX + 11, bOffY, 11, TEXTURE_BASE, 176, 16, 185, 28);
                    new GuiImageButton(mainGui, bOff + 1, () -> bOffX.getAsDouble() + 11, bOffY, () -> 11, TEXTURE_BASE, 176, 16, 185, 28);
            buttonType.setToolTip(ToolTip.createLocalized("gui.list.metadata"));
            buttonType.setBehaviour(IButtonBehaviour.TOGGLE);
            mainGui.shownElements.add(buttonType);

            GuiImageButton buttonMaterial =
//                    new GuiImageButton(mainGui, bOff + 2, bOffX + 22, bOffY, 11, TEXTURE_BASE, 176, 16, 194, 28);
                    new GuiImageButton(mainGui, bOff + 2, () -> bOffX.getAsDouble() + 22, bOffY, () -> 11, TEXTURE_BASE, 176, 16, 194, 28);
            buttonMaterial.setToolTip(ToolTip.createLocalized("gui.list.oredict"));
            buttonMaterial.setBehaviour(IButtonBehaviour.TOGGLE);
            mainGui.shownElements.add(buttonMaterial);
        }

        for (IGuiElement elem : mainGui.shownElements) {
            if (elem instanceof GuiImageButton b) {
                int id = Integer.parseInt(b.id);
                int lineId = id / BUTTON_COUNT;
                int buttonId = id % BUTTON_COUNT;
                if (container.lines[lineId].getOption(buttonId)) {
                    b.activate();
                }

                b.registerListener(this);
            }
        }

//        textField = new GuiTextField(6, this.fontRenderer, guiLeft + 10, guiTop + 10, 156, 12);
//        textField = new EditBox(this.font, leftPos + 10, topPos + 10, 156, 12, Component.literal(""));
        textField = new EditBox(this.font, 10, 10, 156, 12, Component.literal(""));
//        textField.setMaxStringLength(32);
        textField.setMaxLength(32);
//        textField.setText(BCCoreItems.list.getName(container.getListItemStack()));
        textField.setValue(BCCoreItems.list.get().getName_INamedItem(container.getListItemStack()));
        textField.setFocused(false);
    }

    @Override
    protected void drawBackgroundLayer(float partialTicks, GuiGraphics guiGraphics) {
        ICON_GUI.drawAt(mainGui.rootElement, guiGraphics);

        for (int i = 0; i < 2; i++) {
            if (container.lines[i].isOneStackMode()) {
//                ICON_ONE_STACK.drawAt(guiLeft + 6, guiTop + 30 + i * 34);
                ICON_ONE_STACK.drawAt(guiGraphics, leftPos + 6, topPos + 30 + i * 34);
            }
        }
    }

    @Override
    protected void drawForegroundLayer(GuiGraphics guiGraphics) {
//        textField.drawTextBox();
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(leftPos, topPos, 0);
        textField.renderWidget(guiGraphics, 0, 0, Minecraft.getInstance().getFrameTime());
        poseStack.popPose();
    }

    private boolean isCarryingNonEmptyList() {
//        ItemStack stack = mc.player.inventory.getItemStack();
        ItemStack stack = minecraft.player.inventoryMenu.getCarried();
//        return !stack.isEmpty() && stack.getItem() instanceof ItemList_BC8 && stack.getTagCompound() != null;
        return !stack.isEmpty() && stack.getItem() instanceof ItemList_BC8 && stack.getTag() != null;
    }

    private boolean hasListEquipped() {
        return !container.getListItemStack().isEmpty();
    }

    @Override
//    protected void keyTyped(char typedChar, int keyCode) throws IOException
    public boolean keyPressed(int typedChar, int keyCode, int modifiers) {
        boolean handled = false;
//        if (textField.isFocused() && keyCode != Keyboard.KEY_ESCAPE)
        if (textField.isFocused() && typedChar != InputConstants.KEY_ESCAPE) {
//            textField.textboxKeyTyped(typedChar, keyCode);
            handled = textField.keyPressed(typedChar, keyCode, modifiers);
//            container.setLabel(textField.getText());
            container.setLabel(textField.getValue());

            return handled;
        } else {
//            super.keyTyped(typedChar, keyCode);
            return super.keyPressed(typedChar, keyCode, modifiers);
        }
    }

    public boolean charTyped(char typedChar, int keyCode) {
        boolean handled = false;
//        if (textField.isFocused() && keyCode != Keyboard.KEY_ESCAPE)
        if (textField.isFocused() && typedChar != InputConstants.KEY_ESCAPE) {
//            textField.textboxKeyTyped(typedChar, keyCode);
            handled = textField.charTyped(typedChar, keyCode);
//            container.setLabel(textField.getText());
            container.setLabel(textField.getValue());
            return handled;
        } else {
//            super.keyTyped(typedChar, keyCode);
            return super.charTyped(typedChar, keyCode);
        }
    }

    @Override
//    protected void mouseClicked(int x, int y, int b) throws IOException
    public boolean mouseClicked(double x, double y, int b) {
        super.mouseClicked(x, y, b);

        if (isCarryingNonEmptyList() || !hasListEquipped()) {
//            return;
            return true;
        }

//        textField.mouseClicked(x, y, b);
        return textField.mouseClicked(x - leftPos, y - topPos, b);

    }

    @Override
    public void handleButtonClick(IButtonClickEventTrigger sender, int buttonKey) {
        if (!(sender instanceof GuiImageButton)) {
            return;
        }
        int id = Integer.parseInt(((GuiImageButton) sender).id);
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
        Map<ListMatchHandler.Type, NonNullList<ItemStack>> exampleList =
                exampleCache.computeIfAbsent(lineId, k -> new EnumMap<>(ListMatchHandler.Type.class));

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

    // Calen 1.20.1
    public void clientSetStackToServer(WidgetListSlot slot, ItemStack stack) {
        slot.clientSetStackToServer(stack);
        this.clearExamplesCache(slot.lineIndex);
    }
}
