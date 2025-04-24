/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.client.gui;

import buildcraft.api.core.render.ISprite;
import buildcraft.energy.tile.TileDynamoMJ;
import buildcraft.lib.BCLibSprites;
import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.config.GuiConfigManager;
import buildcraft.lib.gui.ledger.Ledger_Neptune;
import buildcraft.lib.misc.LocaleUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.ResourceLocation;

public class LedgerDynamoMJ extends Ledger_Neptune {
    private static final int OVERLAY_COLOUR = 0xFF_D4_6C_1F;// 0xFF_FF_55_11;// TEMP!
    private static final int HEADER_COLOUR = 0xFF_E1_C9_2F;
    private static final int SUB_HEADER_COLOUR = 0xFF_AA_AF_b8;
    private static final int TEXT_COLOUR = 0xFF_00_00_00;

    public final TileDynamoMJ engine;

    public LedgerDynamoMJ(BuildCraftGui gui, TileDynamoMJ engine, boolean expandPositive) {
        super(gui, OVERLAY_COLOUR, expandPositive);
        this.engine = engine;
        this.title = "gui.power";

        appendText(LocaleUtil.localize("gui.currentOutput") + ":", SUB_HEADER_COLOUR).setDropShadow(true);
        appendText(() -> LocaleUtil.localizeRfFlow(engine.getCurrentOutput()), TEXT_COLOUR);
        appendText(LocaleUtil.localize("gui.stored") + ":", SUB_HEADER_COLOUR).setDropShadow(true);
        appendText(() -> LocaleUtil.localizeMj(engine.getMjStored()), TEXT_COLOUR);
        appendText(LocaleUtil.localize("gui.heat") + ":", SUB_HEADER_COLOUR).setDropShadow(true);
        appendText(() -> LocaleUtil.localizeHeat(engine.getHeat()), TEXT_COLOUR);
        calculateMaxSize();

        setOpenProperty(GuiConfigManager.getOrAddBoolean(new ResourceLocation("buildcraftlib:engine"), "ledger.power.is_open", false));
    }

    @Override
    public int getTitleColour() {
        return HEADER_COLOUR;
    }

    @Override
    protected void drawIcon(MatrixStack poseStack, double x, double y) {
        ISprite sprite;
        switch (engine.getPowerStage()) {
            case OVERHEAT:
                sprite = BCLibSprites.ENGINE_OVERHEAT;
                break;
            case RED:
            case YELLOW:
                sprite = BCLibSprites.ENGINE_WARM;
                break;
            default:
                sprite = engine.isEngineOn() ? BCLibSprites.ENGINE_ACTIVE : BCLibSprites.ENGINE_INACTIVE;
        }
        GuiIcon.draw(sprite, poseStack, x, y, x + 16, y + 16);
    }
}
