// STUB(R.Chen): InventoryCrafting → CraftingInventory shim for 1.12.2 compat
package net.minecraft.inventory;

import net.minecraft.screen.ScreenHandler;

public class InventoryCrafting extends CraftingInventory {
    public InventoryCrafting(ScreenHandler handler, int width, int height) {
        super(handler, width, height);
    }

    @Override
    public boolean canUse(net.minecraft.entity.player.PlayerEntity player) { return true; }
}
