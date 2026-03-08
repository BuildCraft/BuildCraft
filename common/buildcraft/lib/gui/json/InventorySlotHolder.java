package buildcraft.lib.gui.json;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import java.util.ArrayList;
import java.util.List;

public class InventorySlotHolder {

    public final Slot[] slots;

    // public InventorySlotHolder(IInventory container, IInventory inventory)
    public InventorySlotHolder(Container container, IInventory inventory) {
        List<Slot> list = new ArrayList<>();
//        for (Slot s : container.inventorySlots)
        for (Slot s : container.slots) {
//            if (s.inventory == inventory)
            if (s.container == inventory) {
                list.add(s);
            }
        }
        slots = list.toArray(new Slot[0]);
    }

    public InventorySlotHolder(Container container, IItemHandler inventory) {
        List<Slot> list = new ArrayList<>();
//        for (Slot s : container.inventorySlots)
        for (Slot s : container.slots) {
            if (s instanceof SlotItemHandler && ((SlotItemHandler) s).getItemHandler() == inventory) {
                list.add(s);
            }
        }
        slots = list.toArray(new Slot[0]);
    }
}
