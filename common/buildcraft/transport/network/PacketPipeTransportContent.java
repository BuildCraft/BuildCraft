package buildcraft.transport.network;

import buildcraft.core.network.BuildCraftPacket;
import buildcraft.core.network.PacketIds;
import buildcraft.core.utils.EnumColor;
import buildcraft.transport.TravelingItem;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.ForgeDirection;

public class PacketPipeTransportContent extends BuildCraftPacket {

	private TravelingItem item;
	private int entityId;
	private ForgeDirection input;
	private ForgeDirection output;
	private int itemId;
	private byte stackSize;
	private int itemDamage;
	private EnumColor color;
	private float itemX;
	private float itemY;
	private float itemZ;
	private float speed;
	private boolean hasNBT;
	public int posX;
	public int posY;
	public int posZ;

	public PacketPipeTransportContent() {
	}

	public PacketPipeTransportContent(TravelingItem item) {
		this.item = item;
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		data.writeFloat((float) item.xCoord);
		data.writeFloat((float) item.yCoord);
		data.writeFloat((float) item.zCoord);

		data.writeShort(item.id);

		data.writeByte((byte) item.input.ordinal());
		data.writeByte((byte) item.output.ordinal());

		data.writeShort(item.getItemStack().itemID);
		data.writeByte((byte) item.getItemStack().stackSize);
		data.writeShort(item.getItemStack().getItemDamage());

		data.writeByte(item.color != null ? item.color.ordinal() : -1);

		data.writeFloat(item.getSpeed());
		data.writeBoolean(item.getItemStack().hasTagCompound());
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		this.itemX = data.readFloat();
		this.itemY = data.readFloat();
		this.itemZ = data.readFloat();

		posX = MathHelper.floor_float(itemX);
		posY = MathHelper.floor_float(itemY);
		posZ = MathHelper.floor_float(itemZ);

		this.entityId = data.readShort();

		this.input = ForgeDirection.getOrientation(data.readByte());
		this.output = ForgeDirection.getOrientation(data.readByte());

		this.itemId = data.readShort();
		this.stackSize = data.readByte();
		this.itemDamage = data.readShort();

		byte c = data.readByte();
		if (c != -1)
			this.color = EnumColor.fromId(c);

		this.speed = data.readFloat();
		this.hasNBT = data.readBoolean();
	}

	public int getTravellingItemId() {
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

	public EnumColor getColor() {
		return color;
	}

	public double getItemX() {
		return itemX;
	}

	public double getItemY() {
		return itemY;
	}

	public double getItemZ() {
		return itemZ;
	}

	public float getSpeed() {
		return speed;
	}

	public boolean hasNBT() {
		return hasNBT;
	}

	@Override
	public int getID() {
		return PacketIds.PIPE_CONTENTS;
	}
}
