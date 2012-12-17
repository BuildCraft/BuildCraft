package buildcraft.core.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class PacketTileState extends PacketCoordinates {

	private class StateWithId {
		public byte stateId;
		public IClientState state;

		public StateWithId(byte stateId, IClientState state) {
			this.stateId = stateId;
			this.state = state;
		}
	}

	private List<StateWithId> stateList = new LinkedList<StateWithId>();

	/**
	 * Default constructor for incomming packets
	 */
	public PacketTileState() {
	}

	/**
	 * Constructor for outgoing packets
	 * 
	 * @param x
	 *            , y, z - the coordinates the tile to sync
	 */
	public PacketTileState(int x, int y, int z) {
		super(PacketIds.STATE_UPDATE, x, y, z);
		isChunkDataPacket = true;
	}

	@Override
	public int getID() {
		return PacketIds.STATE_UPDATE;
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);
	}

	public void applyStates(DataInputStream data, ISyncedTile tile) throws IOException {
		byte stateCount = data.readByte();
		for (int i = 0; i < stateCount; i++) {
			byte stateId = data.readByte();
			tile.getStateInstance(stateId).readData(data);
			tile.afterStateUpdated(stateId);
		}
	}

	public void addStateForSerialization(byte stateId, IClientState state) {
		stateList.add(new StateWithId(stateId, state));
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeByte(stateList.size());
		for (StateWithId stateWithId : stateList) {
			data.writeByte(stateWithId.stateId);
			stateWithId.state.writeData(data);
		}
	}
}
