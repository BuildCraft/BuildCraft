package buildcraft.silicon;

import buildcraft.api.tiles.IHasWork;
import buildcraft.core.utils.StringUtils;
import cofh.api.energy.IEnergyContainerItem;
import net.minecraft.item.ItemStack;

public class TileChargingTable extends TileLaserTableBase implements IHasWork {
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
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return slot == 0 && stack != null && stack.getItem() != null && stack.getItem() instanceof IEnergyContainerItem;
    }
}
