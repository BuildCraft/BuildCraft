/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.container;

import buildcraft.energy.tile.TileDynamoMJ;
import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

public class ContainerDynamoMJ extends ContainerBCTile<TileDynamoMJ> {

    public ContainerDynamoMJ(MenuType menuType, int id, Player player, TileDynamoMJ engine) {
        super(menuType, id, player, engine);

        addFullPlayerInventory(95);
        for (int slot = 0; slot < 4; slot++) {
            // addSlotToContainer(new SlotBase(engine.invUpgrades, slot, 44 + 18 * slot, 44));
            addSlot(new SlotBase(engine.invUpgrades, slot, 44 + 18 * slot, 44));
        }
    }
}
