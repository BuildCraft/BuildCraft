/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.network;

import buildcraft.api.core.ISerializable;
import buildcraft.core.lib.network.base.Packet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public abstract class PacketUpdate extends Packet {
    public ISerializable payload;

    protected ByteBuf payloadData;

    public PacketUpdate() {}

    public PacketUpdate(ISerializable payload) {
        this.payload = payload;
        this.isChunkDataPacket = true;
    }

    @Override
    public void writeData(ByteBuf data) {
        super.writeData(data);
        writeIdentificationData(data);

        ByteBuf payloadData = Unpooled.buffer();
        if (payload != null) {
            payload.writeData(payloadData);
        }

        int readableBytes = payloadData.readableBytes();
        data.writeInt(readableBytes);
        data.writeBytes(payloadData);
    }

    public abstract void writeIdentificationData(ByteBuf data);

    @Override
    public void readData(ByteBuf data) {
        super.readData(data);
        readIdentificationData(data);
        int length = data.readInt();
        payloadData = Unpooled.copiedBuffer(data.readBytes(length));
    }

    public abstract void readIdentificationData(ByteBuf data);
}
