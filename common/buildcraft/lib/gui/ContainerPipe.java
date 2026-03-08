package buildcraft.lib.gui;

import buildcraft.api.transport.pipe.IPipeHolder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ContainerType;

//public abstract class ContainerPipe extends ContainerBC_Neptune
public abstract class ContainerPipe extends ContainerBC_Neptune<IPipeHolder> {

    public final IPipeHolder pipeHolder;

    public ContainerPipe(ContainerType menuType, int id, PlayerEntity player, IPipeHolder pipeHolder) {
        super(menuType, id, player);
        this.pipeHolder = pipeHolder;
    }

    @Override
//    public final boolean canInteractWith(PlayerEntity player)
    public final boolean stillValid(PlayerEntity player) {
        return pipeHolder.canPlayerInteract(player);
    }
}
