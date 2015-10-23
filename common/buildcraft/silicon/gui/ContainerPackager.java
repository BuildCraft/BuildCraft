/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.silicon.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import buildcraft.core.lib.gui.BuildCraftContainer;
import buildcraft.core.lib.gui.slots.SlotOutput;
import buildcraft.core.lib.gui.slots.SlotValidated;
import buildcraft.silicon.TilePackager;

public class ContainerPackager extends BuildCraftContainer {
    private final TilePackager tile;

    // private int lastProgress;

    public ContainerPackager(EntityPlayer player, TilePackager t) {
        super(player, t.getSizeInventory());

        this.tile = t;

        // sort in order of shift-click!

        addSlotToContainer(new SlotValidated(tile, 9, 124, 7));

        for (int x = 0; x < 9; x++) {
            addSlotToContainer(new Slot(tile, x, 8 + x * 18, 84));
        }

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                addSlotToContainer(new SlotPackager(tile, 12 + x + y * 3, 30 + x * 18, 17 + y * 18));
            }
        }

        addSlotToContainer(new Slot(tile, 10, 108, 31));
        addSlotToContainer(new SlotOutput(tile, 11, 123, 59));

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                addSlotToContainer(new Slot(player.inventory, x + y * 9 + 9, 8 + x * 18, 115 + y * 18));
            }
        }

        for (int x = 0; x < 9; x++) {
            addSlotToContainer(new Slot(player.inventory, x, 8 + x * 18, 173));
        }

        onCraftMatrixChanged(tile);
    }

    @Override
    public void updateProgressBar(int id, int data) {
        /* switch (id) { case 0: tile.progress = data; break; } */
    }

    @Override
    public ItemStack slotClick(int slotNum, int mouseButton, int modifier, EntityPlayer player) {
        ItemStack out = super.slotClick(slotNum, mouseButton, modifier, player);
        Slot slot = slotNum < 0 ? null : (Slot) this.inventorySlots.get(slotNum);

        if (slot instanceof SlotPackager) {
            int idx = slot.getSlotIndex() - 12;
            ItemStack stack = player != null && player.inventory != null ? player.inventory.getItemStack() : null;
            if (stack == null) {
                tile.setPatternSlot(idx, !tile.isPatternSlotSet(idx));
            } else {
                tile.setPatternSlot(idx, true);
            }
            tile.sendNetworkUpdate();
        }
        return out;
    }

    @Override
    public boolean canInteractWith(EntityPlayer entityplayer) {
        return tile.isUseableByPlayer(entityplayer);
    }
}
