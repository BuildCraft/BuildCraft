/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import buildcraft.api.core.ISerializable;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public abstract class PacketUpdate extends Packet {
    public ISerializable payload;

    protected ByteBuf payloadData;
    private int packetId;

    public PacketUpdate() {}

    public PacketUpdate(int packetId, ISerializable payload) {
        this(packetId);

        this.payload = payload;
    }

    public PacketUpdate(int packetId) {
        this.packetId = packetId;
        this.isChunkDataPacket = true;
    }

    @Override
    public void writeData(ByteBuf data, World world, EntityPlayer player) {
        super.writeData(data, world, player);
        data.writeByte(packetId);
        writeIdentificationData(data);

        ByteBuf payloadData = Unpooled.buffer();
        if (payload != null) {
            payload.writeData(payloadData);
        }

        data.writeInt(payloadData.readableBytes());
        data.writeBytes(payloadData);
    }

    public abstract void writeIdentificationData(ByteBuf data);

    @Override
    public void readData(ByteBuf data, World world, EntityPlayer player) {
        super.readData(data, world, player);
        packetId = data.readByte();
        readIdentificationData(data);
        int length = data.readInt();
        payloadData = Unpooled.copiedBuffer(data.readBytes(length));
    }

    public abstract void readIdentificationData(ByteBuf data);

    @Override
    public int getID() {
        return packetId;
    }
}
