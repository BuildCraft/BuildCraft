package buildcraft.lib.inventory;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.InventoryPlayer;
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
}
