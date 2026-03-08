/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.container;

import buildcraft.factory.tile.TileTank;
import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.widget.WidgetFluidTank;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerTank extends ContainerBCTile<TileTank> {
    public final WidgetFluidTank widgetTank;

    public ContainerTank(MenuType menuType, int id, Player player, TileTank tank) {
        super(menuType, id, player, tank);

        addFullPlayerInventory(99);

        widgetTank = addWidget(new WidgetFluidTank(this, tank.tank));
    }

    @Override
    // public ItemStack transferStackInSlot(EntityPlayer player, int index)
    public ItemStack quickMoveStack(Player player, int index) {
        // The only slots are player slots -- try to interact with the tank

        if (!player.level.isClientSide) {
            // Slot slot = inventorySlots.get(index);
            Slot slot = slots.get(index);
            ItemStack stack = slot.getItem();
            ItemStack original = stack.copy();
            stack = tile.tank.transferStackToTank(this, stack);
            tile.balanceTankFluids();
            // if (!ItemStack.areItemStacksEqual(stack, original))
            if (!ItemStack.matches(stack, original)) {
                slot.set(stack);
                // detectAndSendChanges();
                broadcastChanges();
                return ItemStack.EMPTY;
            }
        }

        // return super.transferStackInSlot(player, index);
        return super.quickMoveStack(player, index);
    }
}
