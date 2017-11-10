/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.network;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import buildcraft.api.core.ISerializable;
import buildcraft.core.network.PacketIds;

public class PacketTileState extends PacketCoordinates {

	private ByteBuf state;

	private class StateWithId {
		public byte stateId;
		public ISerializable state;

		public StateWithId(byte stateId, ISerializable state) {
			this.stateId = stateId;
			this.state = state;
		}
	}

	private List<StateWithId> stateList = new LinkedList<StateWithId>();

	/**
	 * Default constructor for incoming packets
	 */
	public PacketTileState() {
	}

	/**
	 * Constructor for outgoing packets
	 *
	 * @param x, y, z - the coordinates the tile to sync
	 */
	public PacketTileState(int x, int y, int z) {
		super(PacketIds.STATE_UPDATE, x, y, z);
		isChunkDataPacket = true;
	}

	@Override
	public int getID() {
		return PacketIds.STATE_UPDATE;
	}

	public void applyStates(ISyncedTile tile) throws IOException {
		byte stateCount = state.readByte();
		for (int i = 0; i < stateCount; i++) {
			byte stateId = state.readByte();
			tile.getStateInstance(stateId).readData(state);
			tile.afterStateUpdated(stateId);
		}
	}

	public void addStateForSerialization(byte stateId, ISerializable state) {
		stateList.add(new StateWithId(stateId, state));
	}

	@Override
	public void writeData(ByteBuf data) {
		super.writeData(data);

		ByteBuf tmpState = Unpooled.buffer();

		tmpState.writeByte(stateList.size());
		for (StateWithId stateWithId : stateList) {
			tmpState.writeByte(stateWithId.stateId);
			stateWithId.state.writeData(tmpState);
		}

		data.writeShort((short) tmpState.readableBytes());
		data.writeBytes(tmpState.readBytes(tmpState.readableBytes()));
	}

	@Override
	public void readData(ByteBuf data) {
		super.readData(data);

		state = Unpooled.buffer();
		int length = data.readUnsignedShort();
		state.writeBytes(data.readBytes(length));
	}
}
