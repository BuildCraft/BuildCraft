/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import buildcraft.api.enums.EnumColor;
import buildcraft.core.network.BuildCraftPacket;
import buildcraft.core.network.PacketIds;
import buildcraft.transport.TravelingItem;

public class PacketPipeTransportTraveler extends BuildCraftPacket {

	public BlockPos pos;

	private TravelingItem item;
	private boolean forceStackRefresh;
	private int entityId;
	private EnumFacing input;
	private EnumFacing output;
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

		pos = new BlockPos(MathHelper.floor_float(itemX), MathHelper.floor_float(itemY), MathHelper.floor_float(itemZ));

		this.entityId = data.readShort();

		this.input = EnumFacing.getFront(data.readByte());
		this.output = EnumFacing.getFront(data.readByte());

		byte c = data.readByte();
		if (c != -1) {
			this.color = EnumColor.fromId(c);
		}

		this.speed = data.readFloat();

		this.forceStackRefresh = data.readBoolean();
	}

	public int getTravelingEntityId() {
		return entityId;
	}

	public EnumFacing getInputOrientation() {
		return input;
	}

	public EnumFacing getOutputOrientation() {
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
