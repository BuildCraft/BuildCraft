/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.gui;

import java.io.IOException;

import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import buildcraft.core.BuildCraftCore;
import buildcraft.core.item.ItemList;
import buildcraft.core.lib.gui.AdvancedSlot;
import buildcraft.core.lib.gui.GuiAdvancedInterface;

public class GuiList extends GuiAdvancedInterface {

    private static final ResourceLocation TEXTURE_BASE = new ResourceLocation("buildcraftcore:textures/gui/list.png");

    private GuiTextField textField;
    private EntityPlayer player;

    private static class MainSlot extends AdvancedSlot {
        public int lineIndex;

        public MainSlot(GuiAdvancedInterface gui, int x, int y, int iLineIndex) {
            super(gui, x, y);

            lineIndex = iLineIndex;
        }

        @Override
        public ItemStack getItemStack() {
            ContainerList container = (ContainerList) gui.getContainer();

            if (container.lines[lineIndex].getStack(0) != null) {
                return container.lines[lineIndex].getStack(0);
            } else {
                return null;
            }
        }
    }

    private static class SecondarySlot extends AdvancedSlot {
        public int lineIndex;
        public int slotIndex;

        public SecondarySlot(GuiAdvancedInterface gui, int x, int y, int iLineIndex, int iSlotIndex) {
            super(gui, x, y);

            lineIndex = iLineIndex;
            slotIndex = iSlotIndex;
        }

        @Override
        public ItemStack getItemStack() {
            ContainerList container = (ContainerList) gui.getContainer();

            if (slotIndex == 6 && container.lines[lineIndex].getStack(7) != null) {
                return null;
            }

            if (container.lines[lineIndex].getStack(slotIndex) != null) {
                return container.lines[lineIndex].getStack(slotIndex);
            } else {
                return null;
            }
        }
    }

    private static class Button extends AdvancedSlot {

        public int line;
        public int kind;
        private String desc;

        public Button(GuiAdvancedInterface gui, int x, int y, int iLine, int iKind, String iDesc) {
            super(gui, x, y);

            line = iLine;
            kind = iKind;
            desc = iDesc;
        }

        @Override
        public String getDescription() {
            return desc;
        }
    }

    public GuiList(EntityPlayer iPlayer) {
        super(new ContainerList(iPlayer), iPlayer.inventory, TEXTURE_BASE);

        xSize = 176;
        ySize = 241;

        for (int sy = 0; sy < 6; ++sy) {
            slots.add(new MainSlot(this, 44, 31 + sy * 18, sy));

            for (int sx = 1; sx < 7; ++sx) {
                slots.add(new SecondarySlot(this, 44 + sx * 18, 31 + sy * 18, sy, sx));
            }

            slots.add(new Button(this, 8, 31 + sy * 18, sy, 0, "gui.list.metadata"));
            slots.add(new Button(this, 26, 31 + sy * 18, sy, 1, "gui.list.oredict"));
        }

        player = iPlayer;
    }

    @Override
    public void initGui() {
        super.initGui();

        textField = new GuiTextField(0, this.fontRendererObj, 10, 10, 156, 12);
        textField.setMaxStringLength(32);
        textField.setText(BuildCraftCore.listItem.getLabel(player.getCurrentEquippedItem()));
        textField.setFocused(false);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
        super.drawGuiContainerBackgroundLayer(f, x, y);

        ContainerList container = (ContainerList) getContainer();

        bindTexture(TEXTURE_BASE);

        for (int i = 0; i < 6; ++i) {
            if (container.lines[i].subitemsWildcard) {
                drawTexturedModalRect(guiLeft + 7, guiTop + 30 + 18 * i, 194, 18, 18, 18);
            } else {
                drawTexturedModalRect(guiLeft + 7, guiTop + 30 + 18 * i, 194, 0, 18, 18);
            }

            if (container.lines[i].isOre) {
                if (container.lines[i].oreWildcard) {
                    drawTexturedModalRect(guiLeft + 25, guiTop + 30 + 18 * i, 176, 18, 18, 18);
                } else {
                    drawTexturedModalRect(guiLeft + 25, guiTop + 30 + 18 * i, 176, 0, 18, 18);
                }
            }

            if (container.lines[i].subitemsWildcard || container.lines[i].oreWildcard) {
                for (int j = 0; j < 6; ++j) {
                    drawTexturedModalRect(guiLeft + 62 + 18 * j, guiTop + 31 + 18 * i, 195, 37, 16, 16);
                }
            }
        }

        drawBackgroundSlots();

        bindTexture(TEXTURE_BASE);

        for (int i = 0; i < 6; ++i) {
            if (container.lines[i].getStack(7) != null) {
                drawTexturedModalRect(guiLeft + 152, guiTop + 31 + 18 * i, 177, 37, 16, 16);
            }
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2) {
        super.drawGuiContainerForegroundLayer(par1, par2);

        textField.drawTextBox();

        drawTooltipForSlotAt(par1, par2);
    }

    private boolean isCarryingList() {
        ItemStack stack = mc.thePlayer.inventory.getItemStack();
        return stack != null && stack.getItem() instanceof ItemList;
    }

    private boolean hasListEquipped() {
        return mc.thePlayer.getCurrentEquippedItem() != null && mc.thePlayer.getCurrentEquippedItem().getItem() instanceof ItemList;
    }

    @Override
    protected void mouseClicked(int x, int y, int b) throws IOException {
        super.mouseClicked(x, y, b);

        if (isCarryingList() || !hasListEquipped()) {
            return;
        }

        AdvancedSlot slot = getSlotAtLocation(x, y);
        ContainerList container = (ContainerList) getContainer();

        if (slot instanceof MainSlot) {
            container.setStack(((MainSlot) slot).lineIndex, 0, mc.thePlayer.inventory.getItemStack());
        } else if (slot instanceof SecondarySlot) {
            container.setStack(((SecondarySlot) slot).lineIndex, ((SecondarySlot) slot).slotIndex, mc.thePlayer.inventory.getItemStack());
        } else if (slot instanceof Button) {
            Button button = (Button) slot;

            container.switchButton(button.line, button.kind);
        }

        textField.mouseClicked(x - guiLeft, y - guiTop, b);
    }

    @Override
    protected void keyTyped(char c, int i) throws IOException {
        if (textField.isFocused()) {
            if (c == 13 || c == 27) {
                textField.setFocused(false);
            } else {
                textField.textboxKeyTyped(c, i);
                ((ContainerList) container).setLabel(textField.getText());
                // RPCHandler.rpcServer(architect, "handleClientSetName",
                // textField.getText());
            }
        } else {
            super.keyTyped(c, i);
        }
    }
}
