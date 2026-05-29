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

// STUB(R.Chen): GUI render — Phase 5. Extends BuildCraftGui (itself a stub).
@Environment(EnvType.CLIENT)
public class GuiBC8<T extends ContainerBC_Neptune> extends BuildCraftGui {
    public final T container;

    @SuppressWarnings("unchecked")
    public GuiBC8(T container, PlayerInventory inv, Text title) {
        super(container, inv, title);
        this.container = container;
    }
}
