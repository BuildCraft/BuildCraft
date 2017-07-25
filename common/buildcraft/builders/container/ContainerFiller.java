/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.container;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;

import buildcraft.builders.filling.Filling;
import buildcraft.builders.tile.TileFiller;

public class ContainerFiller extends ContainerBCTile<TileFiller> {
    public ContainerFiller(EntityPlayer player, TileFiller tile) {
        super(player, tile);

        addFullPlayerInventory(153);

        for (int sy = 0; sy < 3; sy++) {
            for (int sx = 0; sx < 9; sx++) {
                addSlotToContainer(new SlotBase(tile.invResources, sx + sy * 9, 8 + sx * 18, 85 + sy * 18) {
                    @Override
                    public boolean isItemValid(@Nonnull ItemStack stack) {
                        return Filling.getItemBlocks().contains(stack.getItem());
                    }
                });
            }
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
}
