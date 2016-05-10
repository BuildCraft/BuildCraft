/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.list;

import java.io.IOException;
import java.util.*;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.lists.ListMatchHandler;
import buildcraft.core.BCCoreItems;
import buildcraft.core.item.ItemList_BC8;
import buildcraft.core.lib.gui.buttons.GuiImageButton;
import buildcraft.core.lib.gui.buttons.IButtonClickEventListener;
import buildcraft.core.lib.gui.buttons.IButtonClickEventTrigger;
import buildcraft.core.lib.inventory.StackHelper;
import buildcraft.core.list.ContainerList.WidgetListSlot;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.GuiRectangle;
import buildcraft.lib.list.ListHandler;

public class GuiList extends GuiBC8<ContainerList> implements IButtonClickEventListener {
    private static final ResourceLocation TEXTURE_BASE = new ResourceLocation("buildcraftcore:textures/gui/list_new.png");
    private static final int SIZE_X = 176, SIZE_Y = 191;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_HIGHLIGHT = new GuiIcon(TEXTURE_BASE, 176, 0, 16, 16);
    private static final GuiIcon ICON_ONE_STACK = new GuiIcon(TEXTURE_BASE, 0, 191, 20, 20);
    private static final int BUTTON_COUNT = 3;

    private final Map<Integer, Map<ListMatchHandler.Type, List<ItemStack>>> exampleCache = new HashMap<>();
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

                guiElements.add(listSlot.new GuiElementPhantomSlot<GuiList, ContainerList>(this, rootElement, rectangle) {
                    @Override
                    protected boolean shouldDrawHighlight() {
                        return listSlot.slotIndex == 0 || !gui.container.lines[listSlot.lineIndex].isOneStackMode();
                    }

                    @Override
                    public void drawBackground() {
                        if (!shouldDrawHighlight()) {
                            ICON_HIGHLIGHT.draw(getX(), getY());
                        }
                    }

                    @Override
                    public ItemStack getStack() {
                        if (shouldDrawHighlight()) {
                            return super.getStack();
                        } else {
                            List<ItemStack> data = gui.getExamplesList(listSlot.lineIndex, container.lines[listSlot.lineIndex].getSortingType());
                            if (data.size() >= listSlot.slotIndex) {
                                return data.get(listSlot.slotIndex - 1);
                            } else {
                                return null;
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

            buttonList.add(new GuiImageButton(bOff + 0, bOffX, bOffY, 11, TEXTURE_BASE, 176, 16, 176, 28));
            buttonList.add(new GuiImageButton(bOff + 1, bOffX + 11, bOffY, 11, TEXTURE_BASE, 176, 16, 185, 28));
            buttonList.add(new GuiImageButton(bOff + 2, bOffX + 22, bOffY, 11, TEXTURE_BASE, 176, 16, 194, 28));
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

        textField = new GuiTextField(6, this.fontRendererObj, guiLeft + 10, guiTop + 10, 156, 12);
        textField.setMaxStringLength(32);
        textField.setText(BCCoreItems.list.getName(container.getListItemStack()));
        textField.setFocused(false);
    }

    @Override
    protected void drawBackgroundLayer(float partialTicks) {
        ICON_GUI.draw(guiLeft, guiTop);

        for (int i = 0; i < 2; i++) {
            if (container.lines[i].isOneStackMode()) {
                ICON_ONE_STACK.draw(guiLeft + 6, guiTop + 30 + i * 34);
            }
        }
    }

    @Override
    protected void drawForegroundLayer() {
        textField.drawTextBox();
    }

    private boolean isCarryingNonEmptyList() {
        ItemStack stack = mc.thePlayer.inventory.getItemStack();
        return stack != null && stack.getItem() instanceof ItemList_BC8 && stack.getTagCompound() != null;
    }

    private boolean hasListEquipped() {
        return container.getListItemStack() != null;
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
    public void handleButtonClick(IButtonClickEventTrigger sender, int id) {
        int buttonId = id % BUTTON_COUNT;
        int lineId = id / BUTTON_COUNT;

        container.switchButton(lineId, buttonId);
        clearExamplesCache(lineId);
    }

    private void clearExamplesCache(int lineId) {
        Map<ListMatchHandler.Type, List<ItemStack>> exampleList = exampleCache.get(lineId);
        if (exampleList != null) {
            exampleList.clear();
        }
    }

    private List<ItemStack> getExamplesList(int lineId, ListMatchHandler.Type type) {
        Map<ListMatchHandler.Type, List<ItemStack>> exampleList = exampleCache.get(lineId);
        if (exampleList == null) {
            exampleList = new EnumMap<>(ListMatchHandler.Type.class);
            exampleCache.put(lineId, exampleList);
        }

        if (!exampleList.containsKey(type)) {
            List<ItemStack> examples = container.lines[lineId].getExamples();
            ItemStack input = container.lines[lineId].stacks[0];
            if (input != null) {
                List<ItemStack> repetitions = new ArrayList<>();
                for (ItemStack is : examples) {
                    if (StackHelper.isMatchingItem(input, is, true, false)) {
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
