/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.container;

import buildcraft.api.enums.EnumSnapshotType;
import buildcraft.builders.tile.TileBuilder;
import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.gui.slot.SlotDisplay;
import buildcraft.lib.gui.widget.WidgetFluidTank;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

public class ContainerBuilder extends ContainerBCTile<TileBuilder> {
    public final List<WidgetFluidTank<ContainerBuilder>> widgetTanks;

    public ContainerBuilder(ContainerType menuType, int id, PlayerEntity player, TileBuilder tile) {
        super(menuType, id, player, tile);

        addFullPlayerInventory(140);

//        addSlotToContainer(new SlotBase(tile.invSnapshot, 0, 80, 27));
        addSlot(new SlotBase(tile.invSnapshot, 0, 80, 27));

        for (int sy = 0; sy < 3; sy++) {
            for (int sx = 0; sx < 9; sx++) {
//                addSlotToContainer(new SlotBase(tile.invResources, sx + sy * 9, 8 + sx * 18, 72 + sy * 18));
                addSlot(new SlotBase(tile.invResources, sx + sy * 9, 8 + sx * 18, 72 + sy * 18));
            }
        }

        widgetTanks = tile.getTankManager().stream()
                .map(tank -> new WidgetFluidTank<>(this, tank))
                .map(this::addWidget)
                .collect(Collectors.toList());

        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 4; x++) {
//                addSlotToContainer(new SlotDisplay(this::getDisplay, x + y * 4, 179 + x * 18, 18 + y * 18));
                addSlot(new SlotDisplay(this::getDisplay, x + y * 4, 179 + x * 18, 18 + y * 18));
            }
        }
    }

    private ItemStack getDisplay(int index) {
        return tile.snapshotType == EnumSnapshotType.BLUEPRINT &&
                index < tile.blueprintBuilder.remainingDisplayRequired.size()
                ? tile.blueprintBuilder.remainingDisplayRequired.get(index)
                : ItemStack.EMPTY;
    }
}
