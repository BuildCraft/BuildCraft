/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.tile.craft;

import buildcraft.lib.inventory.filter.ArrayStackFilter;
import buildcraft.lib.misc.CraftingUtil;
import buildcraft.lib.misc.InventoryUtil;
import buildcraft.lib.misc.ItemStackKey;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

public class WorkbenchCrafting extends CraftingContainer {
    enum EnumRecipeType {
        INGREDIENTS,
        EXACT_STACKS;
    }

    public static final AbstractContainerMenu CONTAINER_EVENT_HANDLER = new ContainerNullEventHandler();

    private final BlockEntity tile;
    private final ItemHandlerSimple invBlueprint;
    private final ItemHandlerSimple invMaterials;
    private final ItemHandlerSimple invResult;
    private boolean isBlueprintDirty = true;
    private boolean areMaterialsDirty = true;
    private boolean cachedHasRequirements = false;

    @Nullable
    private Recipe currentRecipe;
    private ItemStack assumedResult = ItemStack.EMPTY;

    private EnumRecipeType recipeType = null;

    public WorkbenchCrafting(int width, int height, TileBC_Neptune tile, ItemHandlerSimple invBlueprint,
            ItemHandlerSimple invMaterials, ItemHandlerSimple invResult) {
        super(CONTAINER_EVENT_HANDLER, width, height);
        this.tile = tile;
        this.invBlueprint = invBlueprint;
        if (invBlueprint.getSlots() < this.getContainerSize()) {
            throw new IllegalArgumentException("Passed blueprint has a smaller size than width * height! ( expected "
                    + getContainerSize() + ", got " + invBlueprint.getSlots() + ")");
        }
        this.invMaterials = invMaterials;
        this.invResult = invResult;
    }

    @Override
//    public ItemStack getStackInSlot(int index)
    public ItemStack getItem(int index) {
        return isBlueprintDirty ? invBlueprint.getStackInSlot(index) : super.getItem(index);
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
        if (tile.getLevel().isClientSide) {
            throw new IllegalStateException("Never call this on the client side!");
        }
        if (isBlueprintDirty) {
            currentRecipe = CraftingUtil.findMatchingRecipe(this, tile.getLevel());
            if (currentRecipe == null) {
                assumedResult = ItemStack.EMPTY;
                recipeType = null;
            } else {
                assumedResult = currentRecipe.getResultItem();
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
                case INGREDIENTS:
                    // cachedHasRequirements = hasIngredients();
                    // break;
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
            case INGREDIENTS:
                // return craftByIngredients();
            case EXACT_STACKS: {
                return craftExact();
            }
            default: {
                throw new IllegalStateException("Unknown recipe type " + recipeType);
            }
        }
    }

    private boolean hasExactStacks() {
        Object2IntOpenHashMap<ItemStackKey> required = new Object2IntOpenHashMap<>(getContainerSize());
        for (int s = 0; s < getContainerSize(); s++) {
            ItemStack req = invBlueprint.getStackInSlot(s);
            if (!req.isEmpty()) {
                int count = req.getCount();
                if (count != 1) {
                    req = req.copy();
                    req.setCount(1);
                }
                ItemStackKey key = new ItemStackKey(req);
                // required.adjustOrPutValue(key, count, count);
                required.addTo(key, count);
            }
        }
//        return required.forEachEntry((stack, count) -> {
//            ArrayStackFilter filter = new ArrayStackFilter(stack.baseStack);
//            ItemStack inInventory = invMaterials.extract(filter, count, count, true);
//            return !inInventory.isEmpty() && inInventory.getCount() == count;
//        });
        return required.object2IntEntrySet().stream().allMatch(entry -> {
            ItemStackKey stack = entry.getKey();
            int count = entry.getIntValue();
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
        for (int s = 0; s < getContainerSize(); s++) {
            ItemStack bpt = invBlueprint.getStackInSlot(s);
            if (!bpt.isEmpty()) {
                ItemStack stack = invMaterials.extract(new ArrayStackFilter(bpt), 1, 1, false);
                if (stack.isEmpty()) {
                    clearInventory();
                    return false;
                }
                setItem(s, stack);
            }
        }

        // Step 3
        // Some recipes (for example vanilla fireworks) require calling
        // matches before calling getCraftingResult, as they store the
        // result of matches for getCraftingResult and getResult.
        if (!currentRecipe.matches(this, tile.getLevel())) {
            return false;
        }
//        ItemStack result = currentRecipe.getCraftingResult(this);
        ItemStack result = currentRecipe.getResultItem();
        if (result.isEmpty()) {
            // what?
            clearInventory();
            return false;
        }
        ItemStack leftover = invResult.insert(result, false, false);
        if (!leftover.isEmpty()) {
            InventoryUtil.addToBestAcceptor(tile.getLevel(), tile.getBlockPos(), null, leftover);
        }
        NonNullList<ItemStack> remainingStacks = currentRecipe.getRemainingItems(this);
        for (int s = 0; s < remainingStacks.size(); s++) {
            ItemStack inSlot = getItem(s);
            ItemStack remaining = remainingStacks.get(s);

            if (!inSlot.isEmpty()) {
                removeItem(s, 1);
                inSlot = getItem(s);
            }

            if (!remaining.isEmpty()) {
                if (inSlot.isEmpty()) {
                    setItem(s, remaining);
                }
//                else if (ItemStack.areItemsEqual(inSlot, remaining) && ItemStack.areItemStackTagsEqual(inSlot, remaining))
                else if (StackUtil.isSameItemSameDamageSameTag(inSlot, remaining)) {
                    remaining.grow(inSlot.getCount());
                    setItem(s, remaining);
                } else {
                    leftover = invMaterials.insert(remaining, false, false);
                    if (!leftover.isEmpty()) {
                        InventoryUtil.addToBestAcceptor(tile.getLevel(), tile.getBlockPos(), null, leftover);
                    }
                }
            }
        }

        // Step 4
        // Some ingredients really need to be removed (like empty buckets)
        for (int s = 0; s < getContainerSize(); s++) {
            ItemStack inSlot = super.removeItemNoUpdate(s);
            if (!inSlot.isEmpty()) {
                leftover = invMaterials.insert(inSlot, false, false);
                if (!leftover.isEmpty()) {
                    InventoryUtil.addToBestAcceptor(tile.getLevel(), tile.getBlockPos(), null, leftover);
                }
            }
        }
        return true;
    }

    /** @return True if this inventory is now clear, false otherwise. */
    private boolean clearInventory() {
        for (int s = 0; s < getContainerSize(); s++) {
            ItemStack inSlot = super.getItem(s);
            if (!inSlot.isEmpty()) {
                ItemStack leftover = invMaterials.insert(inSlot, false, false);
                removeItem(s, inSlot.getCount() - (leftover.isEmpty() ? 0 : leftover.getCount()));
                if (!leftover.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    static class ContainerNullEventHandler extends AbstractContainerMenu {
        protected ContainerNullEventHandler() {
            super(null, 0);
        }

        @Override
//        public boolean canInteractWith(Player playerIn)
        public boolean stillValid(Player playerIn) {
            return false;
        }

        @Override
//        public void onCraftMatrixChanged(IInventory inventoryIn)
        public void slotsChanged(Container inventoryIn) {
            // NO-OP
        }
    }
}
