package buildcraft.lib.gui;

import buildcraft.api.transport.pipe.IPipeHolder;
import net.minecraft.entity.player.EntityPlayer;

public abstract class ContainerPipe extends ContainerBC_Neptune {

    public final IPipeHolder pipeHolder;

    public ContainerPipe(EntityPlayer player, IPipeHolder pipeHolder) {
        super(player);
        this.pipeHolder = pipeHolder;
    }

    @Override
    public final boolean canInteractWith(EntityPlayer player) {
        return pipeHolder.canPlayerInteract(player);
    }
}
