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
import buildcraft.lib.gui.slot.SlotPhantom;
import buildcraft.silicon.tile.TileAdvancedCraftingTable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ContainerType;

//public class ContainerAdvancedCraftingTable extends ContainerWithRecipeBookBCTile<TileAdvancedCraftingTable>
public class ContainerAdvancedCraftingTable extends ContainerBCTile<TileAdvancedCraftingTable> {
    public ContainerAdvancedCraftingTable(ContainerType menuType, int id, PlayerEntity player, TileAdvancedCraftingTable tile) {
        super(menuType, id, player, tile);
        addFullPlayerInventory(153);

//        addSlotToContainer(new SlotDisplay(i -> tile.resultClient, 0, 127, 33));
        addSlot(new SlotDisplay(i -> tile.resultClient, 0, 127, 33));

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 5; x++) {
//                addSlotToContainer(new SlotBase(tile.invMaterials, x + y * 5, 15 + x * 18, 85 + y * 18));
                addSlot(new SlotBase(tile.invMaterials, x + y * 5, 15 + x * 18, 85 + y * 18));
            }
        }

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
//                addSlotToContainer(new SlotOutput(tile.invResults, x + y * 3, 109 + x * 18, 85 + y * 18));
                addSlot(new SlotOutput(tile.invResults, x + y * 3, 109 + x * 18, 85 + y * 18));
            }
        }

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
//                addSlotToContainer(new SlotPhantom(tile.invBlueprint, x + y * 3, 33 + x * 18, 16 + y * 18, false));
                addSlot(new SlotPhantom(tile.invBlueprint, x + y * 3, 33 + x * 18, 16 + y * 18, false));
            }
        }
    }
}
