/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.factory.tile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.core.lib.utils.CraftingUtils;
import buildcraft.lib.delta.DeltaInt;
import buildcraft.lib.delta.DeltaManager.EnumNetworkVisibility;
import buildcraft.lib.misc.ItemStackKey;
import buildcraft.lib.tile.TileBCInventory_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;

import gnu.trove.set.hash.TIntHashSet;

public abstract class TileAutoWorkbenchBase extends TileBCInventory_Neptune implements ITickable, IDebuggable {
    protected WorkbenchCrafting crafting = createCrafting();
    public final IItemHandlerModifiable invBlueprint;
    public final IItemHandlerModifiable invMaterials;
    public final IItemHandlerModifiable invResult;
    protected final Map<ItemStackKey, TIntHashSet> itemStackCache;

    public IRecipe currentRecipe;

    public final DeltaInt deltaProgress = deltaManager.addDelta("progress", EnumNetworkVisibility.GUI_ONLY);

    public TileAutoWorkbenchBase(int slots) {
        invBlueprint = addInventory("blueprint", slots, EnumAccess.NONE);
        invMaterials = addInventory("materials", slots, EnumAccess.INSERT, EnumPipePart.VALUES);
        invResult = addInventory("result", 1, EnumAccess.EXTRACT, EnumPipePart.VALUES);
        itemStackCache = new HashMap<>();
    }

    protected abstract WorkbenchCrafting createCrafting();

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
    }

    protected void enableBindings() {
        crafting.enableBindings();
    }

    protected void disableBindings() {
        crafting.disableBindings();
    }

    @Override
    public void update() {
        deltaManager.tick();
    }

    @Override
    protected void onSlotChange(IItemHandlerModifiable handler, int slot, ItemStack before, ItemStack after) {
        super.onSlotChange(handler, slot, before, after);
        String h = handler == invBlueprint ? "bpt" : (handler == invMaterials ? "mat" : (handler == invResult) ? "res" : "?");
        BCLog.logger.info("onSlotChange ( " + h + ", " + slot + ", " + before + ", " + after + " )");
        if (handler == invMaterials) {
            ItemStackKey keyBefore = new ItemStackKey(before);
            ItemStackKey keyAfter = new ItemStackKey(after);
            if (keyAfter.equals(keyBefore)) return;
            if (itemStackCache.containsKey(keyBefore)) {
                TIntHashSet set = itemStackCache.get(keyBefore);
                set.remove(slot);
                if (set.size() == 0) {
                    itemStackCache.remove(keyBefore);
                }
            }

            if (after != null) {
                if (!itemStackCache.containsKey(keyAfter)) {
                    // Use a different _no_entry_value as 0 is a valid slot
                    itemStackCache.put(keyAfter, new TIntHashSet(10, 0.5f, -1));
                }
                TIntHashSet set = itemStackCache.get(keyAfter);
                set.add(slot);
            }
        } else if (handler == invBlueprint) {
            this.updateRecipe();
        }
    }

    public void updateRecipe() {
        this.currentRecipe = CraftingUtils.findMatchingRecipe(this.crafting, worldObj);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        for (Entry<ItemStackKey, TIntHashSet> entry : itemStackCache.entrySet()) {
            ItemStackKey key = entry.getKey();
            TIntHashSet set = entry.getValue();
            left.add("  " + key);
            left.add("  = " + Arrays.toString(set.toArray()));
        }
    }

    public ItemStack getOutput() {
        return currentRecipe.getCraftingResult(crafting);
    }

    protected abstract class WorkbenchCrafting extends InventoryCrafting {
        protected final CraftingSlot[] craftingSlots;

        public WorkbenchCrafting(int width, int height) {
            super(null, width, height);
            this.craftingSlots = new CraftingSlot[width * height];
        }

        public void enableBindings() {
            for (int i = 0; i < craftingSlots.length; i++) {
                craftingSlots[i] = craftingSlots[i].getBoundVersion();
            }
        }

        public void disableBindings() {
            for (int i = 0; i < craftingSlots.length; i++) {
                craftingSlots[i] = craftingSlots[i].getUnboundVersion();
            }
        }

        @Override
        public ItemStack getStackInSlot(int index) {
            CraftingSlot slot = craftingSlots[index];
            return slot.get();
        }

        @Override
        public ItemStack decrStackSize(int index, int count) {
            CraftingSlot slot = craftingSlots[index];
            return slot.use(count);
        }

        @Override
        public ItemStack removeStackFromSlot(int index) {
            return decrStackSize(index, Integer.MAX_VALUE);
        }

        @Override
        public void setInventorySlotContents(int index, ItemStack stack) {
            if (stack == null) {
                removeStackFromSlot(index);
            } else {
                CraftingSlot slot = craftingSlots[index];
                slot.set(stack);
            }
        }
    }

    protected abstract class CraftingSlot {
        protected final int slot;

        public CraftingSlot(int slot) {
            this.slot = slot;
        }

        public abstract void set(ItemStack stack);

        public abstract ItemStack get();

        /** Removes up to the specified count from the inventory */
        public abstract ItemStack use(int count);

        public abstract CraftingSlot getBoundVersion();

        public abstract CraftingSlot getUnboundVersion();
    }

    protected class CraftSlotItem extends CraftingSlot {
        private final CraftSlotItemBound bound = new CraftSlotItemBound(this);

        public CraftSlotItem(int slot) {
            super(slot);
        }

        @Override
        public ItemStack get() {
            return invBlueprint.getStackInSlot(slot);
        }

        @Override
        public void set(ItemStack stack) {
            // Shouldn't really be called, this is the UNBOUND version
            throw new IllegalStateException("Tried to set the UNBOUND slot!");
        }

        @Override
        public ItemStack use(int count) {
            // Shouldn't really be called, this is the UNBOUND version
            throw new IllegalStateException("Tried to use from the UNBOUND slot!");
        }

        @Override
        public CraftingSlot getBoundVersion() {
            bound.rebind();
            return bound;
        }

        @Override
        public CraftingSlot getUnboundVersion() {
            return this;
        }
    }

    protected class CraftSlotItemBound extends CraftingSlot {
        protected final CraftSlotItem nonBound;
        protected int bound = -1;

        public CraftSlotItemBound(CraftSlotItem from) {
            super(from.slot);
            nonBound = from;
        }

        protected void rebind() {

        }

        @Override
        public void set(ItemStack stack) {
            if (bound != -1) {
                invMaterials.setStackInSlot(bound, stack);
            }
        }

        @Override
        public ItemStack get() {
            if (bound != -1) {
                return invMaterials.getStackInSlot(bound);
            } else {
                return null;
            }
        }

        @Override
        public ItemStack use(int count) {
            if (bound != -1) {
                return invMaterials.extractItem(bound, count, false);
            } else {
                return null;
            }
        }

        @Override
        public CraftingSlot getBoundVersion() {
            rebind();
            return this;
        }

        @Override
        public CraftingSlot getUnboundVersion() {
            return nonBound;
        }
    }
}
