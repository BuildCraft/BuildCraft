/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.container;

import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.gui.slot.SlotDisplay;
import buildcraft.silicon.tile.TileAssemblyTable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;

public class ContainerAssemblyTable extends ContainerBCTile<TileAssemblyTable> {
    public ContainerAssemblyTable(MenuType menuType, int id, Player player, TileAssemblyTable tile) {
        super(menuType, id, player, tile);
        addFullPlayerInventory(123);

        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 3; x++) {
//                addSlotToContainer(new SlotBase(tile.inv, x + y * 3, 8 + x * 18, 36 + y * 18));
                addSlot(new SlotBase(tile.inv, x + y * 3, 8 + x * 18, 36 + y * 18));
            }
        }

        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 3; x++) {
//                addSlotToContainer(new SlotDisplay(this::getDisplay, x + y * 3, 116 + x * 18, 36 + y * 18));
                addSlot(new SlotDisplay(this::getDisplay, x + y * 3, 116 + x * 18, 36 + y * 18));
            }
        }
    }

    private ItemStack getDisplay(int index) {
        return index < tile.recipesStates.size()
                ? new ArrayList<>(tile.recipesStates.keySet()).get(index).output
                : ItemStack.EMPTY;
    }
}
