package buildcraft.transport.network;

import buildcraft.core.network.PacketCoordinates;
import buildcraft.core.network.PacketIds;
import buildcraft.transport.EntityData;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraftforge.common.ForgeDirection;

public class PacketPipeTransportContent extends PacketCoordinates {

	private EntityData entityData;

	private int entityId;

	private ForgeDirection input;
	private ForgeDirection output;

	private int itemId;
	private byte stackSize;
	private int itemDamage;

	private float itemX;
	private float itemY;
	private float itemZ;

	private float speed;

	public PacketPipeTransportContent() {}

	public PacketPipeTransportContent(int x, int y, int z, EntityData data) {
		super(PacketIds.PIPE_CONTENTS, x, y, z);
		
		this.entityData = data;
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);

		data.writeInt(entityData.item.getEntityId());

		data.writeByte((byte)entityData.input.ordinal());
		data.writeByte((byte)entityData.output.ordinal());

		data.writeShort(entityData.item.getItemStack().itemID);
		data.writeByte((byte)entityData.item.getItemStack().stackSize);
		data.writeShort(entityData.item.getItemStack().getItemDamage());

		data.writeFloat((float) entityData.item.getPosition().x);
		data.writeFloat((float) entityData.item.getPosition().y);
		data.writeFloat((float) entityData.item.getPosition().z);

		data.writeFloat(entityData.item.getSpeed());
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);

		this.entityId = data.readInt();

		this.input = ForgeDirection.getOrientation(data.readByte());
		this.output = ForgeDirection.getOrientation(data.readByte());

		this.itemId = data.readShort();
		this.stackSize = data.readByte();
		this.itemDamage = data.readShort();

		this.itemX = data.readFloat();
		this.itemY = data.readFloat();
		this.itemZ = data.readFloat();

		this.speed = data.readFloat();
	}

	public int getEntityId() {
		return entityId;
	}

	public ForgeDirection getInputOrientation() {
		return input;
	}

	public ForgeDirection getOutputOrientation() {
		return output;
	}

	public int getItemId() {
		return itemId;
	}

	public int getStackSize() {
		return stackSize;
	}

	public int getItemDamage() {
		return itemDamage;
	}

	public double getPosX() {
		return itemX;
	}

	public double getPosY() {
		return itemY;
	}

	public double getPosZ() {
		return itemZ;
	}

	public float getSpeed() {
		return speed;
	}

	@Override
	public int getID() {
		return PacketIds.PIPE_CONTENTS;
	}
}
