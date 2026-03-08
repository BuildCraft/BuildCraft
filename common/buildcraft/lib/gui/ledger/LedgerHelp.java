/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.ledger;

import buildcraft.api.core.render.ISprite;
import buildcraft.lib.BCLibSprites;
import buildcraft.lib.client.sprite.SpriteNineSliced;
import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.config.GuiConfigManager;
import buildcraft.lib.gui.elem.GuiElementContainerHelp;
import buildcraft.lib.gui.help.ElementHelpInfo.HelpPosition;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.misc.GuiUtil;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.RenderUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

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

    private IGuiElement selected = null;
    private boolean foundAny = false, init = false;

    public LedgerHelp(BuildCraftGui gui, boolean expandPositive) {
        super(gui, 0xFF_CC_99_FF, expandPositive);
        title = LocaleUtil.localize("gui.ledger.help");
        calculateMaxSize();

        ResourceLocation id = new ResourceLocation("buildcraftlib:base");
        setOpenProperty(GuiConfigManager.getOrAddBoolean(id, "ledger.help.is_open", false));
    }

    @Override
    public void tick() {
        super.tick();
        if (currentWidth == CLOSED_WIDTH && currentHeight == CLOSED_HEIGHT) {
            selected = null;
            if (openElements.size() == 2) {
                openElements.remove(1);
                title = LocaleUtil.localize("gui.ledger.help");
                calculateMaxSize();
            }
        }
    }

    @Override
    protected void drawIcon(MatrixStack poseStack, double x, double y) {
        if (!init) {
            init = true;
            List<HelpPosition> elements = new ArrayList<>();
            for (IGuiElement element : gui.shownElements) {
                element.addHelpElements(elements);
            }
            foundAny = elements.size() > 0;
        }
        ISprite sprite = foundAny ? BCLibSprites.HELP : BCLibSprites.WARNING_MINOR;
        GuiIcon.draw(sprite, poseStack, x, y, x + 16, y + 16);
    }

    @Override
    public void drawForeground(MatrixStack poseStack, float partialTicks) {
        super.drawForeground(poseStack, partialTicks);
        if (!shouldDrawOpen()) {
            return;
        }
        boolean set = false;
        List<HelpPosition> elements = new ArrayList<>();
        for (IGuiElement element : gui.shownElements) {
            element.addHelpElements(elements);
            foundAny |= elements.size() > 0;
            for (HelpPosition info : elements) {
                IGuiArea rect = info.target;
                boolean isHovered = rect.contains(gui.mouse);
                if (isHovered) {
                    if (selected != element && !set) {
                        selected = element;
                        GuiElementContainerHelp container = new GuiElementContainerHelp(gui, positionLedgerInnerStart);
                        info.info.addGuiElements(container);
                        if (openElements.size() == 2) {
                            openElements.remove(1);
                        }
                        openElements.add(container);
                        title = LocaleUtil.localize("gui.ledger.help") + ": " + LocaleUtil.localize(info.info.title);
                        calculateMaxSize();
                        set = true;
                    }
                }
                boolean isSelected = selected == element;
                SpriteNineSliced split = SPRITE_HELP_SPLIT[isHovered ? 1 : 0][isSelected ? 1 : 0];
                RenderUtil.setGLColorFromInt(info.info.colour);
                split.draw(rect, poseStack);
            }
            elements.clear();
        }
//        GlStateManager.color(1, 1, 1);
        RenderUtil.color(1, 1, 1);
    }
}
