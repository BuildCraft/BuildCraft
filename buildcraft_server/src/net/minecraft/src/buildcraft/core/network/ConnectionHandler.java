package net.minecraft.src.buildcraft.core.network;

import net.minecraft.src.NetworkManager;
import net.minecraft.src.Packet1Login;
import net.minecraft.src.forge.IConnectionHandler;
import net.minecraft.src.forge.MessageManager;

public class ConnectionHandler implements IConnectionHandler {

	@Override
	public void onConnect(NetworkManager network) {
		MessageManager.getInstance().registerChannel(network, new PacketHandler(), "BC");
	}

	@Override
	public void onLogin(NetworkManager network, Packet1Login login) {
	}

	@Override
	public void onDisconnect(NetworkManager network, String message,
			Object[] args) {
	}

}
