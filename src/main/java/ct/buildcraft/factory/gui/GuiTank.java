/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.factory.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import ct.buildcraft.factory.container.ContainerTank;
import ct.buildcraft.lib.gui.GuiBC8;
import ct.buildcraft.lib.gui.GuiIcon;
import ct.buildcraft.lib.gui.component.TankComponent;
import ct.buildcraft.lib.gui.help.DummyHelpElement;
import ct.buildcraft.lib.gui.pos.GuiRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.fluids.FluidType;
import ct.buildcraft.lib.gui.help.GuiHelpUtil;

public class GuiTank extends GuiBC8<ContainerTank> {
    private static final ResourceLocation TEXTURE_BASE = new ResourceLocation("buildcraftfactory:textures/gui/tank.png");
    private static final int SIZE_X = 176;
    private static final int SIZE_Y = 181;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);

    private final TankComponent tankComponent = new TankComponent(
        80, 18, 16, 64,
        16 * FluidType.BUCKET_VOLUME,
        176, 0
    );

    public GuiTank(ContainerTank menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = SIZE_X;
        imageHeight = SIZE_Y;
        inventoryLabelX = 8;
        inventoryLabelY = SIZE_Y - 96;

        tankComponent.setDataoffset(0);
        tankComponent.setup(this, menu.data);

        GuiHelpUtil.addRoot(mainGui, 80, 18, 16, 64, "buildcraft.help.tank.slot.title", 0xFF_55_BB_DD, "buildcraft.help.tank.slot.desc");

        if (menu.tile != null) {
            mainGui.shownElements.add(new DummyHelpElement(
                new GuiRectangle(80, 18, 16, 64).offset(mainGui.rootElement).expand(4),
                menu.tile.tank.helpInfo
            ));
        }
    }

    @Override
    protected void drawBackgroundLayer(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        ICON_GUI.drawAt(pose, mainGui.rootElement);
        tankComponent.render(pose, mouseX, mouseY, partialTicks, this);
        RenderSystem.setShaderTexture(0, TEXTURE_BASE);
        tankComponent.postRender(pose, mouseX, mouseY, partialTicks, this);
    }

    @Override
    protected void drawForegroundLayer(PoseStack pose, int mouseX, int mouseY) {
        float rootX = (float) mainGui.rootElement.getX();
        float rootY = (float) mainGui.rootElement.getY();
        font.draw(pose, title, rootX + (imageWidth - font.width(title)) / 2.0F, rootY + 6.0F, 0x404040);
        font.draw(pose, playerInventoryTitle, rootX + inventoryLabelX, rootY + inventoryLabelY, 0x404040);
    }

    @Override
    protected void renderTooltip(PoseStack pose, int mouseX, int mouseY) {
        tankComponent.renderTooltip(pose, mouseX, mouseY);
        super.renderTooltip(pose, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (tankComponent.onClick(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        tankComponent.mouseRelease(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        tankComponent.onClose();
        super.onClose();
    }
}
