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
import buildcraft.silicon.tile.TileProgrammingTable_Neptune;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class ContainerProgrammingTable_Neptune extends ContainerBCTile<TileProgrammingTable_Neptune> {
    // IInventory playerIInventory;
    // Container playerIInventory;
    // TileProgrammingTable table;
    // TileProgrammingTable_Neptune table;

    public ContainerProgrammingTable_Neptune(MenuType menuType, int id, Player player, TileProgrammingTable_Neptune tile) {
        super(menuType, id, player, tile);

        // this.playerIInventory = player.inventory;
        // this.playerIInventory = player.getInventory();
        // this.table = tile;

        addFullPlayerInventory(123);

        // addSlotToContainer(new Slot(table, 0, 8, 36));
        addSlot(new SlotBase(tile.input, 0, 8, 36));
        // addSlotToContainer(new Slot(table, 1, 8, 90));
        addSlot(new SlotOutput(tile.output, 0, 8, 90));

        // for (int l = 0; l < 3; l++) {
        //     for (int k1 = 0; k1 < 9; k1++) {
        //         addSlotToContainer(new Slot(player.inventory, k1 + l * 9 + 9, 8 + k1 * 18, 123 + l * 18));
        //     }
        // }

        // for (int i1 = 0; i1 < 9; i1++) {
        //     addSlotToContainer(new Slot(player.inventory, i1, 8 + i1 * 18, 181));
        // }

        for (int j = 0; j < TileProgrammingTable_Neptune.HEIGHT; ++j) {
            for (int i = 0; i < TileProgrammingTable_Neptune.WIDTH; ++i) {
                // slots.add(new RecipeSlot(43 + 18 * i, 36 + 18 * j, (j * TileProgrammingTable.WIDTH) + i));
                addSlot(new SlotDisplay(this::getDisplay, (j * TileProgrammingTable_Neptune.WIDTH) + i, 43 + 18 * i, 36 + 18 * j));
            }
        }
    }

    // Calen 1.18.2: in super
    // @Override
    // public boolean canInteractWith(EntityPlayer entityplayer) {
    //     return table.isUseableByPlayer(entityplayer);
    // }

    // @Override
    // public void updateProgressBar(int i, int j) {
    //     table.getGUINetworkData(i, j);
    // }

    // @Override
    // public void detectAndSendChanges() {
    //     super.detectAndSendChanges();
    //
    //     for (Object crafter : crafters) {
    //         table.sendGUINetworkData(this, (ICrafting) crafter);
    //     }
    // }

    // Calen 1.18.2 from ContainerAssemblyTable
    private ItemStack getDisplay(int index) {
        return (index < tile.optionRecipes.size() && tile.optionRecipes.get(index) != null)
                ? tile.optionRecipes.get(index).getOutput()
                : ItemStack.EMPTY;
    }
}
