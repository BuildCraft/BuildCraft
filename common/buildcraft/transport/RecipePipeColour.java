package buildcraft.transport;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import buildcraft.lib.client.guide.parts.ChangingItemStack;
import buildcraft.lib.client.guide.parts.IRecipeViewable;

public class RecipePipeColour implements IRecipeViewable {

    private final ItemStack output;
    /** Single-dimension because all pipe recipes use 3 items or less. */
    private final Object[] required;
    private final boolean shaped;

    public RecipePipeColour(ItemStack out, Object[] required, boolean shaped) {
        this.output = out;
        this.required = required;
        this.shaped = shaped;
    }

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("Implement this!");
        return false;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("Implement this!");
        return null;
    }

    @Override
    public int getRecipeSize() {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("Implement this!");
        return 0;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return output;
    }

    @Override
    public ItemStack[] getRemainingItems(InventoryCrafting inv) {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("Implement this!");
        return null;
    }

    @Override
    public ChangingItemStack[][] getRecipeInputs() {
        return null;
    }

    @Override
    public ChangingItemStack getRecipeOutputs() {
        return null;
    }
}
