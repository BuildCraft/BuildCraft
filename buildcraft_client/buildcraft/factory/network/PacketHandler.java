package buildcraft.factory.network;

import net.minecraft.src.NetworkManager;
import net.minecraft.src.forge.IPacketHandler;

public class PacketHandler implements IPacketHandler {

	@Override
	public void onPacketData(NetworkManager network, String channel, byte[] data) {}

}
