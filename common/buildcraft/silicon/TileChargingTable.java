package buildcraft.silicon;

import net.minecraft.item.ItemStack;

import cofh.api.energy.IEnergyContainerItem;
import buildcraft.api.tiles.IHasWork;
import buildcraft.core.lib.utils.StringUtils;

public class TileChargingTable extends TileLaserTableBase implements IHasWork {
    @Override
    public void update() {
        super.update();

		if (worldObj.isRemote) {
			return;
		}

        if (getEnergy() > 0) {
            if (getRequiredEnergy() > 0) {
                ItemStack stack = this.getStackInSlot(0);
                IEnergyContainerItem containerItem = (IEnergyContainerItem) stack.getItem();
                addEnergy(0 - containerItem.receiveEnergy(stack, getEnergy(), false));
                this.setInventorySlotContents(0, stack);
            } else {
                subtractEnergy(Math.min(getEnergy(), 10));
            }
        }
    }

    @Override
    public int getRequiredEnergy() {
        ItemStack stack = this.getStackInSlot(0);
        if (stack != null && stack.getItem() != null && stack.getItem() instanceof IEnergyContainerItem) {
            IEnergyContainerItem containerItem = (IEnergyContainerItem) stack.getItem();
            return containerItem.getMaxEnergyStored(stack) - containerItem.getEnergyStored(stack);
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
        return 1;
    }

    @Override
    public String getInventoryName() {
        return StringUtils.localize("tile.chargingTableBlock.name");
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return slot == 0 && stack != null && stack.getItem() != null && stack.getItem() instanceof IEnergyContainerItem;
    }
}
