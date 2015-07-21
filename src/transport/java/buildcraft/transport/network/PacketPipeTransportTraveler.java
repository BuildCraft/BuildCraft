/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.network;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;

import buildcraft.api.enums.EnumColor;
import buildcraft.core.lib.network.Packet;
import buildcraft.core.lib.utils.Utils;
import buildcraft.core.network.PacketIds;
import buildcraft.transport.TravelingItem;

import io.netty.buffer.ByteBuf;

public class PacketPipeTransportTraveler extends Packet {

    public BlockPos pos;

    private TravelingItem item;
    private boolean forceStackRefresh;
    private int entityId;
    private EnumFacing input;
    private EnumFacing output;
    private EnumColor color;
    private Vec3 itemPos;
    private float speed;

    public PacketPipeTransportTraveler() {}

    public PacketPipeTransportTraveler(TravelingItem item, boolean forceStackRefresh) {
        this.item = item;
        this.forceStackRefresh = forceStackRefresh;
    }

    @Override
    public void writeData(ByteBuf data) {
        data.writeFloat((float) item.pos.xCoord);
        data.writeFloat((float) item.pos.yCoord);
        data.writeFloat((float) item.pos.zCoord);

        data.writeShort(item.id);

        int out = item.output == null ? 6 : item.output.ordinal();
        int in = item.input == null ? 6 : item.input.ordinal();

        byte flags = (byte) ((out & 7) | ((in & 7) << 3) | (forceStackRefresh ? 64 : 0));
        data.writeByte(flags);

        data.writeByte(item.color != null ? item.color.ordinal() : -1);

        data.writeFloat(item.getSpeed());
    }

    @Override
    public void readData(ByteBuf data) {
        itemPos = new Vec3(data.readFloat(), data.readFloat(), data.readFloat());

        pos = Utils.convertFloor(itemPos);

        this.entityId = data.readShort();

        int flags = data.readUnsignedByte();

        int in = (flags >> 3) & 7;
        if (in == 6) {
            this.input = null;
        } else {
            this.input = EnumFacing.getFront(in);
        }

        int out = flags & 7;
        if (out == 6) {
            this.output = null;
        } else {
            this.output = EnumFacing.getFront(out);
        }

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

    public EnumFacing getInputOrientation() {
        return input;
    }

    public EnumFacing getOutputOrientation() {
        return output;
    }

    public EnumColor getColor() {
        return color;
    }

    public Vec3 getItemPos() {
        return itemPos;
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
