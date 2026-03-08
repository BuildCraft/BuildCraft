/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.gui;

import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.gui.pos.PositionAbsolute;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.RenderUtil;
import buildcraft.silicon.container.ContainerProgrammingTable_Neptune;
import buildcraft.silicon.tile.TileProgrammingTable_Neptune;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiProgrammingTable_Neptune extends GuiBC8<ContainerProgrammingTable_Neptune> {

    private static final ResourceLocation TEXTURE_BASE = new ResourceLocation("buildcraftsilicon:textures/gui/programming_table.png");
    private static final int SIZE_X = 176, SIZE_Y = 207;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_PROGRESS = new GuiIcon(TEXTURE_BASE, SIZE_X, 18, 4, 70);
    private static final GuiRectangle RECT_PROGRESS = new GuiRectangle(164, 36, 4, 70);
    private static final GuiIcon ICON_SAVED_ENOUGH_ACTIVE = new GuiIcon(TEXTURE_BASE, 196, 1, 16, 16);

//    private class LaserTableLedger extends Ledger {
//
//        int headerColour = 0xe1c92f;
//        int subheaderColour = 0xaaafb8;
//        int textColour = 0x000000;
//
//        public LaserTableLedger() {
//            maxHeight = 94;
//            overlayColor = 0xd46c1f;
//        }
//
//        @Override
//        public void draw(int x, int y) {
//
//            // Draw background
//            drawBackground(x, y);
//
//            // Draw icon
//            Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
//            drawIcon(CoreIconProvider.ENERGY.getSprite(), x + 3, y + 4);
//
//            if (!isFullyOpened()) {
//                return;
//            }
//
//            fontRendererObj.drawStringWithShadow(BCStringUtils.localize("gui.energy"), x + 22, y + 8, headerColour);
//            fontRendererObj.drawStringWithShadow(BCStringUtils.localize("gui.assemblyCurrentRequired") + ":", x + 22, y + 20, subheaderColour);
//            fontRendererObj.drawString(String.format("%d RF", table.clientRequiredEnergy), x + 22, y + 32, textColour);
//            fontRendererObj.drawStringWithShadow(BCStringUtils.localize("gui.stored") + ":", x + 22, y + 44, subheaderColour);
//            fontRendererObj.drawString(String.format("%d RF", table.getEnergy()), x + 22, y + 56, textColour);
//            fontRendererObj.drawStringWithShadow(BCStringUtils.localize("gui.assemblyRate") + ":", x + 22, y + 68, subheaderColour);
//            fontRendererObj.drawString(String.format("%.1f RF/t", table.getRecentEnergyAverage() / 100.0f), x + 22, y + 80, textColour);
//
//        }
//
//        @Override
//        public String getTooltip() {
//            return String.format("%.1f RF/t", table.getRecentEnergyAverage() / 100.0f);
//        }
//    }

    private final TileProgrammingTable_Neptune table;

//    class RecipeSlot extends AdvancedSlot {
//        public ItemStack slot;
//        public int id;
//
//        public RecipeSlot(int x, int y, int i) {
//            super(GuiProgrammingTable.this, x, y);
//            id = i;
//        }
//
//        @Override
//        public ItemStack getItemStack() {
//            return slot;
//        }
//    }

    public GuiProgrammingTable_Neptune(ContainerProgrammingTable_Neptune container, Inventory inventory, Component component) {
        super(container, inventory, component);
        this.table = container.tile;
        // xSize = 176;
        imageWidth = SIZE_X;
        // ySize = 207;
        imageHeight = SIZE_Y;

        // Calen 1.18.2: moved to ContainerProgrammingTable_Neptune
        // for (int j = 0; j < TileProgrammingTable.HEIGHT; ++j) {
        //     for (int i = 0; i < TileProgrammingTable.WIDTH; ++i) {
        //         slots.add(new RecipeSlot(43 + 18 * i, 36 + 18 * j, (j * TileProgrammingTable.WIDTH) + i));
        //     }
        // }

        // updateRecipes();

        mainGui.shownElements.add(new LedgerTablePower(mainGui, container.tile, true));
    }

    // Calen 1.18.2: use ContainerProgrammingTable_Neptune#getDisplay
//    public void updateRecipes() {
//        if (table.options != null) {
//            Iterator<ItemStack> cur = table.options.iterator();
//
//            for (AdvancedSlot s : slots) {
//                if (cur.hasNext()) {
//                    ((RecipeSlot) s).slot = cur.next();
//                } else {
//                    ((RecipeSlot) s).slot = null;
//                }
//            }
//        } else {
//            for (AdvancedSlot s : slots) {
//                ((RecipeSlot) s).slot = null;
//            }
//        }
//    }

    @Override
    // protected void drawGuiContainerForegroundLayer(int par1, int par2)
    protected void drawForegroundLayer(PoseStack poseStack) {
        // super.drawGuiContainerForegroundLayer(par1, par2);
        String title = LocaleUtil.localize("tile.programmingTableBlock.name");
        // fontRendererObj.drawString(title, getCenteredOffset(title), 15, 0x404040);
        font.draw(poseStack, title, leftPos + (float) (imageWidth - font.width(title)) / 2, topPos + 15, 0x404040);
        // fontRendererObj.drawString(BCStringUtils.localize("gui.inventory"), 8, ySize - 97, 0x404040);
        font.draw(poseStack, LocaleUtil.localize("gui.inventory"), leftPos + 8, topPos + imageHeight - 97, 0x404040);
        // drawTooltipForSlotAt(par1, par2);
    }

    @Override
    // protected void drawGuiContainerBackgroundLayer(float f, int x, int y)
    protected void drawBackgroundLayer(float partialTicks, PoseStack poseStack) {
        // GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderUtil.color(1.0F, 1.0F, 1.0F, 1.0F);
        // mc.renderEngine.bindTexture(TEXTURE_BASE);
        // drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        ICON_GUI.drawAt(mainGui.rootElement, poseStack);

        // updateRecipes();

//        int i = 0;
//        for (AdvancedSlot slot2 : slots) {
//            RecipeSlot slot = (RecipeSlot) slot2;
//
//            if (slot.slot != null) {
//                if (table.optionId == i) {
//                    drawTexturedModalRect(guiLeft + slot.x, guiTop + slot.y, 196, 1, 16, 16);
//                }
//            }
//            i++;
//        }
        for (int i = 0; i < table.optionRecipes.size(); i++) {
            if (table.optionRecipes.get(i) != null) {
                if (table.optionId == i) {
                    ICON_SAVED_ENOUGH_ACTIVE.drawAt(getArea(i), poseStack);
                    break;
                }
            }
        }

        // int h = table.getProgressScaled(70);
        // drawTexturedModalRect(guiLeft + 164, guiTop + 36 + 70 - h, 176, 18, 4, h);
        long target = container.tile.getTarget();
        if (target != 0) {
            double v = (double) container.tile.power / target;
            ICON_PROGRESS.drawCutInside(
                    new GuiRectangle(
                            RECT_PROGRESS.x,
                            (int) (RECT_PROGRESS.y + RECT_PROGRESS.height * Math.max(1 - v, 0)),
                            RECT_PROGRESS.width,
                            (int) Math.ceil(RECT_PROGRESS.height * Math.min(v, 1))
                    ).offset(mainGui.rootElement),
                    poseStack
            );
        }

        // drawBackgroundSlots(x, y);
    }

    @Override
    // protected void slotClicked(AdvancedSlot aslot, int mouseButton)
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        // super.slotClicked(aslot, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);

//        if (aslot instanceof RecipeSlot) {
//            RecipeSlot slot = (RecipeSlot) aslot;
//
//            if (slot.slot == null) {
//                return;
//            }
//
//            if (table.optionId == slot.id) {
//                table.rpcSelectOption(-1);
//            } else {
//                table.rpcSelectOption(slot.id);
//            }
//        }

        if (mouseButton == 0) {
            for (int i = 0; i < table.optionRecipes.size(); i++) {
                if (getArea(i).contains(mouseX, mouseY)) {
                    if (table.optionId == i) {
                        table.rpcSelectOption(-1);
                    } else {
                        table.rpcSelectOption(i);
                    }
                }
            }
        }
        return true;
    }

    private IGuiArea getArea(int index) {
        return index < TileProgrammingTable_Neptune.WIDTH * TileProgrammingTable_Neptune.HEIGHT
                ? new GuiRectangle(16, 16).offset(mainGui.rootElement).offset(getPos(index))
                : GuiRectangle.ZERO;
    }

    private IGuiPosition getPos(int index) {
        int posX = index % TileProgrammingTable_Neptune.WIDTH;
        int posY = index / TileProgrammingTable_Neptune.WIDTH;
        return new PositionAbsolute(43 + posX * 18, 36 + posY * 18);
    }

//    @Override
//    protected void initLedgers(IInventory inventory) {
//        super.initLedgers(inventory);
//        if (!BuildCraftCore.hidePowerNumbers) {
//            ledgerManager.add(new LaserTableLedger());
//        }
//    }
}
