package net.minecraft.src.buildcraft.api;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.src.NetHandler;
import net.minecraft.src.Packet;

public class Packet122PassiveItemUpdate extends Packet {

	public int entityId;

	public double posX;
	public double posY;
	public double posZ;
	
	public double motionX;
	public double motionY;
	public double motionZ;	
	
	private EntityPassiveItem item;
	
	public Packet122PassiveItemUpdate () {
		
	}
	
	public Packet122PassiveItemUpdate (EntityPassiveItem item) {
		this.item = item;
	}
	
	@Override
	public void readPacketData(DataInputStream datainputstream)
			throws IOException {
		
		
		entityId = datainputstream.readInt();

		posX = datainputstream.readDouble();
		posY = datainputstream.readDouble();
		posZ = datainputstream.readDouble();
		
		motionX = datainputstream.readDouble();
		motionY = datainputstream.readDouble();
		motionZ = datainputstream.readDouble();
		
	}

	@Override
	public void writePacketData(DataOutputStream dataoutputstream)
			throws IOException {
		dataoutputstream.writeInt(item.entityId);

		dataoutputstream.writeDouble(item.posX);
		dataoutputstream.writeDouble(item.posY);
		dataoutputstream.writeDouble(item.posZ);
		
		dataoutputstream.writeDouble(item.motionX);
		dataoutputstream.writeDouble(item.motionY);
		dataoutputstream.writeDouble(item.motionZ);		
	}

	@Override
	public void processPacket(NetHandler nethandler) {
		APIProxy.handlePassiveEntityUpdate(this);		
	}

	@Override
	public int getPacketSize() {
		// TODO Auto-generated method stub
		return 1 * 4 + 6 * 8;
	}

}
