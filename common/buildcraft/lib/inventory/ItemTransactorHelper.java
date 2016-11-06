package buildcraft.lib.inventory;

import javax.annotation.Nonnull;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import buildcraft.api.inventory.IItemTransactor;

public class ItemTransactorHelper {
    @Nonnull
    public static IItemTransactor getTransactor(ICapabilityProvider provider, EnumFacing face) {
        if (provider == null) {
            return NoSpaceTransactor.INSTANCE;
        }

        IItemHandler handler = provider.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, face);
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
}
