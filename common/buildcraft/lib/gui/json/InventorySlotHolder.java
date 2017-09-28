package buildcraft.lib.gui.json;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class InventorySlotHolder {

    public final Slot[] slots;

    public InventorySlotHolder(Container container, IInventory inventory) {
        List<Slot> list = new ArrayList<>();
        for (Slot s : container.inventorySlots) {
            if (s.inventory == inventory) {
                list.add(s);
            }
        }
        slots = list.toArray(new Slot[0]);
    }

    public InventorySlotHolder(Container container, IItemHandler inventory) {
        List<Slot> list = new ArrayList<>();
        for (Slot s : container.inventorySlots) {
            if (s instanceof SlotItemHandler && ((SlotItemHandler) s).getItemHandler() == inventory) {
                list.add(s);
            }
        }
        slots = list.toArray(new Slot[0]);
    }
}
