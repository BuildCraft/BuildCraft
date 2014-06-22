package buildcraft.core.network;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ServerConnectionFromClientEvent;

import buildcraft.core.utils.Utils;

public class NetworkIdRegistry {

	public static NetworkIdRegistry instance;

	private static boolean isLocal = false;
	private static boolean isMaster = false;
	private static BiMap<String, Integer> idMap = HashBiMap.create();
	private static LinkedList<EntityPlayerMP> playersToInitialize = new LinkedList<EntityPlayerMP>();

	public NetworkIdRegistry() {
		FMLCommonHandler.instance().bus().register(this);
	}

	public static void write(ByteBuf buf, String strId) {
		if (!isMaster) {
			if (!idMap.containsKey(strId)) {
				buf.writeInt(-1);
				Utils.writeUTF(buf, strId);
			} else {
				buf.writeInt(idMap.get(strId));
			}
		} else {
			if (!idMap.containsKey(strId)) {
				idMap.put(strId, idMap.size());

				if (!isLocal) {
					RPCHandler.rpcBroadcastAllPlayers(NetworkIdRegistry.class, "receiveId", strId, idMap.size() - 1);
				}
			}

			buf.writeInt(idMap.get(strId));
		}
	}

	public static String read(ByteBuf buf) {
		int id = buf.readInt();

		if (!isMaster) {
			if (!idMap.inverse().containsKey(id)) {
				RPCHandler.rpcServer(NetworkIdRegistry.class, "requestId", id);
				throw new IllegalArgumentException("Id " + id + " unknown by the registry.");
			} else {
				return idMap.inverse().get(id);
			}
		} else {
			if (id == -1) {
				String str = Utils.readUTF(buf);

				if (!idMap.containsKey(str)) {
					idMap.put(str, idMap.size());
				}

				return str;
			} else {
				return idMap.inverse().get(id);
			}
		}
	}

	@RPC(RPCSide.SERVER)
	private static void requestId(int id, RPCMessageInfo info) {
		if (!idMap.inverse().containsKey(id)) {
			throw new IllegalArgumentException("Id " + id + " unknown by the registry.");
		} else {
			RPCHandler.rpcPlayer(info.sender, NetworkIdRegistry.class, "receiveId", idMap.inverse().get(id), id);
		}
	}

	@RPC(RPCSide.SERVER)
	private static void receiveId(String str, int id) {
		idMap.put(str, id);
	}

	private static void sendAllIdsTo(EntityPlayerMP player) {
		ArrayList<String> idStr = new ArrayList<String>();
		ArrayList<Integer> ids = new ArrayList<Integer>();

		for (Map.Entry<String, Integer> e : idMap.entrySet()) {
			idStr.add(e.getKey());
			ids.add(e.getValue());
		}

		RPCHandler.rpcPlayer(player, NetworkIdRegistry.class, "receiveAllIds", idStr, ids);
	}

	@RPC(RPCSide.CLIENT)
	private static void receiveAllIds(ArrayList<String> idStr, ArrayList<Integer> ids) {
		idMap.clear();

		for (int i = 0; i < idStr.size(); ++i) {
			if (!idMap.containsKey(idStr.get(i))) {
				System.out.println("INIT " + ids.get(i) + " => " + idStr.get(i));
				idMap.put(idStr.get(i), ids.get(i));
			}
		}
	}

	@SubscribeEvent
	public void serverConnected(ServerConnectionFromClientEvent evt) {
		isMaster = true;

		if (evt.isLocal) {
			isLocal = true;
		} else {
			isLocal = false;

			// the server cannot send messages to the client at this stage, so
			// cache the new client to receive ids on the next tick.
			playersToInitialize.add(((NetHandlerPlayServer) evt.handler).playerEntity);
		}
	}

	@SubscribeEvent
	public void serverConnected(ClientConnectedToServerEvent evt) {
		if (!evt.isLocal) {
			isMaster = false;
			isLocal = false;
		}
	}

	@SubscribeEvent
	public void tick(ServerTickEvent evt) {
		for (EntityPlayerMP player : playersToInitialize) {
			sendAllIdsTo(player);
		}

		playersToInitialize.clear();
	}

	static {
		// The ids below are the minimal ids necessary to initialize properly
		// the server. They are aimed at supporting the provide of receiveAllIds
		idMap.put("", 0);
		idMap.put(NetworkIdRegistry.class.getCanonicalName(), 1);
		idMap.put(String.class.getCanonicalName(), 2);
		idMap.put(Integer.class.getCanonicalName(), 3);
	}
}
