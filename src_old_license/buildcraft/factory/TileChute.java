/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.factory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;

import buildcraft.api.power.IRedstoneEngineReceiver;
import buildcraft.api.transport.IInjectable;
import buildcraft.api.transport.IPipeTile;
import buildcraft.core.lib.RFBattery;
import buildcraft.core.lib.block.TileBuildCraft;
import buildcraft.core.lib.inventory.ITransactor;
import buildcraft.core.lib.inventory.SimpleInventory;
import buildcraft.core.lib.inventory.Transactor;

public class TileChute extends TileBuildCraft implements IInventory, IRedstoneEngineReceiver {

    private final SimpleInventory inventory = new SimpleInventory(4, "Chute", 64);
    private boolean isEmpty;

    @Override
    public void initialize() {
        this.setBattery(new RFBattery(10, 10, 0));
        inventory.addListener(this);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        super.readFromNBT(nbtTagCompound);

        NBTTagCompound p = nbtTagCompound;

        if (nbtTagCompound.hasKey("inventory")) {
            // to support pre 6.0 loading
            p = nbtTagCompound.getCompoundTag("inventory");
        }

        inventory.readFromNBT(p);
        inventory.markDirty();
    }

    @Override
    public void writeToNBT(NBTTagCompound nbtTagCompound) {
        super.writeToNBT(nbtTagCompound);

        inventory.writeToNBT(nbtTagCompound);
    }

    @Override
    public void update() {
        super.update();
        if (worldObj.isRemote || isEmpty || worldObj.getTotalWorldTime() % 2 != 0) {
            return;
        }

        TileEntity outputTile = getTile(EnumFacing.DOWN);

        ITransactor transactor = Transactor.getTransactorFor(outputTile, EnumFacing.UP);

        if (transactor == null) {
            if (outputTile instanceof IInjectable && getBattery().getEnergyStored() >= 10) {
                ItemStack stackToOutput = null;
                int internalSlot = 0;

                getBattery().useEnergy(10, 10, false);

                for (; internalSlot < inventory.getSizeInventory(); internalSlot++) {
                    ItemStack stackInSlot = inventory.getStackInSlot(internalSlot);
                    if (stackInSlot == null || stackInSlot.stackSize == 0) {
                        continue;
                    }
                    stackToOutput = stackInSlot.copy();
                    stackToOutput.stackSize = 1;
                    break;
                }

                if (stackToOutput != null) {
                    int used = ((IInjectable) outputTile).injectItem(stackToOutput, true, EnumFacing.UP, null);
                    if (used > 0) {
                        decrStackSize(internalSlot, 1);
                    }
                }
            }

            return;
        }

        for (int internalSlot = 0; internalSlot < inventory.getSizeInventory(); internalSlot++) {
            ItemStack stackInSlot = inventory.getStackInSlot(internalSlot);
            if (stackInSlot == null || stackInSlot.stackSize == 0) {
                continue;
            }

            ItemStack clonedStack = stackInSlot.copy().splitStack(1);
            if (transactor.add(clonedStack, true).stackSize > 0) {
                inventory.decrStackSize(internalSlot, 1);
                return;
            }
        }
    }

    @Override
    public void markDirty() {
        isEmpty = true;

        for (int internalSlot = 0; internalSlot < inventory.getSizeInventory(); internalSlot++) {
            ItemStack stackInSlot = inventory.getStackInSlot(internalSlot);
            if (stackInSlot != null && stackInSlot.stackSize > 0) {
                isEmpty = false;
                return;
            }
        }
    }

    /** IInventory Implementation * */
    @Override
    public int getSizeInventory() {
        return inventory.getSizeInventory();
    }

    @Override
    public ItemStack getStackInSlot(int slotId) {
        return inventory.getStackInSlot(slotId);
    }

    @Override
    public ItemStack decrStackSize(int slotId, int count) {
        ItemStack output = inventory.decrStackSize(slotId, count);
        return output;
    }

    @Override
    public ItemStack removeStackFromSlot(int slotId) {
        return inventory.removeStackFromSlot(slotId);
    }

    @Override
    public void setInventorySlotContents(int slotId, ItemStack itemStack) {
        inventory.setInventorySlotContents(slotId, itemStack);
    }

    @Override
    public String getInventoryName() {
        return inventory.getName();
    }

    @Override
    public int getInventoryStackLimit() {
        return inventory.getInventoryStackLimit();
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer entityPlayer) {
        return worldObj.getTileEntity(pos) == this && entityPlayer.getDistanceSq(pos) <= 64.0D;
    }

    @Override
    public void openInventory(EntityPlayer player) {}

    @Override
    public void closeInventory(EntityPlayer player) {}

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack) {
        return true;
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public boolean canConnectRedstoneEngine(EnumFacing side) {
        return side.getAxis() != Axis.Y;
    }

    @Override
    public String getOwner() {
        return super.getOwner();
    }

    @Override
    public boolean canConnectEnergy(EnumFacing side) {
        return canConnectRedstoneEngine(side) && !(getTile(side) instanceof IPipeTile);
    }
}
