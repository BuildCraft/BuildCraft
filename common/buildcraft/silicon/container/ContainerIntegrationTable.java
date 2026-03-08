/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.container;

import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.gui.slot.SlotDisplay;
import buildcraft.lib.gui.slot.SlotOutput;
import buildcraft.silicon.tile.TileIntegrationTable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

public class ContainerIntegrationTable extends ContainerBCTile<TileIntegrationTable> {
    public ContainerIntegrationTable(MenuType menuType, int id, Player player, TileIntegrationTable tile) {
        super(menuType, id, player, tile);
        addFullPlayerInventory(109);

        int[] indexes = { 0, 1, 2, 3, 0, 4, 5, 6, 7 };

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
//                addSlotToContainer(new SlotBase((x == 1 && y == 1) ? tile.invTarget : tile.invToIntegrate, indexes[x + y * 3], 19 + x * 25, 24 + y * 25));
                addSlot(new SlotBase((x == 1 && y == 1) ? tile.invTarget : tile.invToIntegrate, indexes[x + y * 3], 19 + x * 25, 24 + y * 25));
            }
        }

//        addSlotToContainer(new SlotDisplay(i -> tile.getOutput(), 0, 101, 36));
        addSlot(new SlotDisplay(i -> tile.getOutput(), 0, 101, 36));

//        addSlotToContainer(new SlotOutput(tile.invResult, 0, 138, 49));
        addSlot(new SlotOutput(tile.invResult, 0, 138, 49));
    }
}
