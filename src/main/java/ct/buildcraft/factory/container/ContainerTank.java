/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.factory.container;

import ct.buildcraft.factory.BCFactoryGuis;
import ct.buildcraft.factory.tile.TileTank;
import ct.buildcraft.lib.fluid.Tank;
import ct.buildcraft.lib.gui.ContainerBCTile;
import ct.buildcraft.lib.gui.widget.WidgetFluidTank;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;

public class ContainerTank extends ContainerBCTile<TileTank> {
    private static final ForgeRegistry<Fluid> FLUIDS = (ForgeRegistry<Fluid>) ForgeRegistries.FLUIDS;

    public final ContainerData data;
    public final WidgetFluidTank widgetTank;

    public ContainerTank(int containerId, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(containerId, playerInventory, new SimpleContainerData(2), CreateClientLevelAccess(buffer));
    }

    public ContainerTank(int containerId, Inventory playerInventory, TileTank tank, ContainerLevelAccess access) {
        this(containerId, playerInventory, new TankData(tank), access);
    }

    private ContainerTank(int containerId, Inventory playerInventory, ContainerData data, ContainerLevelAccess access) {
        super(BCFactoryGuis.MENU_TANK.get(), playerInventory, containerId, access);
        this.data = data;

        addFullPlayerInventory(8, 99);
        addDataSlots(data);

        Tank guiTank = tile != null ? tile.tank : new Tank("tank", 16 * FluidType.BUCKET_VOLUME, null);
        widgetTank = addWidget(new WidgetFluidTank(this, guiTank));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }

        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack before = slot.getItem().copy();
        ItemStack after = access.evaluate((level, pos) -> {
            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof TileTank tile)) {
                return slot.getItem();
            }

            ItemStack result = tile.tank.transferStackToTank(player, slot.getItem());
            tile.balanceTankFluids();
            tile.setChanged();
            return result;
        }).orElse(slot.getItem());

        if (!ItemStack.matches(after, before)) {
            slot.set(after);
            slot.setChanged();
            broadcastChanges();
            return before;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean clickMenuButton(Player player, int index) {
        return access.evaluate((level, pos) -> {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TileTank tile) {
                tile.tank.onGuiClicked(this, player);
                tile.balanceTankFluids();
                tile.setChanged();
                broadcastChanges();
                return true;
            }
            return false;
        }).orElse(false);
    }

    private static final class TankData implements ContainerData {
        private final TileTank tank;

        private TankData(TileTank tank) {
            this.tank = tank;
        }

        @Override
        public int get(int index) {
            FluidStack fluid = tank.tank.getFluid();
            return switch (index) {
                case 0 -> FLUIDS.getID(fluid.isEmpty() ? Fluids.EMPTY : fluid.getFluid());
                case 1 -> fluid.getAmount();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
