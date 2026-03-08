/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.container;

import buildcraft.builders.tile.TileReplacer;
import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

public class ContainerReplacer extends ContainerBCTile<TileReplacer> {
    public ContainerReplacer(MenuType menuType, int id, Player player, TileReplacer tile) {
        super(menuType, id, player, tile);

//        addSlotToContainer(new SlotBase(tile.invSnapshot, 0, 8, 115));
        addSlot(new SlotBase(tile.invSnapshot, 0, 8, 115));
//        addSlotToContainer(new SlotBase(tile.invSchematicFrom, 0, 8, 137));
        addSlot(new SlotBase(tile.invSchematicFrom, 0, 8, 137));
//        addSlotToContainer(new SlotBase(tile.invSchematicTo, 0, 56, 137));
        addSlot(new SlotBase(tile.invSchematicTo, 0, 56, 137));

        addFullPlayerInventory(159);
    }
}
