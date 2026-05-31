/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.gui.ledger;

import com.mojang.authlib.GameProfile;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.tile.TileBC_Neptune;

@Environment(EnvType.CLIENT)
public class LedgerOwnership extends Ledger_Neptune {

    private final TileBC_Neptune tile;

    public LedgerOwnership(BuildCraftGui gui, TileBC_Neptune tile, boolean expandPositive) {
        super(gui, 0xFF_E0_F0_FF, expandPositive);
        this.title = "gui.ledger.ownership";
        this.tile = tile;

        appendText(this::getOwnerName, 0xFF_00_00_00);
        calculateMaxSize();
        // TODO(R.Chen): ownership ledger open persistence — GuiConfigManager blocked by BCLibConfig migration.
    }

    @Override
    protected void drawIcon(double x, double y) {
        // TODO(R.Chen): owner face sprite draw — SpriteUtil.getFaceSprite not yet migrated.
    }

    private String getOwnerName() {
        GameProfile owner = tile.getOwner();
        return owner == null ? "no-one" : owner.getName();
    }
}
