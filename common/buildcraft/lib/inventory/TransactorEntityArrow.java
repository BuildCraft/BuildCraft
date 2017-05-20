package buildcraft.lib.inventory;

import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityArrow.PickupStatus;
import net.minecraft.item.ItemStack;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.inventory.IItemTransactor.IItemExtractable;

import buildcraft.lib.misc.EntityUtil;
import buildcraft.lib.misc.StackUtil;

import javax.annotation.Nonnull;

public class TransactorEntityArrow implements IItemExtractable {

    private final EntityArrow entity;

    public TransactorEntityArrow(EntityArrow entity) {
        this.entity = entity;
    }

    @Nonnull
    @Override
    public ItemStack extract(IStackFilter filter, int min, int max, boolean simulate) {
        if (entity.isDead || entity.pickupStatus != PickupStatus.ALLOWED || min > 1 || max < 1 || max < min) {
            return StackUtil.EMPTY;
        }

        ItemStack stack = EntityUtil.getArrowStack(entity);
        if (!simulate) {
            entity.setDead();
        }
        return stack;
    }
}
