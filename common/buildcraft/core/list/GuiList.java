/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): GuiList rendering deferred — LWJGL Keyboard + GuiTextField compat pending
package buildcraft.core.list;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.entity.player.PlayerEntity;

import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.button.IButtonClickEventListener;
import buildcraft.lib.gui.button.IButtonClickEventTrigger;

@Environment(EnvType.CLIENT)
public class GuiList extends GuiBC8<ContainerList> implements IButtonClickEventListener {

    public GuiList(PlayerEntity iPlayer) {
        super(new ContainerList(iPlayer));
    }

    @Override
    public void handleButtonClick(IButtonClickEventTrigger sender, int buttonKey) {
        // STUB
    }
}
