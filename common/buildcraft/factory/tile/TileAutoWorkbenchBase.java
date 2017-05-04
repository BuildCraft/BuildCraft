/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.factory.tile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import buildcraft.api.tiles.TilesAPI;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.tiles.IHasWork;

import buildcraft.lib.delta.DeltaInt;
import buildcraft.lib.delta.DeltaManager.EnumNetworkVisibility;
import buildcraft.lib.misc.CraftingUtil;
import buildcraft.lib.misc.InventoryUtil;
import buildcraft.lib.misc.ItemStackKey;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraft.lib.tile.item.ItemHandlerSimple;

import gnu.trove.set.hash.TIntHashSet;

public abstract class TileAutoWorkbenchBase extends TileBC_Neptune implements ITickable, IDebuggable {
    protected WorkbenchCrafting crafting = createCrafting();
    public final ItemHandlerSimple invBlueprint;
    public final ItemHandlerSimple invMaterials;
    public final ItemHandlerSimple invResult;
    public final ItemHandlerSimple invOverflow;
    protected final Map<ItemStackKey, TIntHashSet> itemStackCache;

    public IRecipe currentRecipe;
    private int progress = 0;

    public final DeltaInt deltaProgress = deltaManager.addDelta("progress", EnumNetworkVisibility.GUI_ONLY);

    public TileAutoWorkbenchBase(int slots) {
        invBlueprint = itemManager.addInvHandler("blueprint", slots, EnumAccess.NONE);
        invMaterials = itemManager.addInvHandler("materials", slots, EnumAccess.INSERT, EnumPipePart.VALUES);
        invResult = itemManager.addInvHandler("result", 1, EnumAccess.EXTRACT, EnumPipePart.VALUES);
        invOverflow = itemManager.addInvHandler("overflow", slots, EnumAccess.EXTRACT, EnumPipePart.VALUES);
        itemStackCache = new HashMap<>();
    }

    protected abstract WorkbenchCrafting createCrafting();

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagCompound nbt = super.writeToNBT(compound);

        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
    }

    @Override
    public void onRemove() {
        super.onRemove();
        InventoryUtil.dropAll(getWorld(), getPos(), invMaterials);
        InventoryUtil.dropAll(getWorld(), getPos(), invResult);
        InventoryUtil.dropAll(getWorld(), getPos(), invOverflow);
    }

    @Override
    public void update() {
        deltaManager.tick();
        if (getWorld().isRemote) {
            return;
        }
        moveOverflowDown();
        // craft 1 item
        updateRecipe();
        if (hasMaterialsForRecipe()) {
            if (progress == 0) {
                deltaProgress.addDelta(0, 200, 1);
                deltaProgress.addDelta(200, 205, -1);
            }
            if (progress < 200) {
                progress++;
                return;
            }
            if (invOverflow.getStackInSlot(1).isEmpty()) {
                ItemStack out = crafting.craft();
                ItemStack leftOver = invResult.insertItem(0, out, false);
                InventoryUtil.drop(getWorld(), getPos(), leftOver);
                progress = 0;
            }
        } else if (progress != -1) {
            progress = -1;
            deltaProgress.setValue(0);
        }
    }

    private boolean hasMaterialsForRecipe() {
        if (currentRecipe == null) {
            return false;
        }
        crafting.enableBindings();
        boolean has = currentRecipe.matches(crafting, getWorld());
        crafting.disableBindings();
        return has;
    }

    private void moveOverflowDown() {
        // TODO!

        // TIntArrayList free = new TIntArrayList();
        // TObjectIntHashMap<ItemStackKey> used = new TObjectIntHashMap<>();
        // for (int i = 1; i < invResult.getSlots(); i++) {
        // ItemStack current = invResult.getStackInSlot(i);
        // if (current == null) {
        // free.add(i);
        // } else if (free.size() > 0) {
        // int t = free.get(0);
        // free.remove(0, 1);
        // free.add(i);
        // invResult.setStackInSlot(i, null);
        // invResult.setStackInSlot(t, current);
        // if (current.stackSize < current.getMaxStackSize()) {
        // used.put(new ItemStackKey(current), t);
        // }
        // } else if (used.containsKey(new ItemStackKey(current))) {
        //
        // }
        // }
    }

    @Override
    protected void onSlotChange(IItemHandlerModifiable handler, int slot, ItemStack before, ItemStack after) {
        super.onSlotChange(handler, slot, before, after);
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

            if (!after.isEmpty()) {
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
        this.currentRecipe = CraftingUtil.findMatchingRecipe(this.crafting, world);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("");
        left.add("Stack Cache:");
        for (Entry<ItemStackKey, TIntHashSet> entry : itemStackCache.entrySet()) {
            ItemStackKey key = entry.getKey();
            TIntHashSet set = entry.getValue();
            left.add("  " + key);
            left.add("  = " + Arrays.toString(set.toArray()));
        }
        left.add("Current Recipe = " + currentRecipe);
    }

    public ItemStack getOutput() {
        return currentRecipe.getCraftingResult(crafting);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == TilesAPI.CAP_HAS_WORK) {
            return (T) (IHasWork) () -> progress >= 0;
        }
        return super.getCapability(capability, facing);
    }

    protected abstract class WorkbenchCrafting extends InventoryCrafting {
        protected final CraftingSlot[] craftingSlots;

        public WorkbenchCrafting(int width, int height) {
            super(null, width, height);
            this.craftingSlots = new CraftingSlot[width * height];
        }

        @Nonnull
        public ItemStack craft() {
            enableBindings();
            ItemStack out = StackUtil.asNonNull(currentRecipe.getCraftingResult(crafting));
            if (! out.isEmpty()) {
                NonNullList<ItemStack> leftOvers = currentRecipe.getRemainingItems(crafting);
                for (int i = 0; i < leftOvers.size(); i++) {
                    CraftingSlot slot = craftingSlots[i];
                    ItemStack before = slot.get();
                    ItemStack leftOver = leftOvers.get(i);
                    if (leftOver.isEmpty()) {
                        if (!before.isEmpty()) {
                            slot.use(1);
                        }
                    } else {
                        leftOver = slot.useAndAdd(leftOver);
                        if (leftOver != null) {
                            invOverflow.insert(leftOver, false, false);
                        }
                    }
                }
            }
            disableBindings();
            return out;
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
            throw new IllegalStateException("Not allowed to directly set the materials!");
        }

        @Override
        public ItemStack removeStackFromSlot(int index) {
            throw new IllegalStateException("Not allowed to directly set the materials!");
        }

        @Override
        public void setInventorySlotContents(int index, ItemStack stack) {
            throw new IllegalStateException("Not allowed to directly set the materials!");
        }
    }

    protected abstract class CraftingSlot {
        protected final int slot;

        public CraftingSlot(int slot) {
            this.slot = slot;
        }

        public ItemStack useAndAdd(@Nonnull ItemStack leftOver) {
            ItemStack current = get();
            if (StackUtil.canMerge(current, leftOver)) {
                if (leftOver.getCount() == 1) {
                    return null;
                }
            }
            if (!current.isEmpty()) {
                use(1);
            }
            return leftOver;
        }

        @Nonnull
        public abstract ItemStack get();

        /** Removes up to the specified count from the inventory */
        public abstract void use(int count);

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
        public void use(int count) {
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
        protected TIntHashSet boundTo = null;

        public CraftSlotItemBound(CraftSlotItem from) {
            super(from.slot);
            nonBound = from;
        }

        protected void rebind() {
            ItemStack wanted = nonBound.get();
            ItemStackKey key = new ItemStackKey(wanted);
            boundTo = itemStackCache.get(key);
        }

        @Override
        public ItemStack get() {
            if (boundTo == null) {
                return StackUtil.EMPTY;
            }
            for (int s : boundTo.toArray()) {
                if (!invMaterials.getStackInSlot(s).isEmpty()) {
                    ItemStack inSlot = invMaterials.extractItem(s, 1, true);
                    if (!inSlot.isEmpty()) {
                        return inSlot;
                    }
                }
            }
            return StackUtil.EMPTY;
        }

        @Override
        public void use(int count) {
            if (boundTo == null) {
                // sigh
                throw new IllegalStateException("Attempted to use an item from a slot with nothing in it!");
            } else {
                for (int s : boundTo.toArray()) {
                    if (!invMaterials.getStackInSlot(s).isEmpty()) {
                        ItemStack extracted = invMaterials.extractItem(s, 1, false);
                        if (!extracted.isEmpty()) {
                            return;
                        }
                    }
                }
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
