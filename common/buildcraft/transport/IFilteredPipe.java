package buildcraft.transport;

import buildcraft.core.network.IClientState;
import net.minecraft.inventory.IInventory;

public interface IFilteredPipe extends IClientState {
    IInventory getFilters();

}
