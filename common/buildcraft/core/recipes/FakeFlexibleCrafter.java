package buildcraft.core.recipes;

import buildcraft.api.recipes.IFlexibleCrafter;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/**
 * Use this class for simulated crafts.
 */
public class FakeFlexibleCrafter implements IFlexibleCrafter {
    private final IFlexibleCrafter original;
    private int[] usedItems, usedFluids;

    public FakeFlexibleCrafter(IFlexibleCrafter original) {
        this.original = original;
        this.usedFluids = new int[original.getCraftingFluidStackSize()];
        this.usedItems = new int[original.getCraftingItemStackSize()];
    }

    @Override
    public ItemStack getCraftingItemStack(int slotId) {
        ItemStack output = original.getCraftingItemStack(slotId);
        if (usedItems[slotId] == 0) {
            return output;
        } else if (output.stackSize <= usedItems[slotId]) {
            return null;
        }
        output = output.copy();
        output.stackSize -= usedItems[slotId];
        return output;
    }

    @Override
    public ItemStack decrCraftingItemStack(int slotId, int amount) {
        ItemStack output = original.getCraftingItemStack(slotId);
        int result = Math.min(output.stackSize - usedItems[slotId], amount);
        usedItems[slotId] += result;

        if (result == 0) {
            return null;
        }
        ItemStack decrOut = output.copy();
        decrOut.stackSize = result;
        return decrOut;
    }

    @Override
    public int getCraftingItemStackSize() {
        return this.usedItems.length;
    }

    @Override
    public FluidStack getCraftingFluidStack(int slotId) {
        FluidStack output = original.getCraftingFluidStack(slotId);
        if (usedFluids[slotId] == 0) {
            return output;
        } else if (output.amount <= usedFluids[slotId]) {
            return null;
        }
        output = output.copy();
        output.amount -= usedFluids[slotId];
        return output;
    }

    @Override
    public FluidStack decrCraftingFluidStack(int slotId, int amount) {
        FluidStack output = original.getCraftingFluidStack(slotId);
        int result = Math.min(output.amount - usedFluids[slotId], amount);
        usedFluids[slotId] += result;

        if (result == 0) {
            return null;
        }
        FluidStack decrOut = output.copy();
        decrOut.amount = result;
        return decrOut;
    }

    @Override
    public int getCraftingFluidStackSize() {
        return this.usedFluids.length;
    }
}
