/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.gui.ledger;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import ct.buildcraft.api.core.render.ISprite;
import ct.buildcraft.lib.BCLibSprites;
import ct.buildcraft.lib.client.sprite.SpriteNineSliced;
import ct.buildcraft.lib.gui.BuildCraftGui;
import ct.buildcraft.lib.gui.GuiIcon;
import ct.buildcraft.lib.gui.IGuiElement;
import ct.buildcraft.lib.gui.config.GuiConfigManager;
import ct.buildcraft.lib.gui.elem.GuiElementContainerHelp;
import ct.buildcraft.lib.gui.help.ElementHelpInfo.HelpPosition;
import ct.buildcraft.lib.gui.pos.IGuiArea;
import ct.buildcraft.lib.misc.GuiUtil;
import ct.buildcraft.lib.misc.RenderUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class LedgerHelp extends Ledger_Neptune {

    private static final SpriteNineSliced[][] SPRITE_HELP_SPLIT = new SpriteNineSliced[2][2];

    static {
        SPRITE_HELP_SPLIT[0][0] =
            GuiUtil.slice(GuiUtil.subRelative(BCLibSprites.HELP_SPLIT, 0, 0, 8, 8, 16), 2, 2, 6, 6, 8);
        SPRITE_HELP_SPLIT[0][1] =
            GuiUtil.slice(GuiUtil.subRelative(BCLibSprites.HELP_SPLIT, 0, 8, 8, 8, 16), 2, 2, 6, 6, 8);
        SPRITE_HELP_SPLIT[1][0] =
            GuiUtil.slice(GuiUtil.subRelative(BCLibSprites.HELP_SPLIT, 8, 0, 8, 8, 16), 2, 2, 6, 6, 8);
        SPRITE_HELP_SPLIT[1][1] =
            GuiUtil.slice(GuiUtil.subRelative(BCLibSprites.HELP_SPLIT, 8, 8, 8, 8, 16), 2, 2, 6, 6, 8);
    }

    private HelpPosition selected = null;
    private boolean foundAny = false;

    public LedgerHelp(BuildCraftGui gui, boolean expandPositive) {
        super(gui, 0xFF_CC_99_FF, expandPositive);
        title = Component.translatable("gui.ledger.help");
        calculateMaxSize();

        ResourceLocation id = new ResourceLocation("buildcraftlib:base");
        setOpenProperty(GuiConfigManager.getOrAddBoolean(id, "ledger.help.is_open", false));
    }

    @Override
    public void tick() {
        super.tick();
        List<HelpPosition> positions = collectHelpPositions();
        foundAny = !positions.isEmpty();

        if (currentWidth == CLOSED_WIDTH && currentHeight == CLOSED_HEIGHT) {
            clearSelected();
            return;
        }

        if (selected != null && positions.stream().noneMatch(this::sameAsSelected)) {
            clearSelected();
        }
    }

    @Override
    protected void drawIcon(PoseStack pose, double x, double y) {
        foundAny = !collectHelpPositions().isEmpty();
        ISprite sprite = foundAny ? BCLibSprites.HELP : BCLibSprites.WARNING_MINOR;
        GuiIcon.draw(pose, sprite, x, y, x + 16, y + 16);
    }

    @Override
    public void drawForeground(PoseStack pose, float partialTicks) {
        super.drawForeground(pose, partialTicks);
        RenderSystem.enableBlend();
        if (!shouldDrawOpen()) {
            return;
        }

        List<HelpPosition> positions = collectHelpPositions();
        foundAny = !positions.isEmpty();

        HelpPosition hovered = null;
        for (HelpPosition info : positions) {
            if (info.target.contains(gui.mouse)) {
                hovered = info;
                break;
            }
        }
        if (hovered != null && !same(hovered, selected)) {
            setSelected(hovered);
        }

        for (HelpPosition info : positions) {
            IGuiArea rect = info.target;
            boolean isHovered = rect.contains(gui.mouse);
            boolean isSelected = same(info, selected);
            SpriteNineSliced split = SPRITE_HELP_SPLIT[isHovered ? 1 : 0][isSelected ? 1 : 0];
            RenderUtil.setGLColorFromInt(info.info.colour);
            split.draw(pose, rect);
        }
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    private List<HelpPosition> collectHelpPositions() {
        List<HelpPosition> positions = new ArrayList<>();
        for (IGuiElement element : gui.shownElements) {
            if (element != this) {
                element.addHelpElements(positions);
            }
        }
        return positions;
    }

    private void setSelected(HelpPosition info) {
        selected = info;
        GuiElementContainerHelp container = new GuiElementContainerHelp(gui, positionLedgerInnerStart);
        info.info.addGuiElements(container);
        removeSelectedContainer();
        openElements.add(container);
        title = Component.translatable("gui.ledger.help.selected", info.info.getLocalizedTitle());
        calculateMaxSize();
    }

    private void clearSelected() {
        if (selected == null && openElements.size() <= 1) {
            return;
        }
        selected = null;
        removeSelectedContainer();
        title = Component.translatable("gui.ledger.help");
        calculateMaxSize();
    }

    private void removeSelectedContainer() {
        while (openElements.size() > 1) {
            openElements.remove(openElements.size() - 1);
        }
    }

    private boolean sameAsSelected(HelpPosition other) {
        return same(other, selected);
    }

    private static boolean same(HelpPosition a, HelpPosition b) {
        return a != null && b != null && a.info == b.info &&
            a.target.getX() == b.target.getX() &&
            a.target.getY() == b.target.getY() &&
            a.target.getWidth() == b.target.getWidth() &&
            a.target.getHeight() == b.target.getHeight();
    }
}
