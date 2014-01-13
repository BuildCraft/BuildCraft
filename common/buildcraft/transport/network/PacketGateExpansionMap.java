package buildcraft.transport.network;

import buildcraft.api.gates.GateExpansions;
import buildcraft.core.network.BuildCraftPacket;
import buildcraft.core.network.PacketIds;
import buildcraft.core.utils.Utils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import io.netty.buffer.ByteBuf;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

public class PacketGateExpansionMap extends BuildCraftPacket {

	public PacketGateExpansionMap() {
	}

	@Override
	public void writeData(ByteBuf data) {
		BiMap<Byte, String> map = GateExpansions.getServerMap();
		data.writeByte(map.size());
		for (Map.Entry<Byte, String> entry : map.entrySet()) {
			data.writeByte(entry.getKey());
			Utils.writeUTF(data, entry.getValue());
		}
	}

	@Override
	public void readData(ByteBuf data) {
		int numEntries = data.readByte();
		BiMap<Byte, String> map = HashBiMap.create(numEntries);
		for (int i = 0; i < numEntries; i++) {
			byte id = data.readByte();
			String identifier = Utils.readUTF(data);
			map.put(id, identifier);
		}
		GateExpansions.setClientMap(map);
	}

	@Override
	public int getID() {
		return PacketIds.PIPE_GATE_EXPANSION_MAP;
	}
}
