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

import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.misc.LocaleUtil;

@Environment(EnvType.CLIENT)
public class LedgerHelp extends Ledger_Neptune {

    public LedgerHelp(BuildCraftGui gui, boolean expandPositive) {
        super(gui, 0xFF_CC_99_FF, expandPositive);
        title = LocaleUtil.localize("gui.ledger.help");
        calculateMaxSize();
        // TODO(R.Chen): help ledger open persistence + help element rendering — GuiConfigManager blocked.
    }

    @Override
    protected void drawIcon(double x, double y) {
        // TODO(R.Chen): help icon draw — BCLibSprites.HELP sprite draw helper pending.
    }
}
