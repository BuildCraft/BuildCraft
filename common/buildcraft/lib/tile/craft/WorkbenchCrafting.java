/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.tile.craft;

import javax.annotation.Nullable;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;

import net.minecraftforge.items.IItemHandler;

import buildcraft.lib.inventory.filter.ArrayStackFilter;
import buildcraft.lib.misc.CraftingUtil;
import buildcraft.lib.misc.InventoryUtil;
import buildcraft.lib.misc.ItemStackKey;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.item.ItemHandlerSimple;

public class WorkbenchCrafting extends InventoryCrafting {
    enum EnumRecipeType {
        INGREDIENTS,
        EXACT_STACKS;
    }

    public static final Container CONTAINER_EVENT_HANDLER = new ContainerNullEventHandler();

    private final TileEntity tile;
    private final ItemHandlerSimple invBlueprint;
    private final ItemHandlerSimple invMaterials;
    private final ItemHandlerSimple invResult;
    private boolean isBlueprintDirty = true;
    private boolean areMaterialsDirty = true;
    private boolean cachedHasRequirements = false;

    @Nullable
    private IRecipe currentRecipe;
    private ItemStack assumedResult = ItemStack.EMPTY;

    private EnumRecipeType recipeType = null;

    // Ok so this is fairly annoying...
    // it would be nice to require ingredients rather than hard stacks
    // and just move the relevant ingredients into the crafting table rather than
    // anything else

    public WorkbenchCrafting(int width, int height, TileBC_Neptune tile, ItemHandlerSimple invBlueprint,
        ItemHandlerSimple invMaterials, ItemHandlerSimple invResult) {
        super(CONTAINER_EVENT_HANDLER, width, height);
        this.tile = tile;
        this.invBlueprint = invBlueprint;
        if (invBlueprint.getSlots() < this.getSizeInventory()) {
            throw new IllegalArgumentException("Passed blueprint has a smaller size than width * height! ( expected "
                + getSizeInventory() + ", got " + invBlueprint.getSlots() + ")");
        }
        this.invMaterials = invMaterials;
        this.invResult = invResult;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return isBlueprintDirty ? invBlueprint.getStackInSlot(index) : super.getStackInSlot(index);
    }

    public ItemStack getAssumedResult() {
        return assumedResult;
    }

    public void onInventoryChange(IItemHandler inv) {
        if (inv == invBlueprint) {
            isBlueprintDirty = true;
        } else if (inv == invMaterials) {
            areMaterialsDirty = true;
        }
    }

    /** @return True if anything changed, false otherwise */
    public boolean tick() {
        if (tile.getWorld().isRemote) {
            throw new IllegalStateException("Never call this on the client side!");
        }
        if (isBlueprintDirty) {
            currentRecipe = CraftingUtil.findMatchingRecipe(this, tile.getWorld());
            if (currentRecipe == null) {
                assumedResult = ItemStack.EMPTY;
                recipeType = null;
            } else {
                assumedResult = currentRecipe.getCraftingResult(this);
                NonNullList<Ingredient> ingredients = currentRecipe.getIngredients();
                if (ingredients.isEmpty()) {
                    recipeType = EnumRecipeType.EXACT_STACKS;
                } else {
                    recipeType = EnumRecipeType.INGREDIENTS;
                }
            }
            isBlueprintDirty = false;
            return true;
        }
        return false;
    }

    /** @return True if {@link #craft()} might return true, or false if {@link #craft()} will definitely return
     *         false. */
    public boolean canCraft() {
        if (currentRecipe == null || isBlueprintDirty) {
            return false;
        }
        if (!invResult.canFullyAccept(assumedResult)) {
            return false;
        }
        if (areMaterialsDirty) {
            areMaterialsDirty = false;
            switch (recipeType) {
                case INGREDIENTS: {
                    // cachedHasRequirements = hasIngredients();
                    // break;
                }
                case EXACT_STACKS: {
                    cachedHasRequirements = hasExactStacks();
                    break;
                }
                default: {
                    throw new IllegalStateException("Unknown recipe type " + recipeType);
                }
            }
        }
        return cachedHasRequirements;
    }

    /** Attempts to craft a single item. Assumes that {@link #canCraft()} has been called in the same tick, without any
     * modifications happening to the
     * 
     * @return True if the crafting happened, false otherwise. *
     * @throws IllegalStateException if {@link #canCraft()} hasn't been called before, or something changed in the
     *             meantime. */
    public boolean craft() throws IllegalStateException {
        if (isBlueprintDirty) {
            return false;
        }

        switch (recipeType) {
            case INGREDIENTS: {
                // return craftByIngredients();
            }
            case EXACT_STACKS: {
                return craftExact();
            }
            default: {
                throw new IllegalStateException("Unknown recipe type " + recipeType);
            }
        }
    }

    private boolean hasExactStacks() {
        TObjectIntMap<ItemStackKey> required = new TObjectIntHashMap<>(getSizeInventory());
        for (int s = 0; s < getSizeInventory(); s++) {
            ItemStack req = invBlueprint.getStackInSlot(s);
            if (!req.isEmpty()) {
                int count = req.getCount();
                if (count != 1) {
                    req = req.copy();
                    req.setCount(1);
                }
                ItemStackKey key = new ItemStackKey(req);
                required.adjustOrPutValue(key, count, count);
            }
        }
        return required.forEachEntry((stack, count) -> {
            ArrayStackFilter filter = new ArrayStackFilter(stack.baseStack);
            ItemStack inInventory = invMaterials.extract(filter, count, count, true);
            return !inInventory.isEmpty() && inInventory.getCount() == count;
        });
    }

    /** Implementation of {@link #craft()}, assuming nothing about the current recipe. */
    private boolean craftExact() {
        // 4 steps:
        // - Move everything out of this inventory (Just to check: state correction operation)
        // - Attempt to move every exact item from invMaterials to this inventory
        // - Call normal crafting stuffs
        // - Move everything from the inventory back to materials

        // Step 1
        clearInventory();

        // Step 2
        for (int s = 0; s < getSizeInventory(); s++) {
            ItemStack bpt = invBlueprint.getStackInSlot(s);
            if (!bpt.isEmpty()) {
                ItemStack stack = invMaterials.extract(new ArrayStackFilter(bpt), 1, 1, false);
                if (stack.isEmpty()) {
                    clearInventory();
                    return false;
                }
                setInventorySlotContents(s, stack);
            }
        }

        // Step 3
        ItemStack result = currentRecipe.getCraftingResult(this);
        if (result.isEmpty()) {
            // what?
            clearInventory();
            return false;
        }
        ItemStack leftover = invResult.insert(result, false, false);
        if (!leftover.isEmpty()) {
            InventoryUtil.addToBestAcceptor(tile.getWorld(), tile.getPos(), null, leftover);
        }
        NonNullList<ItemStack> remainingStacks = currentRecipe.getRemainingItems(this);
        for (int s = 0; s < remainingStacks.size(); s++) {
            ItemStack inSlot = getStackInSlot(s);
            ItemStack remaining = remainingStacks.get(s);

            if (!inSlot.isEmpty()) {
                decrStackSize(s, 1);
                inSlot = getStackInSlot(s);
            }

            if (!remaining.isEmpty()) {
                if (inSlot.isEmpty()) {
                    setInventorySlotContents(s, remaining);
                } else if (ItemStack.areItemsEqual(inSlot, remaining)
                    && ItemStack.areItemStackTagsEqual(inSlot, remaining)) {
                    remaining.grow(inSlot.getCount());
                    setInventorySlotContents(s, remaining);
                } else {
                    leftover = invMaterials.insert(remaining, false, false);
                    if (!leftover.isEmpty()) {
                        InventoryUtil.addToBestAcceptor(tile.getWorld(), tile.getPos(), null, leftover);
                    }
                }
            }
        }

        // Step 4
        // Some ingredients really need to be removed (like empty buckets)
        for (int s = 0; s < getSizeInventory(); s++) {
            ItemStack inSlot = getStackInSlot(s);
            if (!inSlot.isEmpty()) {
                leftover = invMaterials.insert(inSlot, false, false);
                if (!leftover.isEmpty()) {
                    InventoryUtil.addToBestAcceptor(tile.getWorld(), tile.getPos(), null, leftover);
                }
            }
        }
        return true;
    }

    /** @return True if this inventory is now clear, false otherwise. */
    private boolean clearInventory() {
        for (int s = 0; s < getSizeInventory(); s++) {
            ItemStack inSlot = getStackInSlot(s);
            if (!inSlot.isEmpty()) {
                ItemStack leftover = invMaterials.insert(inSlot, false, false);
                decrStackSize(s, inSlot.getCount() - (leftover.isEmpty() ? 0 : leftover.getCount()));
                if (!leftover.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    static class ContainerNullEventHandler extends Container {
        @Override
        public boolean canInteractWith(EntityPlayer playerIn) {
            return false;
        }

        @Override
        public void onCraftMatrixChanged(IInventory inventoryIn) {
            // NO-OP
        }
    }
}
