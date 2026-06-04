/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.silicon.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;

/**
 * STUB(R.Chen): ContainerGate implementation deferred until lib.gui (ContainerBCTile, ScreenHandler migration)
 * is ported to Fabric 1.20.1.
 */
public class ContainerGate extends ScreenHandler {

    public ContainerGate(int syncId, PlayerInventory playerInventory) {
        super(null, syncId);
        // STUB(R.Chen): tile binding, slot registration deferred.
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }
    @Override
    public net.minecraft.item.ItemStack quickMove(net.minecraft.entity.player.PlayerEntity player, int index) {
        return net.minecraft.item.ItemStack.EMPTY;
    }
}