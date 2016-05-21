package buildcraft.lib.net.command;

import net.minecraft.network.PacketBuffer;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

@Deprecated
public interface ICommandTarget {
    ICommandReceiver getReceiver(PacketBuffer buffer, MessageContext context);

    void writePositionData(PacketBuffer buffer);

    CommandTargetType getType();
}
