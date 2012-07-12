package net.minecraft.src.buildcraft.transport.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.src.buildcraft.core.network.PacketCoordinates;
import net.minecraft.src.buildcraft.core.network.PacketIds;
import net.minecraft.src.buildcraft.transport.PipeRenderState;

public class PipeRenderStatePacket extends PacketCoordinates {
	
	private PipeRenderState renderState;
	
	public PipeRenderStatePacket(PipeRenderState renderState, int x, int y, int z) {
		super(PacketIds.PIPE_RENDER_STATE, x, y, z);
		this.renderState = renderState;
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		renderState.writeData(data);
		
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		renderState.readData(data);
	}

	@Override
	public int getID() {
		return PacketIds.PIPE_RENDER_STATE;
	}

}
