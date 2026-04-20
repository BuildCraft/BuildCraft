/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.gui.ledger;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;

import ct.buildcraft.api.core.render.ISprite;
import ct.buildcraft.lib.gui.BuildCraftGui;
import ct.buildcraft.lib.gui.GuiIcon;
import ct.buildcraft.lib.gui.config.GuiConfigManager;
import ct.buildcraft.lib.misc.SpriteUtil;
import ct.buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class LedgerOwnership extends Ledger_Neptune {

    private final TileBC_Neptune tile;

    public LedgerOwnership(BuildCraftGui gui, TileBC_Neptune tile, boolean expandPositive) {
        super(gui, 0xFF_E0_F0_FF, expandPositive);
        this.title = Component.translatable("gui.ledger.ownership");
        this.tile = tile;

        appendText(() ->Component.translatable(getOwnerName()), 0);

        calculateMaxSize();
        setOpenProperty(GuiConfigManager.getOrAddBoolean(new ResourceLocation("buildcraftlib:base"),
            "ledger.owner.is_open", false));
    }

    @Override
    protected void drawIcon(PoseStack pose, double x, double y) {
        ISprite sprite = SpriteUtil.getFaceSprite(tile.getOwner());
        GuiIcon.draw(pose, sprite, x, y, x + 16, y + 16);
        sprite = SpriteUtil.getFaceOverlaySprite(tile.getOwner());
        if (sprite != null) {
            GuiIcon.draw(pose, sprite, x - 0.5, y - 0.5, x + 17, y + 17);
        }
    }

    private String getOwnerName() {
//    	if(tile == null) return "no-one";
        GameProfile owner = tile.getOwner();
        if (owner == null) {
            return "no-one";
        }
        return owner.getName();
    }
}
