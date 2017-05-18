/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders.gui;

import net.minecraft.util.ResourceLocation;

import buildcraft.api.filler.FillerManager;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.statements.IStatementContainer;

import buildcraft.lib.client.sprite.ISprite;
import buildcraft.lib.client.sprite.SpriteAtlas;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.button.GuiButtonSmall;
import buildcraft.lib.gui.button.IButtonBehaviour;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.statement.GuiStatementSelector;

import buildcraft.builders.container.ContainerFiller;

public class GuiFiller extends GuiStatementSelector<ContainerFiller> {
    private static final ResourceLocation TEXTURE_BASE = new ResourceLocation("buildcraftbuilders:textures/gui/filler.png");
    private static final int SIZE_X = GUI_WIDTH, SIZE_Y = 241;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);

    public GuiFiller(ContainerFiller container) {
        super(container);
        xSize = SIZE_X;
        ySize = SIZE_Y;
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.add(new GuiButtonSmall(this, 0, rootElement.getX() + (xSize - 100) / 2, rootElement.getY() + 60, 100, "Can Excavate").setToolTip(ToolTip.createLocalized("gui.filler.canExcavate"))
            .setBehaviour(IButtonBehaviour.TOGGLE).setActive(container.tile.canExcavate()).registerListener((button, buttonId, buttonKey) -> container.tile.sendCanExcavate(button.isButtonActive())));
    }

    @Override
    protected void drawBackgroundLayer(float partialTicks) {
        ICON_GUI.drawAt(rootElement);
        IFillerPattern[] patterns = FillerManager.registry.getPatterns().toArray(new IFillerPattern[0]);
        int i = (int) ((System.currentTimeMillis() / 1000) % patterns.length);
        IFillerPattern pattern = patterns[i];

        ISprite sprite = new SpriteAtlas(pattern.getGuiSprite());
        sprite = sprite.subRelative(4, 4, 8, 8, 16);
        GuiIcon icon = new GuiIcon(sprite, 8);
        GuiRectangle rect = new GuiRectangle(38, 30, 16, 16);
        icon.drawScaledInside(rect.offset(rootElement));
        // icon.drawAt(0, 0);
    }

    @Override
    protected void iteratePossible(OnStatement consumer) {
        // int tx = 0;
        // int ty = 0;
        // EnumPipePart last = null;
        // consumer.iterate(null, rootElement.offset(-18, 8).resize(18, 18));
        // for (TriggerWrapper wrapper : container.possibleTriggers) {
        // tx++;
        // if (tx > 3 || (last != null && last != wrapper.sourcePart)) {
        // tx = 0;
        // ty++;
        // }
        // consumer.iterate(wrapper, rootElement.offset(18 * (-1 - tx), ty * 18 + 8).resize(18, 18));
        // last = wrapper.sourcePart;
        // }
    }

    @Override
    public IStatementContainer getStatementContainer() {
        return null;// TODO!
    }
}
