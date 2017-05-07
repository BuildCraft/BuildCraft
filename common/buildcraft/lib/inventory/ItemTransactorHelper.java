package buildcraft.lib.inventory;

import javax.annotation.Nonnull;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;

import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.IItemHandler;

import buildcraft.api.inventory.IItemTransactor;
import buildcraft.api.transport.IInjectable;
import buildcraft.api.transport.pipe.PipeApi;

import buildcraft.lib.misc.CapUtil;

public class ItemTransactorHelper {
    @Nonnull
    public static IItemTransactor getTransactor(ICapabilityProvider provider, EnumFacing face) {
        if (provider == null) {
            return NoSpaceTransactor.INSTANCE;
        }

        IItemTransactor trans = provider.getCapability(CapUtil.CAP_ITEM_TRANSACTOR, face);
        if (trans != null) {
            return trans;
        }

        IItemHandler handler = provider.getCapability(CapUtil.CAP_ITEMS, face);
        if (handler == null) {
            if (provider instanceof ISidedInventory) {
                return new SidedInventoryWrapper((ISidedInventory) provider, face);
            }
            if (provider instanceof IInventory) {
                return new InventoryWrapper((IInventory) provider);
            }
            return NoSpaceTransactor.INSTANCE;
        }
        if (handler instanceof IItemTransactor) {
            return (IItemTransactor) handler;
        }
        return new ItemHandlerWrapper(handler);
    }

    @Nonnull
    public static IItemTransactor getTransactor(InventoryPlayer inventory) {
        if (inventory == null) {
            return NoSpaceTransactor.INSTANCE;
        }
        return new InventoryWrapper(inventory);
    }

    @Nonnull
    public static IItemTransactor getTransactorForEntity(Entity entity, EnumFacing face) {
        IItemTransactor transactor = getTransactor(entity, face);
        if (transactor != NoSpaceTransactor.INSTANCE) {
            return transactor;
        } else if (entity instanceof EntityItem) {
            return new TransactorEntityItem((EntityItem) entity);
        } else if (entity instanceof EntityArrow) {
            return new TransactorEntityArrow((EntityArrow) entity);
        } else {
            return NoSpaceTransactor.INSTANCE;
        }
    }

    @Nonnull
    public static IInjectable getInjectable(ICapabilityProvider provider, EnumFacing face) {
        if (provider == null) {
            return NoSpaceInjectable.INSTANCE;
        }
        IInjectable injectable = provider.getCapability(PipeApi.CAP_INJECTABLE, face);
        if (injectable == null) {
            return NoSpaceInjectable.INSTANCE;
        }
        return injectable;
    }

    public static IItemTransactor wrapInjectable(IInjectable injectable, EnumFacing facing) {
        return new InjectableWrapper(injectable, facing);
    }

    /** Provides an implementation of {@link IItemTransactor#insert(NonNullList, boolean)} that relies on
     * {@link IItemTransactor#insert(ItemStack, boolean, boolean)}. This is the least efficient, default
     * implementation. */
    public static NonNullList<ItemStack> insertAllBypass(IItemTransactor transactor, NonNullList<ItemStack> stacks, boolean simulate) {
        NonNullList<ItemStack> leftOver = NonNullList.create();
        for (int i = 0; i < stacks.size(); i++) {
            ItemStack stack = stacks.get(i);
            ItemStack leftOverStack = transactor.insert(stack, false, simulate);
            if (!leftOverStack.isEmpty()) {
                leftOver.add(leftOverStack);
            }
        }
        return leftOver;
    }
}
