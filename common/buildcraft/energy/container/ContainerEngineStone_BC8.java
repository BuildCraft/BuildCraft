/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.container;

import buildcraft.energy.tile.TileEngineStone_BC8;
import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

public class ContainerEngineStone_BC8 extends ContainerBCTile<TileEngineStone_BC8> {
    public ContainerEngineStone_BC8(MenuType menuType, int id, Player player, TileEngineStone_BC8 engine) {
        super(menuType, id, player, engine);

        addFullPlayerInventory(84);
//        addSlotToContainer(new SlotBase(engine.invFuel, 0, 80, 41));
        addSlot(new SlotBase(engine.invFuel, 0, 80, 41));
    }
}
