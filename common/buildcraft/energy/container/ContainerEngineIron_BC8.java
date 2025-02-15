/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.container;

import buildcraft.energy.tile.TileEngineIron_BC8;
import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.widget.WidgetFluidTank;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerEngineIron_BC8 extends ContainerBCTile<TileEngineIron_BC8> {
    public final WidgetFluidTank<ContainerEngineIron_BC8> widgetTankFuel;
    public final WidgetFluidTank<ContainerEngineIron_BC8> widgetTankCoolant;
    public final WidgetFluidTank<ContainerEngineIron_BC8> widgetTankResidue;

    public ContainerEngineIron_BC8(MenuType menuType, int id, Player player, TileEngineIron_BC8 engine) {
        super(menuType, id, player, engine);

        addFullPlayerInventory(95);

        widgetTankFuel = addWidget(new WidgetFluidTank<>(this, engine.tankFuel));
        widgetTankCoolant = addWidget(new WidgetFluidTank<>(this, engine.tankCoolant));
        widgetTankResidue = addWidget(new WidgetFluidTank<>(this, engine.tankResidue));
    }


    @Override
//    public ItemStack transferStackInSlot(Player player, int index)
    public ItemStack quickMoveStack(Player player, int index) {
        // The only slots are player slots -- try to interact with all of the tanks

        if (!player.level().isClientSide) {
//            Slot slot = inventorySlots.get(index);
            Slot slot = slots.get(index);
//            ItemStack stack = slot.getStack();
            ItemStack stack = slot.getItem();
            ItemStack original = stack.copy();
            stack = tile.tankFuel.transferStackToTank(this, stack);
//            if (!ItemStack.areItemStacksEqual(stack, original))
            if (!ItemStack.matches(stack, original)) {
//                slot.putStack(stack);
                slot.set(stack);
//                detectAndSendChanges();
                broadcastChanges();
                return ItemStack.EMPTY;
            }
            stack = tile.tankCoolant.transferStackToTank(this, stack);
//            if (!ItemStack.areItemStacksEqual(stack, original))
            if (!ItemStack.matches(stack, original)) {
//                slot.putStack(stack);
                slot.set(stack);
//                detectAndSendChanges();
                broadcastChanges();
                return ItemStack.EMPTY;
            }
            stack = tile.tankResidue.transferStackToTank(this, stack);
//            if (!ItemStack.areItemStacksEqual(stack, original))
            if (!ItemStack.matches(stack, original)) {
//                slot.putStack(stack);
                slot.set(stack);
//                detectAndSendChanges();
                broadcastChanges();
                return ItemStack.EMPTY;
            }
        }

//        return super.transferStackInSlot(player, index);
        return super.quickMoveStack(player, index);
    }
}
