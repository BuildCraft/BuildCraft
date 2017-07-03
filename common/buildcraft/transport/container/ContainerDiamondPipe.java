/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;

import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.gui.slot.SlotPhantom;
import buildcraft.lib.tile.item.ItemHandlerSimple;

import buildcraft.transport.pipe.behaviour.PipeBehaviourDiamond;

public class ContainerDiamondPipe extends ContainerBC_Neptune {
    private final PipeBehaviourDiamond behaviour;
    private final ItemHandlerSimple filterInv;

    public ContainerDiamondPipe(EntityPlayer player, PipeBehaviourDiamond pipe) {
        super(player);
        this.behaviour = pipe;
        this.filterInv = pipe.filters;
        behaviour.pipe.getHolder().onPlayerOpen(player);

        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 9; x++) {
                addSlotToContainer(new SlotPhantom(filterInv, x + y * 9, 8 + x * 18, 18 + y * 18));
            }
        }

        for (int l = 0; l < 3; l++) {
            for (int k1 = 0; k1 < 9; k1++) {
                addSlotToContainer(new Slot(player.inventory, k1 + l * 9 + 9, 8 + k1 * 18, 140 + l * 18));
            }
        }

        for (int i1 = 0; i1 < 9; i1++) {
            addSlotToContainer(new Slot(player.inventory, i1, 8 + i1 * 18, 198));
        }
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        behaviour.pipe.getHolder().onPlayerClose(player);
    }

    @Override
    public boolean canInteractWith(EntityPlayer entityplayer) {
        return true;// FIXME!
    }
}
