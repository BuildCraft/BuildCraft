package ct.buildcraft.lib.gui;

import javax.annotation.Nullable;

import ct.buildcraft.api.transport.pipe.IPipeHolder;
import ct.buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

public abstract class ContainerPipe extends MenuBC_Neptune implements IMenuBCTile {

    public final IPipeHolder pipeHolder;

    public ContainerPipe(Inventory playerInventory, MenuType<?> type, int id, IPipeHolder pipeHolder) {
        super(playerInventory, type, id);
        this.pipeHolder = pipeHolder;
    }

    @Nullable
    @Override
    public TileBC_Neptune getBCTile() {
        return pipeHolder instanceof TileBC_Neptune tile ? tile : null;
    }

	@Override
	public boolean stillValid(Player player) {
		return pipeHolder.canPlayerInteract(player);
	}
}
