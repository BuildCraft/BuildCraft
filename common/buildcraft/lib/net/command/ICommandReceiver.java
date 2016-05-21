package buildcraft.lib.net.command;

import java.io.IOException;

import net.minecraft.network.PacketBuffer;

import net.minecraftforge.fml.relauncher.Side;

import buildcraft.lib.net.MessageCommand;

@Deprecated
public interface ICommandReceiver {
    MessageCommand receiveCommand(String command, Side side, PacketBuffer buffer) throws IOException;
}
