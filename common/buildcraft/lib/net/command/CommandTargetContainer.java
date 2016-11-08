package buildcraft.lib.net.command;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import buildcraft.lib.BCLibProxy;
import buildcraft.lib.gui.ContainerBC_Neptune;

public class CommandTargetContainer implements ICommandTarget {
    private final ContainerBC_Neptune container;

    public CommandTargetContainer(ContainerBC_Neptune container) {
        this.container = container;
    }

    @Override
    public ICommandReceiver getReceiver(PacketBuffer buffer, MessageContext context) {
        int windowId = buffer.readInt();
        EntityPlayer player = BCLibProxy.getProxy().getPlayerForContext(context);
        if (player != null && player.openContainer instanceof ICommandReceiver && player.openContainer.windowId == windowId) {
            return (ICommandReceiver) player.openContainer;
        }
        return null;
    }

    @Override
    public void writePositionData(PacketBuffer buffer) {
        buffer.writeInt(container.windowId);
    }

    @Override
    public CommandTargetType getType() {
        return CommandTargetType.CONTAINER;
    }
}
