/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.silicon;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.power.ILaserTarget;
import buildcraft.api.tiles.IControllable;
import buildcraft.api.tiles.IHasWork;
import buildcraft.core.lib.block.TileBuildCraft;
import buildcraft.core.lib.inventory.SimpleInventory;
import buildcraft.core.lib.inventory.StackHelper;
import buildcraft.core.lib.utils.AverageInt;
import buildcraft.core.lib.utils.Utils;

public abstract class TileLaserTableBase extends TileBuildCraft implements ILaserTarget, IInventory, IHasWork {
    public int clientRequiredEnergy = 0;
    protected SimpleInventory inv = new SimpleInventory(getSizeInventory(), "inv", 64);
    private int energy = 0;
    private int recentEnergyAverage;
    private AverageInt recentEnergyAverageUtil = new AverageInt(20);

    public TileLaserTableBase() {
        setControlMode(IControllable.Mode.On);
    }

    @Override
    public void update() {
        super.update();
        recentEnergyAverageUtil.tick();
    }

    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public void addEnergy(int energy) {
        this.energy += energy;
    }

    public void subtractEnergy(int energy) {
        this.energy -= energy;
    }

    public abstract int getRequiredEnergy();

    public int getProgressScaled(int ratio) {
        if (clientRequiredEnergy == 0) {
            return 0;
        } else if (energy >= clientRequiredEnergy) {
            return ratio;
        } else {
            return (int) ((double) energy / (double) clientRequiredEnergy * ratio);
        }
    }

    public int getRecentEnergyAverage() {
        return recentEnergyAverage;
    }

    public abstract boolean canCraft();

    /* @Override
    public boolean acceptsControlMode(Mode mode) {
        return mode == Mode.On || mode == Mode.Off;
    } */

    @Override
    public boolean requiresLaserEnergy() {
        return canCraft() && energy < getRequiredEnergy() * 5F /* && mode == Mode.On */;
    }

    @Override
    public void receiveLaserEnergy(int energy) {
        this.energy += energy;
        recentEnergyAverageUtil.push(energy);
    }

    @Override
    public boolean isInvalidTarget() {
        return isInvalid();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return inv.getStackInSlot(slot);
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        return inv.decrStackSize(slot, amount);
    }

    @Override
    public ItemStack removeStackFromSlot(int slot) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        inv.setInventorySlotContents(slot, stack);
    }

    @Override
    public int getInventoryStackLimit() {
        return inv.getInventoryStackLimit();
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return worldObj.getTileEntity(getPos()) == this && !isInvalid();
    }

    @Override
    public void openInventory(EntityPlayer player) {

    }

    @Override
    public void closeInventory(EntityPlayer player) {

    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        inv.writeToNBT(nbt, "inv");
        nbt.setInteger("energy", energy);
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        inv.readFromNBT(nbt, "inv");
        energy = nbt.getInteger("energy");
    }

    protected void outputStack(ItemStack remaining, boolean autoEject) {
        outputStack(remaining, null, 0, autoEject);
    }

    protected void outputStack(ItemStack remaining, IInventory inv, int slot, boolean autoEject) {
        if (autoEject) {
            if (remaining != null && remaining.stackSize > 0) {
                remaining.stackSize -= Utils.addToRandomInventoryAround(worldObj, getPos(), remaining);
            }

            if (remaining != null && remaining.stackSize > 0) {
                remaining.stackSize -= Utils.addToRandomInjectableAround(worldObj, getPos(), null, remaining);
            }
        }

        if (inv != null && remaining != null && remaining.stackSize > 0) {
            ItemStack inside = inv.getStackInSlot(slot);

            if (inside == null || inside.stackSize <= 0) {
                inv.setInventorySlotContents(slot, remaining);
                return;
            } else if (StackHelper.canStacksMerge(inside, remaining)) {
                remaining.stackSize -= StackHelper.mergeStacks(remaining, inside, true);
            }
        }

        if (remaining != null && remaining.stackSize > 0) {
            EntityItem entityitem = new EntityItem(worldObj, getPos().getX() + 0.5, getPos().getY() + 0.7, getPos().getZ() + 0.5, remaining);

            worldObj.spawnEntityInWorld(entityitem);
        }
    }

    public void getGUINetworkData(int id, int data) {
        int currentStored = energy;
        int requiredEnergy = clientRequiredEnergy;

        switch (id) {
            case 0:
                requiredEnergy = (requiredEnergy & 0xFFFF0000) | (data & 0xFFFF);
                clientRequiredEnergy = requiredEnergy;
                break;
            case 1:
                currentStored = (currentStored & 0xFFFF0000) | (data & 0xFFFF);
                energy = currentStored;
                break;
            case 2:
                requiredEnergy = (requiredEnergy & 0xFFFF) | ((data & 0xFFFF) << 16);
                clientRequiredEnergy = requiredEnergy;
                break;
            case 3:
                currentStored = (currentStored & 0xFFFF) | ((data & 0xFFFF) << 16);
                energy = currentStored;
                break;
            case 4:
                recentEnergyAverage = recentEnergyAverage & 0xFFFF0000 | (data & 0xFFFF);
                break;
            case 5:
                recentEnergyAverage = (recentEnergyAverage & 0xFFFF) | ((data & 0xFFFF) << 16);
                break;
        }
    }

    public void sendGUINetworkData(Container container, IContainerListener iCrafting) {
        int requiredEnergy = getRequiredEnergy();
        int currentStored = energy;
        int lRecentEnergy = (int) (recentEnergyAverageUtil.getAverage() * 100f);
        iCrafting.sendProgressBarUpdate(container, 0, requiredEnergy & 0xFFFF);
        iCrafting.sendProgressBarUpdate(container, 1, currentStored & 0xFFFF);
        iCrafting.sendProgressBarUpdate(container, 2, (requiredEnergy >>> 16) & 0xFFFF);
        iCrafting.sendProgressBarUpdate(container, 3, (currentStored >>> 16) & 0xFFFF);
        iCrafting.sendProgressBarUpdate(container, 4, lRecentEnergy & 0xFFFF);
        iCrafting.sendProgressBarUpdate(container, 5, (lRecentEnergy >>> 16) & 0xFFFF);
    }
}
