/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.Slot;
import net.minecraft.item.ItemStack;

import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.widget.WidgetFluidTank;

import buildcraft.factory.tile.TileTank;

public class ContainerTank extends ContainerBCTile<TileTank> {
    public final WidgetFluidTank widgetTank;

    public ContainerTank(PlayerEntity player, TileTank tank) {
        super(player, tank);

        addFullPlayerInventory(99);

        widgetTank = addWidget(new WidgetFluidTank(this, tank.tank));
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public ItemStack transferStackInSlot(PlayerEntity player, int index) {
        // The only slots are player slots -- try to interact with the tank

        if (!player.getWorld().isClient) {
            Slot slot = inventorySlots.get(index);
            ItemStack stack = slot.getStack();
            ItemStack original = stack.copy();
            stack = tile.tank.transferStackToTank(this, stack);
            tile.balanceTankFluids();
            if (!ItemStack.areEqual(stack, original)) {
                slot.setStack(stack);
                sendContentUpdates();
                return ItemStack.EMPTY;
            }
        }

        return super.transferStackInSlot(player, index);
    }
}
