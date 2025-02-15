package buildcraft.lib.gui;

import buildcraft.api.transport.pipe.IPipeHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

//public abstract class ContainerPipe extends ContainerBC_Neptune
public abstract class ContainerPipe extends ContainerBC_Neptune<IPipeHolder> {

    public final IPipeHolder pipeHolder;

    public ContainerPipe(MenuType menuType, int id, Player player, IPipeHolder pipeHolder) {
        super(menuType, id, player);
        this.pipeHolder = pipeHolder;
    }

    @Override
//    public final boolean canInteractWith(Player player)
    public final boolean stillValid(Player player) {
        return pipeHolder.canPlayerInteract(player);
    }
}
