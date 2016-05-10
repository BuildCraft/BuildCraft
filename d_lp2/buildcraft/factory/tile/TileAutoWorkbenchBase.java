package buildcraft.factory.tile;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;

import net.minecraftforge.items.IItemHandlerModifiable;

import buildcraft.api.core.EnumPipePart;
import buildcraft.lib.delta.DeltaInt;
import buildcraft.lib.tile.TileBCInventory_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;

public abstract class TileAutoWorkbenchBase extends TileBCInventory_Neptune implements ITickable {
    protected WorkbenchCrafting crafting = createCrafting();
    protected final IItemHandlerModifiable invBlueprint;
    protected final IItemHandlerModifiable invMaterials;
    protected final IItemHandlerModifiable invResult;

    protected IRecipe currentRecipe;

    public final DeltaInt deltaProgress = deltaManager.addDelta();

    public TileAutoWorkbenchBase(int slots) {
        invBlueprint = addInventory("blueprint", slots, EnumAccess.NONE);
        invMaterials = addInventory("materials", slots, EnumAccess.INSERT, EnumPipePart.VALUES);
        invResult = addInventory("result", 1, EnumAccess.EXTRACT, EnumPipePart.VALUES);
    }

    protected abstract WorkbenchCrafting createCrafting();

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
    }

    @Override
    public void update() {
        deltaManager.tick();
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
            ItemStack current = invMaterials.getStackInSlot(slot);
            if (current == null) return null;
            ItemStack split = current.splitStack(count);
            invMaterials.setStackInSlot(slot, current);
            return split;
        }
    }
}
