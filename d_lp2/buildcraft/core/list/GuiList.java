/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.list;

import java.io.IOException;
import java.util.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.lists.ListMatchHandler;
import buildcraft.core.BCCoreItems;
import buildcraft.core.item.ItemList_BC8;
import buildcraft.core.lib.gui.AdvancedSlot;
import buildcraft.core.lib.gui.GuiAdvancedInterface;
import buildcraft.core.lib.gui.buttons.GuiImageButton;
import buildcraft.core.lib.gui.buttons.IButtonClickEventListener;
import buildcraft.core.lib.gui.buttons.IButtonClickEventTrigger;
import buildcraft.core.lib.inventory.StackHelper;
import buildcraft.lib.list.ListHandler;

public class GuiList extends GuiAdvancedInterface implements IButtonClickEventListener {
    private static final ResourceLocation TEXTURE_BASE = new ResourceLocation("buildcraftcore:textures/gui/list_new.png");
    private static final int BUTTON_COUNT = 3;

    private final Map<Integer, Map<ListMatchHandler.Type, List<ItemStack>>> exampleCache = new HashMap<>();
    private GuiTextField textField;
    private final ContainerList containerList;

    private class ListSlot extends AdvancedSlot {
        public int lineIndex;
        public int slotIndex;

        public ListSlot(int x, int y, int iLineIndex, int iSlotIndex) {
            super(GuiList.this, x, y);

            lineIndex = iLineIndex;
            slotIndex = iSlotIndex;
        }

        @Override
        public ItemStack getItemStack() {
            ContainerList container = GuiList.this.containerList;
            if (slotIndex == 0 || !container.lines[lineIndex].isOneStackMode()) {
                return container.lines[lineIndex].getStack(slotIndex);
            } else {
                List<ItemStack> data = GuiList.this.getExamplesList(lineIndex, container.lines[lineIndex].getSortingType());
                if (data.size() >= slotIndex) {
                    return data.get(slotIndex - 1);
                } else {
                    return null;
                }
            }
        }

        @Override
        public void drawSprite(int cornerX, int cornerY) {
            if (!shouldDrawHighlight()) {
                Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURE_BASE);
                gui.drawTexturedModalRect(cornerX + x, cornerY + y, 176, 0, 16, 16);
            }

            super.drawSprite(cornerX, cornerY);
        }

        @Override
        public boolean shouldDrawHighlight() {
            ContainerList container = GuiList.this.containerList;
            return slotIndex == 0 || !container.lines[lineIndex].isOneStackMode();
        }
    }

    public GuiList(EntityPlayer iPlayer) {
        super(new ContainerList(iPlayer), iPlayer.inventory, TEXTURE_BASE);
        containerList = (ContainerList) getContainer();
        xSize = 176;
        ySize = 191;
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
            List<ItemStack> examples = containerList.lines[lineId].getExamples();
            ItemStack input = containerList.lines[lineId].stacks[0];
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

    @Override
    public void initGui() {
        super.initGui();

        exampleCache.clear();
        slots.clear();
        buttonList.clear();

        for (int sy = 0; sy < ListHandler.HEIGHT; sy++) {
            for (int sx = 0; sx < ListHandler.WIDTH; sx++) {
                slots.add(new ListSlot(8 + sx * 18, 32 + sy * 33, sy, sx));
            }
            int bOff = sy * BUTTON_COUNT;
            int bOffX = this.guiLeft + 8 + ListHandler.WIDTH * 18 - BUTTON_COUNT * 11;
            int bOffY = this.guiTop + 32 + sy * 33 + 18;

            buttonList.add(new GuiImageButton(bOff + 0, bOffX, bOffY, 11, TEXTURE_BASE, 176, 16, 176, 28));
            buttonList.add(new GuiImageButton(bOff + 1, bOffX + 11, bOffY, 11, TEXTURE_BASE, 176, 16, 185, 28));
            buttonList.add(new GuiImageButton(bOff + 2, bOffX + 22, bOffY, 11, TEXTURE_BASE, 176, 16, 194, 28));
        }

        for (GuiButton o : buttonList) {
            GuiImageButton b = (GuiImageButton) o;
            int lineId = b.id / BUTTON_COUNT;
            int buttonId = b.id % BUTTON_COUNT;
            if (((ContainerList) getContainer()).lines[lineId].getOption(buttonId)) {
                b.activate();
            }

            b.registerListener(this);
        }

        textField = new GuiTextField(6, this.fontRendererObj, 10, 10, 156, 12);
        textField.setMaxStringLength(32);
        textField.setText(BCCoreItems.list.getName(containerList.getListItemStack()));
        textField.setFocused(false);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
        super.drawGuiContainerBackgroundLayer(f, x, y);

        for (int i = 0; i < 2; i++) {
            if (containerList.lines[i].isOneStackMode()) {
                drawTexturedModalRect(guiLeft + 6, guiTop + 30 + i * 33, 0, ySize, 20, 20);
            }
        }

        drawBackgroundSlots(x, y);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2) {
        super.drawGuiContainerForegroundLayer(par1, par2);

        textField.drawTextBox();
        drawTooltipForSlotAt(par1, par2);
    }

    private boolean isCarryingNonEmptyList() {
        ItemStack stack = mc.thePlayer.inventory.getItemStack();
        return stack != null && stack.getItem() instanceof ItemList_BC8 && stack.getTagCompound() != null;
    }

    private boolean hasListEquipped() {
        return containerList.getListItemStack() != null;
    }

    @Override
    protected void mouseClicked(int x, int y, int b) throws IOException {
        super.mouseClicked(x, y, b);

        if (isCarryingNonEmptyList() || !hasListEquipped()) {
            return;
        }

        AdvancedSlot slot = getSlotAtLocation(x, y);
        ContainerList container = (ContainerList) getContainer();

        if (slot instanceof ListSlot) {
            container.setStack(((ListSlot) slot).lineIndex, ((ListSlot) slot).slotIndex, mc.thePlayer.inventory.getItemStack());
            clearExamplesCache(((ListSlot) slot).lineIndex);
        }

        textField.mouseClicked(x - guiLeft, y - guiTop, b);
    }

    @Override
    public void handleButtonClick(IButtonClickEventTrigger sender, int id) {
        int buttonId = id % BUTTON_COUNT;
        int lineId = id / BUTTON_COUNT;

        ContainerList container = (ContainerList) getContainer();
        container.switchButton(lineId, buttonId);
        clearExamplesCache(lineId);
    }

    @Override
    protected void keyTyped(char c, int i) throws IOException {
        if (textField.isFocused()) {
            if (c == 13 || c == 27) {
                textField.setFocused(false);
            } else {
                textField.textboxKeyTyped(c, i);
                ((ContainerList) container).setLabel(textField.getText());
            }
        } else {
            super.keyTyped(c, i);
        }
    }
}
