package buildcraft.transport.network;

import buildcraft.core.network.BuildCraftPacket;
import buildcraft.core.network.PacketIds;
import buildcraft.core.utils.EnumColor;
import buildcraft.transport.TravelingItem;
import io.netty.buffer.ByteBuf;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.util.MathHelper;
import net.minecraftforge.common.util.ForgeDirection;

public class PacketPipeTransportTraveler extends BuildCraftPacket {

	private TravelingItem item;
	private boolean forceStackRefresh;
	private int entityId;
	private ForgeDirection input;
	private ForgeDirection output;
	private EnumColor color;
	private float itemX;
	private float itemY;
	private float itemZ;
	private float speed;
	public int posX;
	public int posY;
	public int posZ;

	public PacketPipeTransportTraveler() {
	}

	public PacketPipeTransportTraveler(TravelingItem item, boolean forceStackRefresh) {
		this.item = item;
		this.forceStackRefresh = forceStackRefresh;
	}

	@Override
	public void writeData(ByteBuf data) {
		data.writeFloat((float) item.xCoord);
		data.writeFloat((float) item.yCoord);
		data.writeFloat((float) item.zCoord);

		data.writeShort(item.id);

		data.writeByte((byte) item.input.ordinal());
		data.writeByte((byte) item.output.ordinal());

		data.writeByte(item.color != null ? item.color.ordinal() : -1);

		data.writeFloat(item.getSpeed());

		data.writeBoolean(forceStackRefresh);
	}

	@Override
	public void readData(ByteBuf data) {
		this.itemX = data.readFloat();
		this.itemY = data.readFloat();
		this.itemZ = data.readFloat();

		posX = MathHelper.floor_float(itemX);
		posY = MathHelper.floor_float(itemY);
		posZ = MathHelper.floor_float(itemZ);

		this.entityId = data.readShort();

		this.input = ForgeDirection.getOrientation(data.readByte());
		this.output = ForgeDirection.getOrientation(data.readByte());

		byte c = data.readByte();
		if (c != -1)
			this.color = EnumColor.fromId(c);

		this.speed = data.readFloat();

		this.forceStackRefresh = data.readBoolean();
	}

	public int getTravelingEntityId() {
		return entityId;
	}

	public ForgeDirection getInputOrientation() {
		return input;
	}

	public ForgeDirection getOutputOrientation() {
		return output;
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

	public boolean forceStackRefresh() {
		return forceStackRefresh;
	}

	@Override
	public int getID() {
		return PacketIds.PIPE_TRAVELER;
	}
}
