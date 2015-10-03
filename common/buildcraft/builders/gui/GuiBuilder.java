/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.gui;

import java.io.IOException;
import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import buildcraft.builders.tile.TileBuilder;
import buildcraft.core.BuildCraftCore;
import buildcraft.core.lib.fluids.Tank;
import buildcraft.core.lib.gui.GuiAdvancedInterface;
import buildcraft.core.lib.gui.ItemSlot;
import buildcraft.core.lib.network.command.CommandWriter;
import buildcraft.core.lib.network.command.PacketCommand;
import buildcraft.core.lib.utils.StringUtils;

import io.netty.buffer.ByteBuf;

public class GuiBuilder extends GuiAdvancedInterface {
    private static final ResourceLocation REGULAR_TEXTURE = new ResourceLocation("buildcraftbuilders:textures/gui/builder.png");
    private static final ResourceLocation BLUEPRINT_TEXTURE = new ResourceLocation("buildcraftbuilders:textures/gui/builder_blueprint.png");
    private IInventory playerInventory;
    private TileBuilder builder;
    private GuiButton selectedButton;
    private int sbPosition, sbLength;
    private boolean sbInside;

    public GuiBuilder(IInventory playerInventory, TileBuilder builder) {
        super(new ContainerBuilder(playerInventory, builder), builder, BLUEPRINT_TEXTURE);
        this.playerInventory = playerInventory;
        this.builder = builder;
        xSize = 256;
        ySize = 225;

        resetNullSlots(6 * 4);

        for (int i = 0; i < 6; ++i) {
            for (int j = 0; j < 4; ++j) {
                slots.set(i * 4 + j, new ItemSlot(this, 179 + j * 18, 18 + i * 18));
            }
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2) {
        super.drawGuiContainerForegroundLayer(par1, par2);

        drawCenteredString(StringUtils.localize("tile.builderBlock.name"), 178 / 2, 16, 0x404040);
        if (builder.getStackInSlot(0) != null) {
            fontRendererObj.drawString(StringUtils.localize("gui.building.resources"), 8, 60, 0x404040);
            fontRendererObj.drawString(StringUtils.localize("gui.inventory"), 8, ySize - 97, 0x404040);
            fontRendererObj.drawString(StringUtils.localize("gui.needed"), 178, 7, 0x404040);
            fontRendererObj.drawString(StringUtils.localize("gui.building.fluids"), 178, 133, 0x404040);
        }

        drawTooltipForSlotAt(par1, par2);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        boolean isBlueprint = builder.getStackInSlot(0) != null;

        mc.renderEngine.bindTexture(REGULAR_TEXTURE);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, 176, ySize);
        mc.renderEngine.bindTexture(BLUEPRINT_TEXTURE);
        if (isBlueprint) {
            drawTexturedModalRect(guiLeft + 169, guiTop, 169, 0, 256 - 169, ySize);
        }

        List<ItemStack> needs = builder.getNeededItems();

        if (needs != null) {
            if (needs.size() > slots.size()) {
                sbLength = (needs.size() - slots.size() + 3) / 4;
                if (sbPosition >= sbLength) {
                    sbPosition = sbLength;
                }

                // render scrollbar
                drawTexturedModalRect(guiLeft + 172, guiTop + 17, 18, 0, 6, 108);
                int sbPixelPosition = sbPosition * 95 / sbLength;
                drawTexturedModalRect(guiLeft + 172, guiTop + 17 + sbPixelPosition, 24, 0, 6, 14);
            } else {
                sbPosition = 0;
                sbLength = 0;
            }

            int offset = sbPosition * 4;
            for (int s = 0; s < slots.size(); s++) {
                int ts = offset + s;
                if (ts >= needs.size()) {
                    ((ItemSlot) slots.get(s)).stack = null;
                } else {
                    ((ItemSlot) slots.get(s)).stack = needs.get(ts).copy();
                }
            }

            for (GuiButton b : (List<GuiButton>) buttonList) {
                b.visible = true;
            }
        } else {
            sbPosition = 0;
            sbLength = 0;
            for (int s = 0; s < slots.size(); s++) {
                ((ItemSlot) slots.get(s)).stack = null;
            }
            for (GuiButton b : (List<GuiButton>) buttonList) {
                b.visible = false;
            }
        }

        if (isBlueprint) {
            drawBackgroundSlots();

            for (int i = 0; i < builder.fluidTanks.length; i++) {
                Tank tank = builder.fluidTanks[i];
                if (tank.getFluid() != null && tank.getFluid().amount > 0) {
                    drawFluid(tank.getFluid(), guiLeft + 179 + 18 * i, guiTop + 145, 16, 47, tank.getCapacity());
                    drawTexturedModalRect(guiLeft + 179 + 18 * i, guiTop + 145, 0, 54, 16, 47);
                }
            }
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        for (int i = 0; i < 4; i++) {
            buttonList.add(new BuilderEraseButton(i, guiLeft + 178 + 18 * i, guiTop + 197, 18, 18));
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        int guiX = mouseX - guiLeft;
        int guiY = mouseY - guiTop;
        if (sbLength > 0 && button == 0) {
            if (guiX >= 172 && guiX < 178 && guiY >= 17 && guiY < 125) {
                sbInside = true;
                updateToSbHeight(guiY - 17);
            }
        }
        super.mouseClicked(mouseX, mouseY, button);
    }

    private void updateToSbHeight(int h) {
        int hFrac = (h * sbLength + 54) / 108;
        sbPosition = hFrac;
    }

    @Override
    protected void mouseClickMove(int x, int y, int button, long time) {
        super.mouseClickMove(x, y, button, time);
        if (sbInside && button == 0) {
            int guiY = y - guiTop;
            if (sbLength > 0) {
                if (guiY >= 17 && guiY < 125) {
                    updateToSbHeight(guiY - 17);
                }
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int eventType) {
        super.mouseReleased(mouseX, mouseY, eventType);
        if (sbInside && eventType == 0) {
            int guiY = mouseY - guiTop;
            if (sbLength > 0) {
                if (guiY >= 17 && guiY < 125) {
                    updateToSbHeight(guiY - 17);
                    sbInside = false;
                }
            }
        }

        if (this.selectedButton != null && eventType == 0) {
            this.selectedButton.mouseReleased(mouseX, mouseY);
            this.selectedButton = null;
        }
    }

    private class BuilderEraseButton extends GuiButton {
        private boolean clicked;

        public BuilderEraseButton(int id, int x, int y, int width, int height) {
            super(id, x, y, width, height, null);
        }

        @Override
        public boolean mousePressed(Minecraft mc, int x, int y) {
            if (super.mousePressed(mc, x, y)) {
                selectedButton = this;
                clicked = true;
                BuildCraftCore.instance.sendToServer(new PacketCommand(builder, "eraseFluidTank", new CommandWriter() {
                    public void write(ByteBuf data) {
                        data.writeInt(id);
                    }
                }));
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void mouseReleased(int x, int y) {
            super.mouseReleased(x, y);
            clicked = false;
        }

        @Override
        public void drawButton(Minecraft mc, int x, int y) {
            if (!visible) {
                return;
            }
            // hovered
            this.hovered = x >= this.xPosition && y >= this.yPosition && x < this.xPosition + this.width && y < this.yPosition + this.height;

            mc.renderEngine.bindTexture(BLUEPRINT_TEXTURE);
            drawTexturedModalRect(xPosition, yPosition, 0, (clicked ? 1 : this.hovered ? 2 : 0) * 18, 18, 18);
            mouseDragged(mc, x, y);
        }
    }
}
