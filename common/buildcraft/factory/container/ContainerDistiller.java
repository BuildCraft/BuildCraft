/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.widget.WidgetFluidTank;

import buildcraft.factory.tile.TileDistiller_BC8;

public class ContainerDistiller extends ContainerBCTile<TileDistiller_BC8> {
    public final WidgetFluidTank widgetInputTank;
    public final WidgetFluidTank widgetOutputGasTank;
    public final WidgetFluidTank widgetOutputLiquidTank;

    public ContainerDistiller(PlayerEntity player, TileDistiller_BC8 tank) {
        super(player, tank);

        addFullPlayerInventory(79);

        widgetInputTank = addWidget(new WidgetFluidTank(this, tank.tankIn));
        widgetOutputGasTank = addWidget(new WidgetFluidTank(this, tank.tankGasOut));
        widgetOutputLiquidTank = addWidget(new WidgetFluidTank(this, tank.tankLiquidOut));
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int index) {
        // The only slots are player slots -- try to interact with the tank

        if (!player.world.isRemote) {
            Slot slot = inventorySlots.get(index);
            ItemStack stack = slot.getStack();
            ItemStack original = stack.copy();
            stack = tile.tankIn.transferStackToTank(this, stack);
            if (!ItemStack.areItemStacksEqual(stack, original)) {
                slot.putStack(stack);
                detectAndSendChanges();
                return ItemStack.EMPTY;
            }
        }

        return super.transferStackInSlot(player, index);
    }
}
