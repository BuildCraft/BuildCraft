package buildcraft.factory.network;

import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import net.minecraft.src.NetworkManager;
import net.minecraft.src.Packet250CustomPayload;

public class PacketHandlerFactory implements IPacketHandler {

	@Override
	public void onPacketData(NetworkManager manager, Packet250CustomPayload packet, Player player) {}

}
