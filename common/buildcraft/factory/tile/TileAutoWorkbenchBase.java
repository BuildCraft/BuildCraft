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

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.lib.delta.DeltaInt;
import buildcraft.lib.delta.DeltaManager.EnumNetworkVisibility;
import buildcraft.lib.misc.ItemStackKey;
import buildcraft.lib.tile.TileBCInventory_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;

import gnu.trove.set.hash.TIntHashSet;

public abstract class TileAutoWorkbenchBase extends TileBCInventory_Neptune implements ITickable, IDebuggable {
    protected WorkbenchCrafting crafting = createCrafting();
    protected final IItemHandlerModifiable invBlueprint;
    protected final IItemHandlerModifiable invMaterials;
    protected final IItemHandlerModifiable invResult;
    protected final Map<ItemStackKey, TIntHashSet> itemStackCache;

    protected IRecipe currentRecipe;

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

    @Override
    public void update() {
        deltaManager.tick();
    }

    @Override
    protected void onSlotChange(IItemHandlerModifiable handler, int slot, ItemStack before, ItemStack after) {
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
        }
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

    protected class WorkbenchCrafting extends InventoryCrafting {
        protected final CraftingSlot[] craftingSlots;

        public WorkbenchCrafting(int width, int height) {
            super(null, width, height);
            this.craftingSlots = new CraftingSlot[width * height];
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
    }

    protected class CraftingSlotItem extends CraftingSlot {
        public CraftingSlotItem(int slot) {
            super(slot);
        }

        @Override
        public ItemStack get() {
            return invBlueprint.getStackInSlot(slot);
        }

        @Override
        public void set(ItemStack stack) {
            throw new IllegalStateException("Not yet implemented!");
        }

        @Override
        public ItemStack use(int count) {
            ItemStack target = get();
            if (target == null) {
                // Why was this even called? We already know there's nothing here...
                return null;
            }
            ItemStackKey targetKey = new ItemStackKey(target);
            TIntHashSet set = itemStackCache.get(targetKey);
            if (set == null) {
                // No items found :(
                return null;
            }
            int slotToUse = set.iterator().next();
            ItemStack current = invMaterials.getStackInSlot(slotToUse);
            if (current == null) {
                // Something bad happened to the caching stuffs.
                return null;
            }
            ItemStack split = current.splitStack(count);
            invMaterials.setStackInSlot(slotToUse, current);
            return split;
        }
    }
}
