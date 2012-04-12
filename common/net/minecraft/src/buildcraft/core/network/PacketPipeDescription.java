package net.minecraft.src.buildcraft.core.network;

public class PacketPipeDescription extends PacketUpdate {

	public PacketPipeDescription() {}
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
