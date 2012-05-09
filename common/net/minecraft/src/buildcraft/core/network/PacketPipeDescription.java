package net.minecraft.src.buildcraft.core.network;

import net.minecraft.src.buildcraft.transport.Pipe;

public class PacketPipeDescription extends PacketUpdate {

	public PacketPipeDescription() {}
	
	public PacketPipeDescription(int posX, int posY, int posZ, Pipe pipe) {
		this(posX, posY, posZ, pipe.itemID);
		
		if(pipe.gate != null)
			this.payload.append(pipe.gate.toPayload());
				
	}
	
	public PacketPipeDescription(int posX, int posY, int posZ, int pipeId) {
		super(PacketIds.PIPE_DESCRIPTION);

		PacketPayload payload = new PacketPayload(1, 0, 0);
		this.posX = posX;
		this.posY = posY;
		this.posZ = posZ;

		payload.intPayload[0] = pipeId;
		this.payload = payload;

	}

}
