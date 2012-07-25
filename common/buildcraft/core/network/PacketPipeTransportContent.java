package buildcraft.core.network;

import buildcraft.api.core.Orientations;
import buildcraft.api.transport.IPipedItem;

public class PacketPipeTransportContent extends PacketUpdate {

	public PacketPipeTransportContent() {
		super(PacketIds.PIPE_CONTENTS);
	}

	public PacketPipeTransportContent(int x, int y, int z, IPipedItem item, Orientations orientation) {
		this();

		this.posX = x;
		this.posY = y;
		this.posZ = z;

		this.payload = new PacketPayload(6, 4, 0);

		payload.intPayload[0] = item.getEntityId();
		payload.intPayload[1] = orientation.ordinal();
		payload.intPayload[2] = item.getItemStack().itemID;
		payload.intPayload[3] = item.getItemStack().stackSize;
		payload.intPayload[4] = item.getItemStack().getItemDamage();
		payload.intPayload[5] = item.getDeterministicRandomization();

		payload.floatPayload[0] = (float) item.getPosition().x;
		payload.floatPayload[1] = (float) item.getPosition().y;
		payload.floatPayload[2] = (float) item.getPosition().z;
		payload.floatPayload[3] = item.getSpeed();
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
