/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.ledger;

import buildcraft.lib.gui.BuildCraftGui;
import com.mojang.authlib.GameProfile;

import net.minecraft.util.ResourceLocation;

import buildcraft.api.core.render.ISprite;

import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.config.GuiConfigManager;
import buildcraft.lib.misc.SpriteUtil;
import buildcraft.lib.tile.TileBC_Neptune;

public class LedgerOwnership extends Ledger_Neptune {

    private final TileBC_Neptune tile;

    public LedgerOwnership(BuildCraftGui gui, TileBC_Neptune tile, boolean expandPositive) {
        super(gui, 0xFF_E0_F0_FF, expandPositive);
        this.title = "gui.ledger.ownership";
        this.tile = tile;

        appendText(this::getOwnerName, 0);

        calculateMaxSize();
        setOpenProperty(GuiConfigManager.getOrAddBoolean(new ResourceLocation("buildcraftlib:base"), "ledger.owner.is_open", false));
    }

    @Override
    protected void drawIcon(double x, double y) {
        ISprite sprite = SpriteUtil.getFaceSprite(tile.getOwner());
        GuiIcon.draw(sprite, x, y, x + 16, y + 16);
    }

    private String getOwnerName() {
        GameProfile owner = tile.getOwner();
        if (owner == null) {
            return "no-one";
        }
        return owner.getName();
    }
}
