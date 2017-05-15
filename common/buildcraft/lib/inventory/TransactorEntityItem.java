package buildcraft.lib.inventory;

import javax.annotation.Nonnull;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.inventory.IItemTransactor.IItemExtractable;

import buildcraft.lib.misc.StackUtil;

public class TransactorEntityItem implements IItemExtractable {

    private final EntityItem entity;

    public TransactorEntityItem(EntityItem entity) {
        this.entity = entity;
    }

    @Override
    @Nonnull
    public ItemStack extract(IStackFilter filter, int min, int max, boolean simulate) {
        if (entity.isDead) {
            return StackUtil.EMPTY;
        }
        ItemStack current = entity.getEntityItem();
        if (current == null || current.getCount() < min || min > 1 || max < 1 || max < min) {
            return StackUtil.EMPTY;
        }
        if (filter.matches(current)) {
            ItemStack extracted = simulate ? current.copy().splitStack(max) : current.splitStack(max);
            if (!simulate) {
                if (current.getCount() == 0) {
                    entity.setDead();
                } else {
                    entity.setEntityItemStack(current);
                }
            }
            return extracted;
        } else {
            return StackUtil.EMPTY;
        }
    }
}
