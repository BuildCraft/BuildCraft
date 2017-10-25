/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.widget.WidgetFluidTank;

import buildcraft.energy.tile.TileEngineIron_BC8;

public class ContainerEngineIron_BC8 extends ContainerBCTile<TileEngineIron_BC8> {
    public final WidgetFluidTank widgetTankFuel;
    public final WidgetFluidTank widgetTankCoolant;
    public final WidgetFluidTank widgetTankResidue;

    public ContainerEngineIron_BC8(EntityPlayer player, TileEngineIron_BC8 engine) {
        super(player, engine);

        addFullPlayerInventory(95);

        widgetTankFuel = addWidget(new WidgetFluidTank(this, engine.tankFuel));
        widgetTankCoolant = addWidget(new WidgetFluidTank(this, engine.tankCoolant));
        widgetTankResidue = addWidget(new WidgetFluidTank(this, engine.tankResidue));
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tile.canInteractWith(player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        // The only slots are player slots -- try to interact with all of the tanks

        if (!player.world.isRemote) {
            Slot slot = inventorySlots.get(index);
            ItemStack stack = slot.getStack();
            ItemStack original = stack.copy();
            stack = tile.tankFuel.transferStackToTank(this, stack);
            if (!ItemStack.areItemStacksEqual(stack, original)) {
                slot.putStack(stack);
                detectAndSendChanges();
                return ItemStack.EMPTY;
            }
            stack = tile.tankCoolant.transferStackToTank(this, stack);
            if (!ItemStack.areItemStacksEqual(stack, original)) {
                slot.putStack(stack);
                detectAndSendChanges();
                return ItemStack.EMPTY;
            }
            stack = tile.tankResidue.transferStackToTank(this, stack);
            if (!ItemStack.areItemStacksEqual(stack, original)) {
                slot.putStack(stack);
                detectAndSendChanges();
                return ItemStack.EMPTY;
            }
        }

        return super.transferStackInSlot(player, index);
    }
}
