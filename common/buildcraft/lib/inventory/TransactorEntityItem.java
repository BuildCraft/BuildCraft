package buildcraft.lib.inventory;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;

import buildcraft.api.core.IStackFilter;

public class TransactorEntityItem implements IItemExtractable {

    private final EntityItem entity;

    public TransactorEntityItem(EntityItem entity) {
        this.entity = entity;
    }

    @Override
    public ItemStack extract(IStackFilter filter, int min, int max, boolean simulate) {
        if (entity.isDead) {
            return null;
        }
        ItemStack current = entity.getEntityItem();
        if (current == null || current.stackSize < min || min > 1 || max < 1 || max < min) {
            return null;
        }
        if (filter.matches(current)) {
            ItemStack extracted = simulate ? current.copy().splitStack(max) : current.splitStack(max);
            if (!simulate) {
                if (current.stackSize == 0) {
                    entity.setDead();
                } else {
                    entity.setEntityItemStack(current);
                }
            }
            return extracted;
        } else {
            return null;
        }
    }

}
