package ct.buildcraft.lib.gui;

import ct.buildcraft.api.transport.pipe.IPipeHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

public abstract class ContainerPipe extends MenuBC_Neptune {

    public final IPipeHolder pipeHolder;

    public ContainerPipe(Inventory playerInventory, MenuType<?> type, int id, IPipeHolder pipeHolder) {
        super(playerInventory, type, id);
        this.pipeHolder = pipeHolder;
    }

	@Override
	public boolean stillValid(Player player) {
		return pipeHolder.canPlayerInteract(player);
	}
}
