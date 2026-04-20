/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.builders.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import ct.buildcraft.builders.menu.ContainerBuilder;
import ct.buildcraft.lib.gui.GuiBC8;
import ct.buildcraft.lib.gui.GuiIcon;
import ct.buildcraft.lib.gui.pos.GuiRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

// TODO: Convert this gui into JSON!
public class GuiBuilder extends GuiBC8<ContainerBuilder> {
    private static final ResourceLocation TEXTURE_BASE =
            new ResourceLocation("buildcraftbuilders:textures/gui/builder.png");
    private static final ResourceLocation TEXTURE_BLUEPRINT =
            new ResourceLocation("buildcraftbuilders:textures/gui/builder_blueprint.png");
    private static final int SIZE_X = 176, SIZE_BLUEPRINT_X = 256, SIZE_Y = 222, BLUEPRINT_WIDTH = 87;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_BLUEPRINT_GUI = new GuiIcon(
            TEXTURE_BLUEPRINT,
            SIZE_BLUEPRINT_X - BLUEPRINT_WIDTH,
            0,
            BLUEPRINT_WIDTH,
            SIZE_Y
    );
    private static final GuiIcon ICON_TANK_OVERLAY = new GuiIcon(TEXTURE_BLUEPRINT, 0, 54, 16, 47);
    
//    private CycleButton<Boolean> needMaterialButton;

    
 //   private boolean needMaterial = true;

    
    public GuiBuilder(ContainerBuilder container, Inventory inv, Component title) {
        super(container, inv, title);
        imageWidth = SIZE_BLUEPRINT_X;
        imageHeight = SIZE_Y;
    }

    @Override
    public void init() {
        super.init();

        for (int i = 0; i < container.widgetTanks.size(); i++) {
            mainGui.shownElements.add(
                    container.widgetTanks
                    .get(i).createGuiElement(mainGui, new GuiRectangle(179 + i * 18, 145, 16, 47).offset(mainGui.rootElement), ICON_TANK_OVERLAY)
            );
        }
        
//		needMaterial = (p&0b1) == 1;

		
 /*       this.needMaterialButton = this.addRenderableWidget(CycleButton.booleanBuilder(
        		Component.translatable("block.architect.needMaterial"), 
        		Component.translatable("block.architect.noMaterial")).
        			displayOnlyValue().withInitialValue(needMaterial).
        			create(this.width / 2 - 122, this.height/2 - 55, 77, 20, Component.translatable("advMode.type"), (p_169727_, p_169728_) -> {
            this.needMaterial = p_169728_;
         }));*/


//        buttonList.add(
//                new GuiButtonSmall(
//                        this,
//                        0,
//                        rootElement.getX() + (ICON_GUI.width - 100) / 2,
//                        rootElement.getY() + 50,
//                        100,
//                        "Can Excavate"
//                )
//                        .setToolTip(ToolTip.createLocalized("gui.builder.canExcavate"))
//                        .setBehaviour(IButtonBehaviour.TOGGLE)
//                        .setActive(container.tile.canExcavate())
//                        .registerListener((button, buttonId, buttonKey) ->
//                                container.tile.sendCanExcavate(button.isButtonActive())
//                        )
//        );
    }

    @Override
    protected void drawBackgroundLayer(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
    	RenderSystem._setShaderTexture(0, TEXTURE_BASE);
        ICON_GUI.drawAt(pose, mainGui.rootElement);
        RenderSystem._setShaderTexture(0, TEXTURE_BLUEPRINT);
        ICON_BLUEPRINT_GUI.drawAt(pose, mainGui.rootElement.offset(SIZE_BLUEPRINT_X - BLUEPRINT_WIDTH, 0));
    }
}
