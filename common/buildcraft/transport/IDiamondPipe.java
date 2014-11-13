package buildcraft.transport;

import buildcraft.core.network.IClientState;
import net.minecraft.inventory.IInventory;

public interface IDiamondPipe extends IClientState {
    IInventory getFilters();

}
