/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.network;

import io.netty.buffer.ByteBuf;

import net.minecraft.util.MathHelper;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.EnumColor;
import buildcraft.core.lib.network.Packet;
import buildcraft.core.network.PacketIds;
import buildcraft.transport.TravelingItem;

public class PacketPipeTransportTraveler extends Packet {

	public int posX;
	public int posY;
	public int posZ;

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

		byte flags = (byte) ((item.output.ordinal() & 7) | ((item.input.ordinal() & 7) << 3) | (forceStackRefresh ? 64 : 0));
		data.writeByte(flags);

		data.writeByte(item.color != null ? item.color.ordinal() : -1);

		data.writeFloat(item.getSpeed());
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

		int flags = data.readUnsignedByte();

		this.input = ForgeDirection.getOrientation((flags >> 3) & 7);
		this.output = ForgeDirection.getOrientation(flags & 7);

		byte c = data.readByte();
		if (c != -1) {
			this.color = EnumColor.fromId(c);
		}

		this.speed = data.readFloat();

		this.forceStackRefresh = (flags & 0x40) > 0;
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
