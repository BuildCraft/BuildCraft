/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.gui;

import java.util.Optional;

import buildcraft.lib.gui.ledger.LedgerManager_Neptune;
import buildcraft.lib.gui.ledger.Ledger_Neptune;
import buildcraft.lib.misc.LocaleUtil;

import buildcraft.builders.snapshot.ITileForSnapshotBuilder;

public class LedgerCounters extends Ledger_Neptune {
    private static final int OVERLAY_COLOUR = 0xFF_6C_D4_1F;
    private static final int SUB_HEADER_COLOUR = 0xFF_AA_AF_b8;
    private static final int TEXT_COLOUR = 0xFF_00_00_00;

    public final ITileForSnapshotBuilder tile;

    public LedgerCounters(LedgerManager_Neptune manager, ITileForSnapshotBuilder tile) {
        super(manager);
        this.tile = tile;
        title = "gui.counters";

        appendText(LocaleUtil.localize("gui.leftToBreak") + ":", SUB_HEADER_COLOUR).setDropShadow(true);
        appendText(
            () ->
                String.valueOf(
                    Optional.ofNullable(tile.getBuilder())
                        .map(builder -> builder.leftToBreak)
                        .orElse(0)
                ),
            TEXT_COLOUR
        );
        appendText(LocaleUtil.localize("gui.leftToPlace") + ":", SUB_HEADER_COLOUR).setDropShadow(true);
        appendText(
            () ->
                String.valueOf(
                    Optional.ofNullable(tile.getBuilder())
                        .map(builder -> builder.leftToPlace)
                        .orElse(0)
                ),
            TEXT_COLOUR
        );
        calculateMaxSize();
    }

    @Override
    public int getColour() {
        return OVERLAY_COLOUR;
    }
}
