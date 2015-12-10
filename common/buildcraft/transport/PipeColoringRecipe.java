package buildcraft.transport;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

import net.minecraftforge.common.ForgeHooks;

import buildcraft.core.lib.utils.ColorUtils;

public class PipeColoringRecipe implements IRecipe {
    @Override
    public boolean matches(InventoryCrafting crafting, World world) {
        RecipeHolder holder = new RecipeHolder(crafting);
        return holder.output != null;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting crafting) {
        return new RecipeHolder(crafting).output;
    }

    @Override
    public int getRecipeSize() {
        return 2;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return null;
    }

    // Basically the same as RecipeFireworks
    // TODO (PASS 0): Check if this is right!
    @Override
    public ItemStack[] getRemainingItems(InventoryCrafting inv) {
        RecipeHolder holder = new RecipeHolder(inv);

        ItemStack[] items = new ItemStack[inv.getSizeInventory()];

        for (int i = 0; i < items.length; ++i) {
            ItemStack itemstack = inv.getStackInSlot(i);
            if (i == holder.pipeIndex) {
                if (itemstack.stackSize == 8) items[i] = null;
                else {
                    items[i] = itemstack.copy();
                    items[i].stackSize -= 8;
                }
                inv.setInventorySlotContents(i, null);
            } else items[i] = ForgeHooks.getContainerItem(itemstack);
        }

        return items;
    }

    private static class RecipeHolder {
        final int dyeIndex, bleachIndex, pipeIndex;
        /** Will be null if the recipe is invalid */
        final ItemStack output;

        public RecipeHolder(InventoryCrafting crafting) {
            boolean valid = true;

            int pipe = -1;
            int dye = -1;
            int bleach = -1;

            for (int i = 0; i < 9; i++) {
                ItemStack stack = crafting.getStackInSlot(i);
                if (stack == null || stack.getItem() == null || stack.stackSize == 0) {
                    continue;
                }

                if (stack.getItem() instanceof ItemPipe) {
                    if (pipe == -1 && stack.stackSize >= 8) pipe = i;
                    else valid = false;
                } else if (stack.getItem() == Items.water_bucket) {
                    if (bleach == -1 && dye == -1) bleach = i;
                    else valid = false;
                } else if (ColorUtils.isDye(stack)) {
                    if (bleach == -1 && dye == -1) dye = i;
                    else valid = false;
                } else valid = false;
            }

            pipeIndex = pipe;
            bleachIndex = bleach;
            dyeIndex = dye;

            if (bleachIndex == -1 && dyeIndex == -1) valid = false;
            if (pipeIndex == -1) valid = false;

            if (valid) {
                ItemStack pipes = crafting.getStackInSlot(pipe);
                if (bleach != -1) {
                    output = pipes.copy();
                    output.stackSize = 8;
                    output.setItemDamage(0);
                } else {
                    ItemStack dyeStack = crafting.getStackInSlot(dye);
                    output = pipes.copy();
                    output.stackSize = 8;
                    output.setItemDamage(ColorUtils.getColorFromDye(dyeStack).getMetadata() + 1);
                }
            } else {
                output = null;
            }
        }
    }
}
