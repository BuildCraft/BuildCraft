/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

import buildcraft.lib.gui.ledger.LedgerHelp;
import buildcraft.lib.gui.ledger.LedgerOwnership;

@Environment(EnvType.CLIENT)
public class GuiBC8<T extends ContainerBC_Neptune> extends BuildCraftGui {
    public final T container;

    @SuppressWarnings("unchecked")
    public GuiBC8(T container, PlayerInventory inv, Text title) {
        super(container, inv, title);
        this.container = container;
        standardLedgerInit();
    }

    /** Forge-compat single-arg constructor. inv/title default to empty values. */
    @SuppressWarnings("unchecked")
    public GuiBC8(T container) {
        super(container,
              net.minecraft.client.MinecraftClient.getInstance().player != null
                  ? net.minecraft.client.MinecraftClient.getInstance().player.getInventory()
                  : new PlayerInventory(null),
              Text.empty());
        this.container = container;
        standardLedgerInit();
    }

    private void standardLedgerInit() {
        if (container instanceof ContainerBCTile<?>) {
            shownElements.add(new LedgerOwnership(this, ((ContainerBCTile<?>) container).tile, true));
        }
        if (shouldAddHelpLedger()) {
            shownElements.add(new LedgerHelp(this, false));
        }
    }

    protected boolean shouldAddHelpLedger() { return true; }
}
