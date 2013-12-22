package buildcraft.transport.network;

import buildcraft.api.gates.GateExpansions;
import buildcraft.core.network.BuildCraftPacket;
import buildcraft.core.network.PacketIds;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

public class PacketGateExpansionMap extends BuildCraftPacket {

	public PacketGateExpansionMap() {
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		BiMap<Byte, String> map = GateExpansions.getServerMap();
		data.writeByte(map.size());
		for (Map.Entry<Byte, String> entry : map.entrySet()) {
			data.writeByte(entry.getKey());
			data.writeUTF(entry.getValue());
		}
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		int numEntries = data.readByte();
		BiMap<Byte, String> map = HashBiMap.create(numEntries);
		for (int i = 0; i < numEntries; i++) {
			byte id = data.readByte();
			String identifier = data.readUTF();
			map.put(id, identifier);
		}
		GateExpansions.setClientMap(map);
	}

	@Override
	public int getID() {
		return PacketIds.PIPE_GATE_EXPANSION_MAP;
	}
}
