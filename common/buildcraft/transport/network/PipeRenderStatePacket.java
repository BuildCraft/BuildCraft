package buildcraft.transport.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import buildcraft.core.network.PacketCoordinates;
import buildcraft.core.network.PacketIds;
import buildcraft.transport.PipeRenderState;

public class PipeRenderStatePacket extends PacketCoordinates {

	private PipeRenderState renderState;
	public int pipeId;

	public PipeRenderStatePacket() {

	}

	public PipeRenderStatePacket(PipeRenderState renderState, int pipeId, int x, int y, int z) {
		super(PacketIds.PIPE_DESCRIPTION, x, y, z);
		this.pipeId = pipeId;
		this.isChunkDataPacket = true;
		this.renderState = renderState;
	}

	public PipeRenderState getRenderState() {
		return this.renderState;
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeInt(pipeId);
		renderState.writeData(data);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);
		pipeId = data.readInt();
		renderState = new PipeRenderState();
		renderState.readData(data);
	}

	@Override
	public int getID() {
		return PacketIds.PIPE_DESCRIPTION;
	}

	public void setPipeId(int pipeId) {
		this.pipeId = pipeId;
	}

	public int getPipeId() {
		return pipeId;
	}

}
