/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.gui.ledger;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import buildcraft.lib.engine.IEngineLikeForLedger;
import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.misc.LocaleUtil;

@Environment(EnvType.CLIENT)
public class LedgerEngine extends Ledger_Neptune {
    private static final int OVERLAY_COLOUR = 0xFF_D4_6C_1F;
    private static final int HEADER_COLOUR  = 0xFF_E1_C9_2F;
    private static final int SUB_HEADER_COLOUR = 0xFF_AA_AF_B8;
    private static final int TEXT_COLOUR = 0xFF_00_00_00;

    public final IEngineLikeForLedger engine;

    public LedgerEngine(BuildCraftGui gui, IEngineLikeForLedger engine, boolean expandPositive) {
        super(gui, OVERLAY_COLOUR, expandPositive);
        this.engine = engine;
        this.title = "gui.power";

        appendText(LocaleUtil.localize("gui.currentOutput") + ":", SUB_HEADER_COLOUR).setDropShadow(true);
        appendText(() -> LocaleUtil.localizeMjFlow(engine.getCurrentMjOutput()), TEXT_COLOUR);
        appendText(LocaleUtil.localize("gui.stored") + ":", SUB_HEADER_COLOUR).setDropShadow(true);
        appendText(() -> LocaleUtil.localizeMj(engine.getMjStored()), TEXT_COLOUR);
        appendText(LocaleUtil.localize("gui.heat") + ":", SUB_HEADER_COLOUR).setDropShadow(true);
        appendText(() -> LocaleUtil.localizeHeat(engine.getHeat()), TEXT_COLOUR);
        calculateMaxSize();
        // TODO(R.Chen): ledger open-state persistence — GuiConfigManager blocked by BCLibConfig migration.
    }

    @Override
    public int getTitleColour() { return HEADER_COLOUR; }

    @Override
    protected void drawIcon(double x, double y) {
        // TODO(R.Chen): drawIcon — sprite draw helper pending; sprite selected by engine.getPowerStage().
    }
}
