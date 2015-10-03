package buildcraft.silicon.tile;

import java.lang.ref.WeakReference;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import buildcraft.api.tiles.IHasWork;
import buildcraft.core.lib.gui.ContainerDummy;
import buildcraft.core.lib.inventory.InvUtils;
import buildcraft.core.lib.inventory.StackHelper;
import buildcraft.core.lib.utils.CraftingUtils;
import buildcraft.core.lib.utils.NBTUtils;
import buildcraft.core.lib.utils.StringUtils;
import buildcraft.core.lib.utils.Utils;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.silicon.item.ItemPackage;

public class TileStampingTable extends TileLaserTableBase implements IHasWork, ISidedInventory {
    private class LocalInventoryCrafting extends InventoryCrafting {
        public LocalInventoryCrafting() {
            super(new ContainerDummy(), 3, 3);
        }

        private IRecipe findRecipe() {
            return CraftingUtils.findMatchingRecipe(this, worldObj);
        }
    }

    private static final int[] SLOTS = Utils.createSlotArray(0, 5);
    private SlotCrafting craftSlot;
    private final LocalInventoryCrafting crafting = new LocalInventoryCrafting();

    @Override
    public boolean canUpdate() {
        return !FMLCommonHandler.instance().getEffectiveSide().isClient();
    }

    public WeakReference<EntityPlayer> getInternalPlayer() {
        return CoreProxy.proxy.getBuildCraftPlayer((WorldServer) worldObj, xCoord, yCoord + 1, zCoord);
    }

    @Override
    public void updateEntity() {
        super.updateEntity();

        if (getEnergy() >= getRequiredEnergy()) {
            ItemStack input = this.getStackInSlot(0);

            if (input == null) {
                return;
            }

            if (craftSlot == null) {
                craftSlot = new SlotCrafting(getInternalPlayer().get(), crafting, this, 1, 0, 0);
            }

            if (input.getItem() instanceof ItemPackage) {
                // Try a recipe made out of the package's contents
                NBTTagCompound tag = NBTUtils.getItemData(input);
                for (int i = 0; i < 9; i++) {
                    if (tag.hasKey("item" + i)) {
                        crafting.setInventorySlotContents(i, ItemStack.loadItemStackFromNBT(tag.getCompoundTag("item" + i)));
                    } else {
                        crafting.setInventorySlotContents(i, null);
                    }
                }
            } else {
                // Try a shapeless recipe made from just that item
                ItemStack input2 = input.copy();
                input2.stackSize = 1;
                crafting.setInventorySlotContents(0, input2);
                for (int i = 1; i < 9; i++) {
                    crafting.setInventorySlotContents(i, null);
                }
            }

            IRecipe recipe = crafting.findRecipe();
            ItemStack result = recipe != null ? recipe.getCraftingResult(crafting).copy() : null;
            ItemStack resultInto = this.getStackInSlot(1);

            if (recipe == null || result == null || result.stackSize <= 0) {
                if (resultInto == null || StackHelper.canStacksMerge(input, resultInto)) {
                    this.setInventorySlotContents(0, null);
                    this.setInventorySlotContents(1, input);
                }
                return;
            } else if (resultInto != null && (!StackHelper.canStacksMerge(result, resultInto) || resultInto.stackSize + result.stackSize > result
                    .getMaxStackSize())) {
                return;
            }

            addEnergy(-getRequiredEnergy());

            craftSlot.onPickupFromSlot(getInternalPlayer().get(), result);

            IInventory playerInv = getInternalPlayer().get().inventory;

            for (int i = 0; i < playerInv.getSizeInventory(); i++) {
                if (playerInv.getStackInSlot(i) != null) {
                    ItemStack output = playerInv.getStackInSlot(i);

                    for (int j = 2; j < 5; j++) {
                        ItemStack target = getStackInSlot(j);

                        if (target == null) {
                            setInventorySlotContents(j, output);
                            output.stackSize = 0;
                            break;
                        } else {
                            output.stackSize -= StackHelper.mergeStacks(output, input, true);
                            if (output.stackSize == 0) {
                                break;
                            }
                        }
                    }

                    if (output.stackSize > 0) {
                        output.stackSize -= Utils.addToRandomInventoryAround(worldObj, xCoord, yCoord, zCoord, output);
                    }

                    if (output.stackSize > 0) {
                        InvUtils.dropItems(worldObj, output, xCoord, yCoord + 1, zCoord);
                    }

                    playerInv.setInventorySlotContents(i, null);
                }
            }

            if (resultInto == null) {
                setInventorySlotContents(1, result);
            } else {
                resultInto.stackSize += result.stackSize;
            }

            decrStackSize(0, 1);
        }
    }

    @Override
    public int getRequiredEnergy() {
        ItemStack stack = this.getStackInSlot(0);
        ItemStack output = this.getStackInSlot(1);
        if (output != null && output.stackSize == output.getMaxStackSize()) {
            return 0;
        }
        if (stack != null && stack.getItem() != null) {
            if (stack.getItem() instanceof ItemPackage) {
                // tagMap size
                return 400 * NBTUtils.getItemData(stack).getKeySet().size();
            } else {
                return 400;
            }
        }

        return 0;
    }

    @Override
    public boolean hasWork() {
        return getRequiredEnergy() > 0;
    }

    @Override
    public boolean canCraft() {
        return hasWork();
    }

    @Override
    public int getSizeInventory() {
        return 5;
    }

    @Override
    public String getInventoryName() {
        return StringUtils.localize("tile.stampingTableBlock.name");
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return slot == 0;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        return SLOTS;
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack stack, int side) {
        return slot == 0;
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int side) {
        return slot >= 1;
    }
}
