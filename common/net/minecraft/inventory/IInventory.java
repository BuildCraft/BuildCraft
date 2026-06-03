// STUB(R.Chen): IInventory → net.minecraft.inventory.Inventory shim for 1.12.2 compat
package net.minecraft.inventory;

public interface IInventory extends Inventory {
    default int getSizeInventory() { return size(); }
}
