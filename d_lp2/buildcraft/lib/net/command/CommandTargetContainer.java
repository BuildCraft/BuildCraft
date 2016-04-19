package buildcraft.lib.net.command;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import buildcraft.api.core.BCLog;
import buildcraft.lib.LibProxy;
import buildcraft.lib.container.BCContainer_BC8;

public class CommandTargetContainer implements ICommandTarget {
    private final BCContainer_BC8 container;

    public CommandTargetContainer(BCContainer_BC8 container) {
        this.container = container;
    }

    @Override
    public ICommandReceiver getReceiver(PacketBuffer buffer, MessageContext context) {
        int windowId = buffer.readInt();
        EntityPlayer player = LibProxy.getProxy().getPlayerForContext(context);
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
