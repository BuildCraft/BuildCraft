/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.energy.menu;

import ct.buildcraft.energy.BCEnergyGuis;
import ct.buildcraft.energy.tile.TileEngineIron_BC8;
import ct.buildcraft.lib.gui.ContainerBCTile;
import ct.buildcraft.lib.gui.widget.WidgetFluidTank;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;

public class ContainerEngineIron_BC8 extends ContainerBCTile<TileEngineIron_BC8> {
    public final WidgetFluidTank widgetTankFuel;
    public final WidgetFluidTank widgetTankCoolant;
    public final WidgetFluidTank widgetTankResidue;
    
	public ContainerEngineIron_BC8(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
		this(containerId, playerInventory, CreateClientLevelAccess(buf));
	}

    public ContainerEngineIron_BC8(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(BCEnergyGuis.MENU_IRON.get(), playerInventory, containerId, access);

        addFullPlayerInventory(95);

        widgetTankFuel = addWidget(new WidgetFluidTank(this, tile.tankFuel));
        widgetTankCoolant = addWidget(new WidgetFluidTank(this, tile.tankCoolant));
        widgetTankResidue = addWidget(new WidgetFluidTank(this, tile.tankResidue));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // The only slots are player slots -- try to interact with all of the tanks

        if (!player.level.isClientSide) {
            ItemStack stack = playerInventory.getItem(index);
            ItemStack original = stack.copy();
            stack = tile.tankFuel.transferStackToTank(player, stack);
            if (!ItemStack.isSame(stack, original)) {
                playerInventory.setItem(index, stack);
                broadcastChanges();
                return ItemStack.EMPTY;
            }
            stack = tile.tankCoolant.transferStackToTank(player, stack);
            if (!ItemStack.isSame(stack, original)) {
                playerInventory.setItem(index, stack);
                broadcastChanges();
                return ItemStack.EMPTY;
            }
            stack = tile.tankResidue.transferStackToTank(player, stack);
            if (!ItemStack.isSame(stack, original)) {
                playerInventory.setItem(index, stack);
                broadcastChanges();
                return ItemStack.EMPTY;
            }
        }

        return super.quickMoveStack(player, index);
    }
}
