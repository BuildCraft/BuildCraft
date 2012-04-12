package net.minecraft.src.buildcraft.core.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketUpdate extends BuildCraftPacket {

	private int packetId;

    public int posX;
    public int posY;
    public int posZ;

    public PacketPayload payload;

    public PacketUpdate() {}
    public PacketUpdate(int packetId, PacketPayload payload) {
    	this(packetId);
    	this.payload = payload;
    }

    public PacketUpdate(int packetId) {

    	this.packetId = packetId;
		this.isChunkDataPacket = true;

	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {

        data.writeInt(posX);
        data.writeInt(posY);
        data.writeInt(posZ);

        // No payload means no data
        if(payload == null) {
        	data.writeInt(0);
        	data.writeInt(0);
        	data.writeInt(0);
        	return;
        }

        data.writeInt(payload.intPayload.length);
        data.writeInt(payload.floatPayload.length);
        data.writeInt(payload.stringPayload.length);

        for(int intData : payload.intPayload)
        	data.writeInt(intData);
        for(float floatData : payload.floatPayload)
        	data.writeFloat(floatData);
        for(String stringData : payload.stringPayload)
        	data.writeUTF(stringData);

	}

	@Override
	public void readData(DataInputStream data) throws IOException {

        posX = data.readInt();
        posY = data.readInt();
        posZ = data.readInt();

        payload = new PacketPayload();

        payload.intPayload = new int[data.readInt()];
        payload.floatPayload = new float[data.readInt()];
        payload.stringPayload = new String[data.readInt()];

        for(int i = 0; i < payload.intPayload.length; i++)
        	payload.intPayload[i] = data.readInt();
        for(int i = 0; i < payload.floatPayload.length; i++)
        	payload.floatPayload[i] = data.readFloat();
        for(int i = 0; i < payload.stringPayload.length; i++)
        	payload.stringPayload[i] = data.readUTF();

	}

	@Override
	public int getID() {
		return packetId;
	}

}
