package net.minecraft.src.buildcraft.core.network;

import net.minecraft.src.buildcraft.api.EntityPassiveItem;
import net.minecraft.src.buildcraft.api.Orientations;

public class PacketPipeTransportContent extends PacketUpdate {

	public PacketPipeTransportContent() {
		super(PacketIds.PIPE_CONTENTS);
	}
	
	public PacketPipeTransportContent(int x, int y, int z, EntityPassiveItem item, Orientations orientation) {
		this();
		
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		
		this.payload = new PacketPayload(6, 4, 0);
		
		payload.intPayload[0] = item.entityId;
		payload.intPayload[1] = orientation.ordinal();
		payload.intPayload[2] = item.item.itemID;
		payload.intPayload[3] = item.item.stackSize;
		payload.intPayload[4] = item.item.getItemDamage();
		payload.intPayload[5] = item.deterministicRandomization;

		payload.floatPayload[0] = (float) item.posX;
		payload.floatPayload[1] = (float) item.posY;
		payload.floatPayload[2] = (float) item.posZ;
		payload.floatPayload[3] = item.speed;
	}
	
	public int getEntityId() {
		return payload.intPayload[0];
	}
	
	public Orientations getOrientation() {
		return Orientations.values()[payload.intPayload[1]];
	}
	
	public int getItemId() {
		return payload.intPayload[2];
	}
	
	public int getStackSize() {
		return payload.intPayload[3];
	}
	
	public int getItemDamage() {
		return payload.intPayload[4];
	}
	
	public int getRandomization() {
		return payload.intPayload[5];
	}
	
	public double getPosX() {
		return payload.floatPayload[0];
	}
	public double getPosY() {
		return payload.floatPayload[1];
	}
	public double getPosZ() {
		return payload.floatPayload[2];
	}
	
	public float getSpeed() {
		return payload.floatPayload[3];
	}
}
