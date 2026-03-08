/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.silicon.container;

import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotValidated;
import buildcraft.silicon.tile.TileChargingTable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

public class ContainerChargingTable extends ContainerBCTile<TileChargingTable> {

    private TileChargingTable table;

    // public ContainerChargingTable(EntityPlayer player, TileChargingTable table)
    public ContainerChargingTable(MenuType menuType, int id, Player player, TileChargingTable tile) {
        // super(player, table.getSizeInventory());
        super(menuType, id, player, tile);
        // this.table = table;
        this.table = tile;

        addFullPlayerInventory(50);

        // addSlot(new SlotValidated(table, 0, 80, 18));
        addSlot(new SlotValidated(table.inv, 0, 80, 18));

//        for (int y = 0; y < 3; y++) {
//            for (int x = 0; x < 9; x++) {
//                addSlotToContainer(new Slot(player.inventory, x + y * 9 + 9, 8 + x * 18, 50 + y * 18));
//            }
//        }

//        for (int x = 0; x < 9; x++) {
//            addSlotToContainer(new Slot(player.inventory, x, 8 + x * 18, 108));
//        }
    }

//    @Override
//    // public boolean canInteractWith(EntityPlayer var1)
//    public boolean stillValid(Player player) {
//        // return table.isUseableByPlayer(var1);
//        return table.canInteractWith(player);
//    }

//    @Override
//    public void updateProgressBar(int i, int j) {
//        table.getGUINetworkData(i, j);
//    }

//    @Override
//    public void detectAndSendChanges() {
//        super.detectAndSendChanges();
//
//        for (Object crafter : crafters) {
//            table.sendGUINetworkData(this, (ICrafting) crafter);
//        }
//    }
}
