package buildcraft.lib.net.command;

import java.io.IOException;

import net.minecraft.network.PacketBuffer;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;

public interface IPayloadReceiver {
    IMessage receivePayload(Side side, PacketBuffer buffer) throws IOException;
}
