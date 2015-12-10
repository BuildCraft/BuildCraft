package buildcraft.transport;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

import net.minecraftforge.common.ForgeHooks;

import buildcraft.core.lib.utils.ColorUtils;

public class PipeColoringRecipe implements IRecipe {
    @Override
    public boolean matches(InventoryCrafting crafting, World world) {
        return new RecipeHolder(crafting).output != null;
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
            if (itemstack == null) continue;
            if (itemstack.stackSize == 1) items[i] = ForgeHooks.getContainerItem(itemstack);
        }

        return items;
    }

    private static class RecipeHolder {
        final int dyeIndex, bleachIndex, pipeCount, pipeFirstIndex;
        /** Will be null if the recipe is invalid */
        final ItemStack output;

        public RecipeHolder(InventoryCrafting crafting) {
            boolean valid = true;

            int pipeCount = 0;
            int pipeFirstIndex = -1;
            int dye = -1;
            int bleach = -1;
            // -1 for unset, -2 for wildcard (its a bleach)
            int pipeDamage = -1;

            Item pipeItem = null;

            for (int i = 0; i < 9; i++) {
                ItemStack stack = crafting.getStackInSlot(i);
                if (stack == null || stack.getItem() == null || stack.stackSize == 0) {
                    continue;
                }

                if (stack.getItem() instanceof ItemPipe) {
                    if (pipeFirstIndex == -1) {
                        pipeFirstIndex = i;
                        pipeItem = stack.getItem();
                    } else if (stack.getItem() != pipeItem) valid = false;
                    pipeCount++;
                    if (pipeDamage == -1) {
                        pipeDamage = stack.getItemDamage();
                    } else if (pipeDamage != stack.getItemDamage()) pipeDamage = -2;
                } else if (stack.getItem() == Items.water_bucket) {
                    if (bleach == -1 && dye == -1) bleach = i;
                    else valid = false;
                } else if (ColorUtils.isDye(stack)) {
                    if (bleach == -1 && dye == -1) dye = i;
                    else valid = false;
                } else valid = false;
            }

            this.pipeCount = pipeCount;
            this.pipeFirstIndex = pipeFirstIndex;
            this.bleachIndex = bleach;
            this.dyeIndex = dye;

            if (bleachIndex == -1 && dyeIndex == -1) valid = false;
            if (pipeCount == 0) valid = false;

            if (valid) {
                int damage;
                if (bleach != -1) damage = 0;
                else {
                    ItemStack dyeStack = crafting.getStackInSlot(dye);
                    damage = ColorUtils.getColorFromDye(dyeStack).getMetadata() + 1;
                }
                if (damage == pipeDamage) output = null;
                else {
                    output = crafting.getStackInSlot(pipeFirstIndex).copy();
                    output.stackSize = pipeCount;
                    output.setItemDamage(damage);
                }
            } else {
                output = null;
            }
        }
    }
}
