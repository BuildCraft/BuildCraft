package buildcraft.lib.net.command;

import java.io.IOException;

import net.minecraft.network.PacketBuffer;

import net.minecraftforge.fml.relauncher.Side;

public interface IPayloadReceiver {
    void receivePayload(Side side, PacketBuffer buffer) throws IOException;
}
