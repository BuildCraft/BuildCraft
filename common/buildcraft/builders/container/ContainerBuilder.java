/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.container;

import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import buildcraft.api.enums.EnumSnapshotType;

import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.gui.slot.SlotDisplay;
import buildcraft.lib.gui.widget.WidgetFluidTank;

import buildcraft.builders.tile.TileBuilder;

public class ContainerBuilder extends ContainerBCTile<TileBuilder> {
    public final List<WidgetFluidTank> widgetTanks;

    public ContainerBuilder(EntityPlayer player, TileBuilder tile) {
        super(player, tile);

        addFullPlayerInventory(140);

        addSlotToContainer(new SlotBase(tile.invSnapshot, 0, 80, 27));

        for (int sy = 0; sy < 3; sy++) {
            for (int sx = 0; sx < 9; sx++) {
                addSlotToContainer(new SlotBase(tile.invResources, sx + sy * 9, 8 + sx * 18, 72 + sy * 18));
            }
        }

        widgetTanks = tile.getTankManager().stream()
                .map(tank -> new WidgetFluidTank(this, tank))
                .map(this::addWidget)
                .collect(Collectors.toList());

        for(int y = 0; y < 6; y++) {
            for(int x = 0; x < 4; x++) {
                addSlotToContainer(new SlotDisplay(this::getDisplay, x + y * 4, 179 + x * 18, 18 + y * 18));
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
