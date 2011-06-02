package net.minecraft.src.buildcraft.api;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.src.NetHandler;
import net.minecraft.src.Packet;

public class Packet121PassiveItemSpawn extends Packet {
	
	public int entityId;
	public double posX;
	public double posY;
	public double posZ;
	
	public int itemID;
	public int stackSize;
	public int damage;
	
	private EntityPassiveItem item;
	
	public Packet121PassiveItemSpawn () {
		System.out.println ("CREATE CLIENT");
	}
	
	public Packet121PassiveItemSpawn (EntityPassiveItem item) {
		this.item = item;
		
		System.out.println ("CREATE SERVER");
	}

	@Override
	public void readPacketData(DataInputStream datainputstream)
			throws IOException {
		
		System.out.println ("READ");
		
		entityId = datainputstream.readInt();
		posX = datainputstream.readDouble();
		posY = datainputstream.readDouble();
		posZ = datainputstream.readDouble();
		

		itemID = datainputstream.readInt();
		stackSize = datainputstream.readInt();
		damage = datainputstream.readInt();		
	}

	@Override
	public void writePacketData(DataOutputStream dataoutputstream)
			throws IOException {
		
		System.out.println ("WRITE");
		
		dataoutputstream.writeInt(item.entityId);
		dataoutputstream.writeDouble(item.posX);
		dataoutputstream.writeDouble(item.posY);
		dataoutputstream.writeDouble(item.posZ);
		
		dataoutputstream.writeInt(item.item.itemID);
		dataoutputstream.writeInt(item.item.stackSize);
		dataoutputstream.writeInt(item.item.getItemDamage());		
	}

	@Override
	public void processPacket(NetHandler nethandler) {
		APIProxy.handlePassiveEntitySpawn(this);		
	}

	@Override
	public int getPacketSize() {
		return 3 * 8 + 4 * 4;
	}

}
