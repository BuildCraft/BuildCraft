/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.tile.craft;

import javax.annotation.Nullable;

import ct.buildcraft.lib.inventory.filter.ArrayStackFilter;
import ct.buildcraft.lib.misc.InventoryUtil;
import ct.buildcraft.lib.misc.ItemStackKey;
import ct.buildcraft.lib.tile.TileBC_Neptune;
import ct.buildcraft.lib.tile.item.ItemHandlerSimple;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;

public class WorkbenchCrafting extends CraftingContainer {
    enum EnumRecipeType {
        INGREDIENTS,
        EXACT_STACKS;
    }
    
    public static final AbstractContainerMenu CONTAINER_EVENT_HANDLER = new AbstractContainerMenu(null, -1) {
		@Override
		public ItemStack quickMoveStack(Player p_38941_, int p_38942_) {
			return ItemStack.EMPTY;
		}
		@Override
		public boolean stillValid(Player p_38874_) {
			return false;
		}
    	
    };

    private final BlockEntity tile;
    private final ItemHandlerSimple invBlueprint;
    private final ItemHandlerSimple invMaterials;
    private final ItemHandlerSimple invResult;
    private final ItemHandlerSimple invAssumedResult = new ItemHandlerSimple(1);
    private boolean isBlueprintDirty = true;
    private boolean areMaterialsDirty = true;
    private boolean cachedHasRequirements = false;
    
    private final int BPsize;
    private final int MTsize;
    private final int REsize;
    private final int craftTableSize;
    private final int width;
    private final int height;

    @Nullable
    private CraftingRecipe currentRecipe;
    private ItemStack assumedResult = ItemStack.EMPTY;
//    protected final RecipeBookMenu<WorkbenchCrafting> menu = new InnerRecipeBookMenu();

    private EnumRecipeType recipeType = null;

    public WorkbenchCrafting(int width, int height, TileBC_Neptune tile, ItemHandlerSimple invBlueprint,
        ItemHandlerSimple invMaterials, ItemHandlerSimple invResult) {
        super(CONTAINER_EVENT_HANDLER, width, height);
        this.tile = tile;
        this.invBlueprint = invBlueprint;
        this.BPsize = invBlueprint.getSlots();
        this.MTsize = invMaterials.getSlots();
        this.REsize = invResult.getSlots();
        this.craftTableSize = width * height;//super.getContainerSize();
        if (invBlueprint.getSlots() < craftTableSize) {
            throw new IllegalArgumentException("Passed blueprint has a smaller size than width * height! ( expected "
                + getContainerSize() + ", got " + invBlueprint.getSlots() + ")");
        }
        this.invMaterials = invMaterials;
        this.invResult = invResult;
		this.width = width;
		this.height = height;
        
    }

    
    @Override
	public ItemStack getItem(int index) {
    	if(index < craftTableSize) 
    		return (isBlueprintDirty ? invBlueprint.getStackInSlot(index) : super.getItem(index));
    	int[] result = new int[1];
    	ItemHandlerSimple inv = pickInv(index, result);
    	if(inv != null) 
    		return inv.getStackInSlot(result[0]);
    	if(index == craftTableSize + BPsize + MTsize + REsize)
    		return invAssumedResult.getStackInSlot(0);
    	return ItemStack.EMPTY;
	}
    

    @Override
	public void setItem(int index, ItemStack item) {
    	if(index < craftTableSize) {
    		super.setItem(index, item);
    		return;
    	}
    	int[] result = new int[1];
    	ItemHandlerSimple inv = pickInv(index, result);
    	if(inv != null) 
    		inv.setStackInSlot(result[0], item);
	}
    
    private ItemHandlerSimple pickInv(int index, int[] result) {
    	 if(index < craftTableSize + BPsize) {
    		 result[0] = (index-craftTableSize);
    		 return invBlueprint;
    	 }
    	 if(index < craftTableSize + BPsize + MTsize) {
    		 result[0] = (index-craftTableSize-BPsize);
    		 return invMaterials;
    	 }
    	 if(index < craftTableSize + BPsize + MTsize + REsize){
    		 result[0] = (index-craftTableSize-BPsize-MTsize);
    		 return invResult;
    	 }
    	 return null;
    }

	@Override
	public int getContainerSize() {
		return craftTableSize;

	}
	
	public int getSlotSize() {
		return BPsize + MTsize + REsize;
	}

	@Override
	public ItemStack removeItemNoUpdate(int index) {
    	if(index < craftTableSize) {
    		return super.removeItemNoUpdate(index);
    	}
    	int[] result = new int[1];
    	ItemHandlerSimple inv = pickInv(index, result);
    	if(inv != null) 
    		return ContainerHelper.takeItem(inv.stacks, result[0]);
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItem(int index, int num) {
    	if(index < craftTableSize) {
    		return super.removeItem(index, num);
    	}
    	int[] result = new int[1];
    	ItemHandlerSimple inv = pickInv(index, result);
    	if(inv != null) 
    		return ContainerHelper.removeItem(inv.stacks, result[0], num);
		return ItemStack.EMPTY;
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
    	Level world = tile.getLevel();
    	if (world.isClientSide) {
            throw new IllegalStateException("Never call this on the client side!");
        }
        if (isBlueprintDirty) {
            currentRecipe = world.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, this, world).orElse(null);
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
            invAssumedResult.setStackInSlot(0, assumedResult);
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
        Object2IntMap<ItemStackKey> required = new Object2IntArrayMap<>(craftTableSize);//TODO choose the faster one
        for (int s = 0; s < craftTableSize; s++) {
            ItemStack req = invBlueprint.getStackInSlot(s);
            if (!req.isEmpty()) {
                int count = req.getCount();
                if (count != 1) {
                    req = req.copy();
                    req.setCount(1);
                }
                ItemStackKey key = new ItemStackKey(req);
                required.merge(key, count, (a, b) -> a+b);
            }
        }
        final boolean[] flag = {false};
        required.forEach((stack, count) -> {
            ArrayStackFilter filter = new ArrayStackFilter(stack.baseStack);
            ItemStack inInventory = invMaterials.extract(filter, count, count, true);
            flag[0] |= !inInventory.isEmpty() && inInventory.getCount() == count;
        });
        return flag[0];
    }

    /** Implementation of {@link #craft()}, assuming nothing about the current recipe. */
    private boolean craftExact() {
        // 4 steps:
        // - Move everything out of this inventory (Just to check: state correction operation)
        // - Attempt to move every exact item from invMaterials to this inventory
        // - Call normal crafting stuffs
        // - Move everything from the inventory back to materials
    	Level world = tile.getLevel();
    	BlockPos pos = tile.getBlockPos();
        // Step 1
        clearInventory();

        // Step 2
        for (int s = 0; s < craftTableSize; s++) {
            ItemStack bpt = invBlueprint.getStackInSlot(s);
            if (!bpt.isEmpty()) {
                ItemStack stack = invMaterials.extract(new ArrayStackFilter(bpt), 1, 1, false);
                if (stack.isEmpty()) {
                    clearInventory();
                    return false;
                }
                super.setItem(s, stack);
            }
        }

        // Step 3
        // Some recipes (for example vanilla fireworks) require calling
        // matches before calling getCraftingResult, as they store the
        // result of matches for getCraftingResult and getResult.
        if (!currentRecipe.matches(this, world)) {
            return false;
        }
        ItemStack result = currentRecipe.getResultItem();
        if (result.isEmpty()) {
            // what?
            clearInventory();
            return false;
        }
        ItemStack leftover = invResult.insert(result, false, false);
        if (!leftover.isEmpty()) {
            InventoryUtil.addToBestAcceptor(world, pos, null, leftover);
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
//                	super.setItem(s, remaining);
                } else if (ItemStack.isSame(inSlot, remaining)
                    && ItemStack.isSameItemSameTags(inSlot, remaining)) {
                    remaining.grow(inSlot.getCount());
//                    super.setItem(s, remaining);
                } else {
                    leftover = invMaterials.insert(remaining, false, false);
                    if (!leftover.isEmpty()) {
                        InventoryUtil.addToBestAcceptor(world, pos, null, leftover);
                    }
                }
            }
        }

        // Step 4
        // Some ingredients really need to be removed (like empty buckets)
        for (int s = 0; s < craftTableSize; s++) {
            ItemStack inSlot = super.removeItemNoUpdate(s);
            if (!inSlot.isEmpty()) {
                leftover = invMaterials.insert(inSlot, false, false);
                if (!leftover.isEmpty()) {
                    InventoryUtil.addToBestAcceptor(world, pos, null, leftover);
                }
            }
        }
        return true;
    }

    /** @return True if this inventory is now clear, false otherwise. */
    private boolean clearInventory() {
        for (int s = 0; s < craftTableSize; s++) {
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
    
    public RecipeBookMenu<WorkbenchCrafting> getCraftingMenu(AbstractContainerMenu menu) {
    	return new InnerRecipeBookMenu(menu);
    }

    protected class InnerRecipeBookMenu extends RecipeBookMenu<WorkbenchCrafting> {
    	public final AbstractContainerMenu menu;
        protected InnerRecipeBookMenu(AbstractContainerMenu menu) {
			super(menu.getType(), menu.containerId);
			this.menu = menu;
			for(int i=0;i<menu.slots.size();i++)
				this.slots.add(i, menu.slots.get(i));
		}
        
		@Override
		public ItemStack quickMoveStack(Player p_38941_, int p_38942_) {
			return ItemStack.EMPTY;
		}

		@Override
		public boolean stillValid(Player p_38874_) {
			return menu.stillValid(p_38874_);
		}

		@Override
		public void fillCraftSlotsStackedContents(StackedContents stackedContents) {
			WorkbenchCrafting.this.fillStackedContents(stackedContents);
		}

		@Override
		public void clearCraftingContent() {
			WorkbenchCrafting.this.clearContent();
		}

		@Override
		public boolean recipeMatches(Recipe<? super WorkbenchCrafting> p_40118_) {
			return p_40118_.matches(WorkbenchCrafting.this, WorkbenchCrafting.this.tile.getLevel());
		}

		@Override
		public int getResultSlotIndex() {
			return 0;
		}

		@Override
		public int getGridWidth() {
			return WorkbenchCrafting.this.getWidth();
		}

		@Override
		public int getGridHeight() {
			return WorkbenchCrafting.this.getHeight();
		}

		@Override
		public int getSize() {
			return WorkbenchCrafting.this.getWidth() * WorkbenchCrafting.this.getHeight() + 1;
		}

		@Override
		public RecipeBookType getRecipeBookType() {
			return RecipeBookType.CRAFTING;
		}

		@Override
		public boolean shouldMoveToInventory(int p_150635_) {
			return false;
		}

		@Override
		public Slot getSlot(int p_38854_) {
			return menu.getSlot(p_38854_);
		}
		
		
    }
}
