/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.gui;

import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.gui.pos.PositionAbsolute;
import buildcraft.robotics.container.ContainerRequester;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiRequester extends GuiBC8<ContainerRequester> {

    private static final ResourceLocation TEXTURE_BASE = new ResourceLocation("buildcraftrobotics:textures/gui/requester_gui.png");
    private static final int SIZE_X = 196, SIZE_Y = 181;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);

    private static final int SLOT_ROW_COUNT = 5;
    private static final int SLOT_IN_1_ROW = 4;

    // private TileRequester requester;

//    private static class RequestSlot extends AdvancedSlot {
//
//        private int index;
//
//        public RequestSlot(GuiAdvancedInterface gui, int iIndex, int x, int y) {
//            super(gui, x, y);
//
//            index = iIndex;
//        }
//
//        public void setItem(ItemStack itemStack) {
//            TileRequester requester = ((GuiRequester) gui).requester;
//
//            requester.setRequest(index, itemStack);
//            ((GuiRequester) gui).getContainer().getRequestList();
//        }
//
//        @Override
//        public ItemStack getItemStack() {
//            ContainerRequester requester = ((GuiRequester) gui).getContainer();
//
//            return requester.requests[index];
//        }
//    }

    // public GuiRequester(ContainerRequester container, Inventory inventory, Component component, TileRequester iRequester)
    public GuiRequester(ContainerRequester container, Inventory inventory, Component component) {
        super(container, inventory, component);

        // getContainer().gui = this;
        getMenu().getRequestList();

        imageWidth = SIZE_X;
        imageHeight = SIZE_Y;

        // requester = iRequester;

//        for (int x = 0; x < 4; ++x) {
//            for (int y = 0; y < 5; ++y) {
//                slots.add(new RequestSlot(this, x * 5 + y, 9 + 18 * x, 7 + 18 * y));
//            }
//        }
    }

    @Override
    protected void drawBackgroundLayer(float partialTicks, PoseStack poseStack) {
        super.drawBackgroundLayer(partialTicks, poseStack);

        // drawBackgroundSlots(x, y);
        ICON_GUI.drawAt(mainGui.rootElement, poseStack);
    }

//    @Override
//    protected void slotClicked(AdvancedSlot slot, int mouseButton) {
//        super.slotClicked(slot, mouseButton);
//
//        if (slot instanceof RequestSlot) {
//            ((RequestSlot) slot).setItem(mc.thePlayer.inventory.getItemStack());
//        }
//    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton == 0) {
            for (int i = 0; i < container.tile.requests.size(); i++) {
                if (getArea(i).contains(mouseX, mouseY)) {
                    this.container.tile.setRequest(i, minecraft.player.containerMenu.getCarried());
                    this.container.getRequestList();
                    break;
                }
            }
        }
        return true;
    }

    private IGuiArea getArea(int index) {
        return index < SLOT_IN_1_ROW * SLOT_ROW_COUNT
                ? new GuiRectangle(16, 16).offset(mainGui.rootElement).offset(getPos(index))
                : GuiRectangle.ZERO;
    }

    private IGuiPosition getPos(int index) {
        int posX = index % SLOT_IN_1_ROW;
        int posY = index / SLOT_IN_1_ROW;
        return new PositionAbsolute(9 + posX * 18, 7 + posY * 18);
    }
}
