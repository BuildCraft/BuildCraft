package buildcraft.lib.gui.json;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import java.util.ArrayList;
import java.util.List;

public class InventorySlotHolder {

    public final Slot[] slots;

    // public InventorySlotHolder(Container container, IInventory inventory)
    public InventorySlotHolder(AbstractContainerMenu container, Container inventory) {
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

    public InventorySlotHolder(AbstractContainerMenu container, IItemHandler inventory) {
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
