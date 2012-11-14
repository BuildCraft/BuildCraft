package buildcraft.transport.network;

import net.minecraftforge.common.ForgeDirection;

import buildcraft.core.network.PacketIds;
import buildcraft.core.network.PacketPayload;
import buildcraft.core.network.PacketUpdate;

import buildcraft.transport.EntityData;

public class PacketPipeTransportContent extends PacketUpdate {

	public PacketPipeTransportContent() {
		super(PacketIds.PIPE_CONTENTS);
	}

	public PacketPipeTransportContent(int x, int y, int z, EntityData data) {
		this();

		this.posX = x;
		this.posY = y;
		this.posZ = z;

		this.payload = new PacketPayload(6, 4, 0);

		payload.intPayload[0] = data.item.getEntityId();
		payload.intPayload[1] = data.input.ordinal();
		payload.intPayload[2] = data.output.ordinal();
		payload.intPayload[3] = data.item.getItemStack().itemID;
		payload.intPayload[4] = data.item.getItemStack().stackSize;
		payload.intPayload[5] = data.item.getItemStack().getItemDamage();

		payload.floatPayload[0] = (float) data.item.getPosition().x;
		payload.floatPayload[1] = (float) data.item.getPosition().y;
		payload.floatPayload[2] = (float) data.item.getPosition().z;
		payload.floatPayload[3] = data.item.getSpeed();
	}

	public int getEntityId() {
		return payload.intPayload[0];
	}

	public ForgeDirection getInputOrientation() {
		return ForgeDirection.values()[payload.intPayload[1]];
	}

	public ForgeDirection getOutputOrientation() {
		return ForgeDirection.values()[payload.intPayload[2]];
	}

	public int getItemId() {
		return payload.intPayload[3];
	}

	public int getStackSize() {
		return payload.intPayload[4];
	}

	public int getItemDamage() {
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
