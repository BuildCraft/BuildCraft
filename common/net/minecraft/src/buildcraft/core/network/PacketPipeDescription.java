package net.minecraft.src.buildcraft.core.network;

import net.minecraft.src.buildcraft.transport.Pipe;

public class PacketPipeDescription extends PacketUpdate {

	public PacketPipeDescription() {}
	
	public PacketPipeDescription(int posX, int posY, int posZ, Pipe pipe) {
		
		super(PacketIds.PIPE_DESCRIPTION);

		PacketPayload payload = new PacketPayload(5, 0, 0);
		this.posX = posX;
		this.posY = posY;
		this.posZ = posZ;

		/// Null pipe
		if(pipe == null) {
			payload.intPayload[0] = 0;
			this.payload = payload;
			return;
		}
		
		/// Need to synch ID, wires and gates
		payload.intPayload[0] = pipe.itemID;
		this.payload = payload;
		
		for(int i = 0; i < 4; ++i)
			if(pipe.wireSet[i])
				payload.intPayload[1 + i] = 1;
			else
				payload.intPayload[1 + i] = 0;
			
		if(pipe.gate != null)
			this.payload.append(pipe.gate.toPayload());
				
	}

}
